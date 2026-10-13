package com.geoteacher.classroom.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/** Học sinh có identity xuyên năm học; việc thuộc lớp được lưu riêng bằng enrollment. */
@Entity
@Table(name = "students")
public class Student {

    @Id
    private UUID id;

    @Column(name = "student_code", nullable = false, length = 50)
    private String studentCode;

    @Column(name = "student_code_normalized", nullable = false, length = 50, unique = true)
    private String studentCodeNormalized;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "full_name_normalized", nullable = false, length = 150)
    private String fullNameNormalized;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Student() {
    }

    private Student(String studentCode, String fullName, LocalDate dateOfBirth, Instant now) {
        this.id = UUID.randomUUID();
        this.status = StudentStatus.ACTIVE;
        this.createdAt = now;
        apply(studentCode, fullName, dateOfBirth, now);
    }

    public static Student create(String studentCode, String fullName, LocalDate dateOfBirth, Instant now) {
        return new Student(studentCode, fullName, dateOfBirth, now);
    }

    public void update(String studentCode, String fullName, LocalDate dateOfBirth, Instant now) {
        if (status == StudentStatus.ARCHIVED) {
            throw new IllegalStateException("Học sinh đã lưu trữ không thể chỉnh sửa.");
        }
        apply(studentCode, fullName, dateOfBirth, now);
    }

    public void archive(Instant now) {
        this.status = StudentStatus.ARCHIVED;
        this.updatedAt = now;
    }

    private void apply(String studentCode, String fullName, LocalDate dateOfBirth, Instant now) {
        this.studentCode = studentCode.trim();
        this.studentCodeNormalized = normalize(studentCode);
        this.fullName = fullName.trim();
        this.fullNameNormalized = normalize(fullName);
        this.dateOfBirth = dateOfBirth;
        this.updatedAt = now;
    }

    public static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public UUID getId() {
        return id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }
}
