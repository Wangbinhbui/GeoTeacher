package com.geoteacher.classroom.application;

import com.geoteacher.classroom.api.StudentImportPreviewResponse;
import com.geoteacher.classroom.api.StudentImportResultResponse;
import com.geoteacher.classroom.api.StudentImportRowResponse;
import com.geoteacher.classroom.domain.ClassEnrollment;
import com.geoteacher.classroom.domain.ClassGroupStatus;
import com.geoteacher.classroom.domain.SchoolYearStatus;
import com.geoteacher.classroom.domain.Student;
import com.geoteacher.classroom.domain.StudentStatus;
import com.geoteacher.classroom.infrastructure.ClassEnrollmentRepository;
import com.geoteacher.classroom.infrastructure.ClassGroupRepository;
import com.geoteacher.classroom.infrastructure.StudentRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Đọc/ghi roster Excel. Preview và confirm dùng chung một bộ validator; confirm chạy trong một transaction
 * để không bao giờ lưu nửa danh sách khi một dòng không hợp lệ.
 */
@Service
public class StudentExcelService {

    private static final int MAX_ROWS = 2_000;
    private static final DataFormatter DATA_FORMATTER = new DataFormatter(Locale.ROOT);
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter VI_DATE = DateTimeFormatter.ofPattern("d/M/uuuu");

    private final StudentRepository studentRepository;
    private final ClassGroupRepository classGroupRepository;
    private final ClassEnrollmentRepository enrollmentRepository;
    private final Clock clock;

