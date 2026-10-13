package com.geoteacher.classroom.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/** Lớp học thuộc đúng một năm học; mã lớp chỉ duy nhất trong phạm vi năm học đó. */
@Entity
@Table(name = "class_groups")
public class ClassGroup {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_year_id", nullable = false)
    private SchoolYear schoolYear;

    @Column(name = "class_code", nullable = false, length = 30)
    private String classCode;

    @Column(name = "class_code_normalized", nullable = false, length = 30)
    private String classCodeNormalized;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "grade_level", nullable = false)
    private int gradeLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClassGroupStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected ClassGroup() {
    }

    private ClassGroup(
            SchoolYear schoolYear,
            String classCode,
            String displayName,
            int gradeLevel,
            Instant now
    ) {
        this.id = UUID.randomUUID();
        this.schoolYear = schoolYear;
        this.status = ClassGroupStatus.ACTIVE;
        this.createdAt = now;
        apply(classCode, displayName, gradeLevel, now);
    }

    public static ClassGroup create(
            SchoolYear schoolYear,
            String classCode,
            String displayName,
            int gradeLevel,
            Instant now
    ) {
        return new ClassGroup(schoolYear, classCode, displayName, gradeLevel, now);
    }

    public void update(String classCode, String displayName, int gradeLevel, Instant now) {
        if (status == ClassGroupStatus.ARCHIVED) {
            throw new IllegalStateException("Lớp đã lưu trữ không thể chỉnh sửa.");
        }
        apply(classCode, displayName, gradeLevel, now);
    }

    public void archive(Instant now) {
        this.status = ClassGroupStatus.ARCHIVED;
        this.updatedAt = now;
    }

    private void apply(String classCode, String displayName, int gradeLevel, Instant now) {
        if (gradeLevel < 1 || gradeLevel > 12) {
            throw new IllegalArgumentException("Khối lớp phải nằm trong khoảng 1 đến 12.");
        }
        this.classCode = classCode.trim();
        this.classCodeNormalized = normalizeCode(classCode);
        this.displayName = displayName.trim();
        this.gradeLevel = gradeLevel;
        this.updatedAt = now;
    }

    public static String normalizeCode(String code) {
        return code.trim().toLowerCase(Locale.ROOT);
    }

    public UUID getId() {
        return id;
    }

    public SchoolYear getSchoolYear() {
        return schoolYear;
    }

    public String getClassCode() {
        return classCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public ClassGroupStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }
}
