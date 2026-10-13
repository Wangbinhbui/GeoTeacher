package com.geoteacher.classroom.application;

import com.geoteacher.classroom.domain.ClassGroup;
import com.geoteacher.classroom.domain.SchoolYear;
import com.geoteacher.classroom.domain.SchoolYearStatus;
import com.geoteacher.classroom.domain.Student;
import com.geoteacher.classroom.infrastructure.ClassEnrollmentRepository;
import com.geoteacher.classroom.infrastructure.ClassGroupRepository;
import com.geoteacher.classroom.infrastructure.StudentRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentExcelServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-13T00:00:00Z");

    @Mock private StudentRepository studentRepository;
    @Mock private ClassGroupRepository classGroupRepository;
    @Mock private ClassEnrollmentRepository enrollmentRepository;

    private StudentExcelService service;
    private ClassGroup classGroup;

    @BeforeEach
    void setUp() {
        service = new StudentExcelService(
                studentRepository, classGroupRepository, enrollmentRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        var year = SchoolYear.create(
                "2026-2027", LocalDate.of(2026, 8, 1), LocalDate.of(2027, 5, 31),
                SchoolYearStatus.ACTIVE, NOW
        );
        classGroup = ClassGroup.create(year, "10A1", "Lớp 10A1", 10, NOW);
    }

    @Test
    void generatedTemplateCanBePreviewed() {
        when(classGroupRepository.findById(classGroup.getId())).thenReturn(Optional.of(classGroup));
        when(studentRepository.findByStudentCodeNormalized("hs001")).thenReturn(Optional.empty());
        var file = new MockMultipartFile(
                "file", "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                service.template()
        );

        var preview = service.preview(classGroup.getId(), file);

        assertThat(preview.valid()).isTrue();
        assertThat(preview.totalRows()).isEqualTo(1);
        assertThat(preview.newStudents()).isEqualTo(1);
        assertThat(preview.rows().getFirst().studentCode()).isEqualTo("HS001");
    }

    @Test
    void confirmDoesNotSaveAnythingWhenFileContainsDuplicateCodes() throws Exception {
        when(classGroupRepository.findById(classGroup.getId())).thenReturn(Optional.of(classGroup));
        when(studentRepository.findByStudentCodeNormalized(any())).thenReturn(Optional.empty());
        var file = workbookWithRows(
                new String[]{"HS001", "Nguyễn Văn An", "2010-03-12"},
                new String[]{"hs001", "Nguyễn Văn Bình", "2010-04-12"}
        );

        assertThatThrownBy(() -> service.confirm(
                classGroup.getId(), LocalDate.of(2026, 8, 15), file
        ))
                .isInstanceOf(ClassroomException.class)
                .extracting(exception -> ((ClassroomException) exception).getCode())
                .isEqualTo("IMPORT_HAS_ERRORS");

        verify(studentRepository, never()).saveAndFlush(any(Student.class));
        verify(enrollmentRepository, never()).save(any());
    }

    private MockMultipartFile workbookWithRows(String[]... values) throws Exception {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Hoc sinh");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("student_code");
            header.createCell(1).setCellValue("full_name");
            header.createCell(2).setCellValue("date_of_birth");
            for (int rowIndex = 0; rowIndex < values.length; rowIndex++) {
                var row = sheet.createRow(rowIndex + 1);
                for (int cellIndex = 0; cellIndex < values[rowIndex].length; cellIndex++) {
                    row.createCell(cellIndex).setCellValue(values[rowIndex][cellIndex]);
                }
            }
            workbook.write(output);
            return new MockMultipartFile("file", "students.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray());
        }
    }
}
