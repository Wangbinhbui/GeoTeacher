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
import java.time.LocalDate;
import java.util.UUID;

/** Enrollment giữ quan hệ lịch sử lớp–học sinh; rời lớp là state transition, không xóa bản ghi. */
@Entity
@Table(name = "class_enrollments")
public class ClassEnrollment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_group_id", nullable = false)
    private ClassGroup classGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDate enrolledAt;

    @Column(name = "left_at")
    private LocalDate leftAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected ClassEnrollment() {
    }

    private ClassEnrollment(ClassGroup classGroup, Student student, LocalDate enrolledAt, Instant now) {
        this.id = UUID.randomUUID();
        this.classGroup = classGroup;
        this.student = student;
        this.enrolledAt = enrolledAt;
        this.status = EnrollmentStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static ClassEnrollment create(
            ClassGroup classGroup,
            Student student,
            LocalDate enrolledAt,
            Instant now
    ) {
        return new ClassEnrollment(classGroup, student, enrolledAt, now);
    }

    public void leave(LocalDate leftAt, Instant now) {
        if (status == EnrollmentStatus.LEFT) {
            throw new IllegalStateException("Ghi danh này đã kết thúc.");
        }
        if (leftAt.isBefore(enrolledAt)) {
            throw new IllegalArgumentException("Ngày rời lớp không thể trước ngày ghi danh.");
        }
        this.leftAt = leftAt;
        this.status = EnrollmentStatus.LEFT;
        this.updatedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public ClassGroup getClassGroup() {
        return classGroup;
    }

    public Student getStudent() {
        return student;
    }

    public LocalDate getEnrolledAt() {
        return enrolledAt;
    }

    public LocalDate getLeftAt() {
        return leftAt;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }
}
