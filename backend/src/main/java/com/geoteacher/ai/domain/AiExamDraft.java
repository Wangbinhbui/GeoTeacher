package com.geoteacher.ai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_exam_drafts")
public class AiExamDraft {
    @Id private UUID id;
    @Enumerated(EnumType.STRING) @Column(name = "exam_type", nullable = false, length = 30) private AiExamType examType;
    @Column(name = "duration_minutes", nullable = false) private int durationMinutes;
    @Column(name = "grade_level", nullable = false) private int gradeLevel;
    @Column(nullable = false, length = 255) private String title;
    @Column(name = "teacher_instructions", length = 2000) private String teacherInstructions;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false, length = 30) private String provider;
    @Column(name = "model_name", nullable = false, length = 120) private String modelName;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "content_json", nullable = false, columnDefinition = "jsonb") private JsonNode contentJson;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected AiExamDraft() {
    }

    public static AiExamDraft create(AiExamType examType, int durationMinutes, int gradeLevel, String title,
                                     String teacherInstructions, String provider, String modelName,
                                     JsonNode contentJson, Instant now) {
        var draft = new AiExamDraft();
        draft.id = UUID.randomUUID();
        draft.examType = examType;
        draft.durationMinutes = durationMinutes;
        draft.gradeLevel = gradeLevel;
        draft.title = title;
        draft.teacherInstructions = teacherInstructions;
        draft.status = "DRAFT";
        draft.provider = provider;
        draft.modelName = modelName;
        draft.contentJson = contentJson;
        draft.createdAt = now;
        return draft;
    }

    public UUID getId() { return id; }
    public AiExamType getExamType() { return examType; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getGradeLevel() { return gradeLevel; }
    public String getTitle() { return title; }
    public String getTeacherInstructions() { return teacherInstructions; }
    public String getStatus() { return status; }
    public String getProvider() { return provider; }
    public String getModelName() { return modelName; }
    public JsonNode getContentJson() { return contentJson; }
    public Instant getCreatedAt() { return createdAt; }
}
