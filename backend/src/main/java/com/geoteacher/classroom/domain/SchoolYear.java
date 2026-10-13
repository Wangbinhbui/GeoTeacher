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

/** Năm học là aggregate gốc để lớp và dữ liệu lịch sử luôn có phạm vi thời gian rõ ràng. */
@Entity
@Table(name = "school_years")
public class SchoolYear {

    @Id
    private UUID id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(name = "code_normalized", nullable = false, length = 30, unique = true)
    private String codeNormalized;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SchoolYearStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected SchoolYear() {
    }

    private SchoolYear(String code, LocalDate startDate, LocalDate endDate, SchoolYearStatus status, Instant now) {
        this.id = UUID.randomUUID();
        apply(code, startDate, endDate, status, now);
        this.createdAt = now;
    }

    public static SchoolYear create(
            String code,
            LocalDate startDate,
            LocalDate endDate,
            SchoolYearStatus status,
            Instant now
    ) {
        return new SchoolYear(code, startDate, endDate, status, now);
    }

    public void update(
            String code,
            LocalDate startDate,
            LocalDate endDate,
            SchoolYearStatus status,
            Instant now
    ) {
        if (this.status == SchoolYearStatus.ARCHIVED) {
            throw new IllegalStateException("Năm học đã lưu trữ không thể chỉnh sửa.");
        }
        apply(code, startDate, endDate, status, now);
    }

    public void archive(Instant now) {
        this.status = SchoolYearStatus.ARCHIVED;
        this.updatedAt = now;
    }

    private void apply(
            String code,
            LocalDate startDate,
            LocalDate endDate,
            SchoolYearStatus status,
            Instant now
    ) {
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc.");
        }
        if (status == SchoolYearStatus.ARCHIVED) {
            throw new IllegalArgumentException("Hãy dùng thao tác lưu trữ riêng.");
        }
        this.code = code.trim();
        this.codeNormalized = normalizeCode(code);
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.updatedAt = now;
    }

    public static String normalizeCode(String code) {
        return code.trim().toLowerCase(Locale.ROOT);
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public SchoolYearStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }
}