    public StudentExcelService(
            StudentRepository studentRepository,
            ClassGroupRepository classGroupRepository,
            ClassEnrollmentRepository enrollmentRepository,
            Clock clock
    ) {
        this.studentRepository = studentRepository;
        this.classGroupRepository = classGroupRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public StudentImportPreviewResponse preview(UUID classGroupId, MultipartFile file) {
        var classGroup = activeClassGroup(classGroupId);
        return validate(classGroup.getId(), parse(file));
    }

    @Transactional
    public StudentImportResultResponse confirm(UUID classGroupId, LocalDate enrolledAt, MultipartFile file) {
        if (enrolledAt.isAfter(LocalDate.now(clock))) {
            throw invalid("IMPORT_ENROLLED_AT_FUTURE", "Ngày ghi danh không được ở tương lai.");
        }
        var classGroup = activeClassGroup(classGroupId);
        var rows = parse(file);
        var preview = validate(classGroupId, rows);
        if (!preview.valid()) {
            throw invalid("IMPORT_HAS_ERRORS", "File còn lỗi. Hãy sửa toàn bộ dòng lỗi và preview lại.");
        }

        var created = 0;
        try {
            for (var row : rows) {
                var normalizedCode = Student.normalize(row.studentCode());
                var student = studentRepository.findByStudentCodeNormalized(normalizedCode).orElse(null);
                if (student == null) {
                    student = studentRepository.saveAndFlush(Student.create(
                            row.studentCode(), row.fullName(), row.dateOfBirth(), now()
                    ));
                    created++;
                }
                enrollmentRepository.save(ClassEnrollment.create(classGroup, student, enrolledAt, now()));
            }
            enrollmentRepository.flush();
            return new StudentImportResultResponse(created, rows.size());
        } catch (DataIntegrityViolationException exception) {
            throw new ClassroomException(
                    "IMPORT_CONCURRENT_CONFLICT",
                    ClassroomErrorType.CONFLICT,
                    "Dữ liệu đã thay đổi sau preview. Hãy preview lại trước khi import."
            );
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportClassRoster(UUID classGroupId) {
        var classGroup = classGroupRepository.findById(classGroupId).orElseThrow(() -> notFound(
                "CLASS_GROUP_NOT_FOUND", "Không tìm thấy lớp học."
        ));
        var enrollments = enrollmentRepository.findAllByClassGroupIdOrderByStudentFullNameAsc(classGroupId);
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Danh sach hoc sinh");
            var headerStyle = headerStyle(workbook);
            var dateStyle = dateStyle(workbook);
            var title = sheet.createRow(0);
            title.createCell(0).setCellValue("Danh sách " + classGroup.getDisplayName() + " - " + classGroup.getSchoolYear().getCode());
            title.getCell(0).setCellStyle(titleStyle(workbook));
            var header = sheet.createRow(2);
            writeHeader(header, headerStyle, "Mã học sinh", "Họ và tên", "Ngày sinh", "Ngày vào lớp", "Ngày rời lớp", "Trạng thái");
            var rowIndex = 3;
            for (var enrollment : enrollments) {
                var row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(enrollment.getStudent().getStudentCode());
                row.createCell(1).setCellValue(enrollment.getStudent().getFullName());
                writeDate(row.createCell(2), enrollment.getStudent().getDateOfBirth(), dateStyle);
                writeDate(row.createCell(3), enrollment.getEnrolledAt(), dateStyle);
                writeDate(row.createCell(4), enrollment.getLeftAt(), dateStyle);
                row.createCell(5).setCellValue(enrollment.getStatus().name());
            }
            setRosterWidths(sheet);
            sheet.createFreezePane(0, 3);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw invalid("EXCEL_EXPORT_FAILED", "Không thể tạo file Excel.");
        }
    }

    public byte[] template() {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Hoc sinh");
            var header = sheet.createRow(0);
            writeHeader(header, headerStyle(workbook), "student_code", "full_name", "date_of_birth");
            var example = sheet.createRow(1);
            example.createCell(0).setCellValue("HS001");
            example.createCell(1).setCellValue("Nguyễn Văn An");
            var dateCell = example.createCell(2);
            dateCell.setCellValue(java.sql.Date.valueOf(LocalDate.of(2010, 3, 12)));
            dateCell.setCellStyle(dateStyle(workbook));
            sheet.setColumnWidth(0, 18 * 256);
            sheet.setColumnWidth(1, 32 * 256);
            sheet.setColumnWidth(2, 18 * 256);
            sheet.createFreezePane(0, 1);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw invalid("EXCEL_TEMPLATE_FAILED", "Không thể tạo file mẫu Excel.");
        }
    }

    private StudentImportPreviewResponse validate(UUID classGroupId, List<ImportRow> rows) {
        var seenCodes = new HashSet<String>();
        var results = new ArrayList<StudentImportRowResponse>();
        var newStudents = 0;
        var existingStudents = 0;
        for (var row : rows) {
            var errors = new ArrayList<String>(row.parseErrors());
            var normalizedCode = normalizeNullable(row.studentCode());
            if (normalizedCode.isBlank()) errors.add("Thiếu mã học sinh.");
            if (row.fullName() == null || row.fullName().isBlank()) errors.add("Thiếu họ và tên.");
            if (!normalizedCode.isBlank() && !seenCodes.add(normalizedCode)) {
                errors.add("Mã học sinh bị lặp trong file.");
            }
            var existing = normalizedCode.isBlank()
                    ? null
                    : studentRepository.findByStudentCodeNormalized(normalizedCode).orElse(null);
            var action = "CREATE_STUDENT_AND_ENROLL";
            if (existing != null) {
                action = "ENROLL_EXISTING";
                existingStudents++;
                if (existing.getStatus() == StudentStatus.ARCHIVED) errors.add("Học sinh đã lưu trữ.");
                if (!normalizeNullable(existing.getFullName()).equals(normalizeNullable(row.fullName()))) {
                    errors.add("Họ tên không khớp hồ sơ đã có cùng mã.");
                }
                if (row.dateOfBirth() != null && existing.getDateOfBirth() != null
                        && !row.dateOfBirth().equals(existing.getDateOfBirth())) {
                    errors.add("Ngày sinh không khớp hồ sơ đã có cùng mã.");
                }
                if (enrollmentRepository.existsByClassGroupIdAndStudentId(classGroupId, existing.getId())) {
                    errors.add("Học sinh đã có trong lớp này.");
                }
            } else {
                newStudents++;
            }
            if (!errors.isEmpty()) action = "INVALID";
            results.add(new StudentImportRowResponse(
                    row.rowNumber(), row.studentCode(), row.fullName(), row.dateOfBirth(), action, List.copyOf(errors)
            ));
        }
        var valid = !rows.isEmpty() && results.stream().allMatch(result -> result.errors().isEmpty());
        return new StudentImportPreviewResponse(valid, rows.size(), newStudents, existingStudents, results);
    }

    private List<ImportRow> parse(MultipartFile file) {
        if (file == null || file.isEmpty()) throw invalid("IMPORT_FILE_EMPTY", "Hãy chọn file Excel có dữ liệu.");
        var name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".xlsx")) throw invalid("IMPORT_FILE_TYPE", "Chỉ hỗ trợ file .xlsx.");
        try (InputStream input = file.getInputStream(); var workbook = new XSSFWorkbook(input)) {
            var sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null) throw invalid("IMPORT_SHEET_MISSING", "File không có worksheet dữ liệu.");
            verifyHeader(sheet.getRow(0));
            var rows = new ArrayList<ImportRow>();
            for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                var row = sheet.getRow(index);
                if (row == null || blankRow(row)) continue;
                if (rows.size() >= MAX_ROWS) throw invalid("IMPORT_TOO_MANY_ROWS", "Mỗi lần chỉ import tối đa 2.000 học sinh.");
                var errors = new ArrayList<String>();
                var code = text(row.getCell(0));
                var fullName = text(row.getCell(1));
                var dateOfBirth = parseDate(row.getCell(2), errors);
                rows.add(new ImportRow(index + 1, code, fullName, dateOfBirth, errors));
            }
            if (rows.isEmpty()) throw invalid("IMPORT_NO_DATA", "File không có dòng học sinh nào.");
            return rows;
        } catch (ClassroomException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw invalid("IMPORT_FILE_INVALID", "Không thể đọc file Excel. Hãy dùng file mẫu của GeoTeacher.");
        }
    }

    private void verifyHeader(Row header) {
        if (header == null
                || !"student_code".equalsIgnoreCase(text(header.getCell(0)))
                || !"full_name".equalsIgnoreCase(text(header.getCell(1)))
                || !"date_of_birth".equalsIgnoreCase(text(header.getCell(2)))) {
            throw invalid("IMPORT_HEADER_INVALID", "Header phải là student_code, full_name, date_of_birth.");
        }
    }

    private LocalDate parseDate(Cell cell, List<String> errors) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        var value = text(cell);
        if (value.isBlank()) return null;
        for (var formatter : List.of(ISO_DATE, VI_DATE)) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Thử định dạng kế tiếp.
            }
        }
        errors.add("Ngày sinh phải là ngày Excel, yyyy-MM-dd hoặc dd/MM/yyyy.");
        return null;
    }

    private String text(Cell cell) {
        if (cell == null) return "";
        return DATA_FORMATTER.formatCellValue(cell).trim();
    }

    private boolean blankRow(Row row) {
        return text(row.getCell(0)).isBlank() && text(row.getCell(1)).isBlank() && text(row.getCell(2)).isBlank();
    }

    private com.geoteacher.classroom.domain.ClassGroup activeClassGroup(UUID id) {
        var classGroup = classGroupRepository.findById(id).orElseThrow(() -> notFound(
                "CLASS_GROUP_NOT_FOUND", "Không tìm thấy lớp học."
        ));
        if (classGroup.getStatus() == ClassGroupStatus.ARCHIVED
                || classGroup.getSchoolYear().getStatus() == SchoolYearStatus.ARCHIVED) {
            throw invalid("CLASS_GROUP_ARCHIVED", "Không thể import vào lớp đã lưu trữ.");
        }
        return classGroup;
    }

    private CellStyle headerStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        var font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle titleStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        var font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle dateStyle(Workbook workbook) {
        var style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    private void writeHeader(Row row, CellStyle style, String... labels) {
        for (int index = 0; index < labels.length; index++) {
            var cell = row.createCell(index);
            cell.setCellValue(labels[index]);
            cell.setCellStyle(style);
        }
    }

    private void writeDate(Cell cell, LocalDate value, CellStyle style) {
        if (value == null) return;
        cell.setCellValue(java.sql.Date.valueOf(value));
        cell.setCellStyle(style);
    }

    private void setRosterWidths(Sheet sheet) {
        int[] widths = {18, 32, 16, 16, 16, 16};
        for (int index = 0; index < widths.length; index++) sheet.setColumnWidth(index, widths[index] * 256);
    }

    private String normalizeNullable(String value) {
        return value == null ? "" : Student.normalize(value);
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private ClassroomException invalid(String code, String message) {
        return new ClassroomException(code, ClassroomErrorType.INVALID_STATE, message);
    }

    private ClassroomException notFound(String code, String message) {
        return new ClassroomException(code, ClassroomErrorType.NOT_FOUND, message);
    }

    private record ImportRow(
            int rowNumber,
            String studentCode,
            String fullName,
            LocalDate dateOfBirth,
            List<String> parseErrors
    ) {
    }
}
