package com.geoteacher.question.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Question là aggregate của nội dung và đáp án; mọi cập nhật phương án đi qua aggregate để giữ invariant chấm. */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType type;

    @Column(name = "grade_level", nullable = false)
    private int gradeLevel;

    @Column(nullable = false, length = 150)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "cognitive_level", nullable = false, length = 30)
    private CognitiveLevel cognitiveLevel;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "content_normalized", nullable = false, columnDefinition = "text")
    private String contentNormalized;

    @Column(columnDefinition = "text")
    private String explanation;

    @Column(name = "suggested_score", precision = 7, scale = 3)
    private BigDecimal suggestedScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position asc")
    private List<QuestionOption> options = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Question() {
    }

    private Question(Instant now) {
        this.id = UUID.randomUUID();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Question createSingleChoice(
            int gradeLevel,
            String topic,
            CognitiveLevel cognitiveLevel,
            String content,
            String explanation,
            BigDecimal suggestedScore,
            QuestionStatus status,
            List<OptionInput> options,
            Instant now
    ) {
        var question = new Question(now);
        question.apply(gradeLevel, topic, cognitiveLevel, content, explanation, suggestedScore, status, options, now);
        return question;
    }

    public void updateSingleChoice(
            int gradeLevel,
            String topic,
            CognitiveLevel cognitiveLevel,
            String content,
            String explanation,
            BigDecimal suggestedScore,
            QuestionStatus status,
            List<OptionInput> optionInputs,
            Instant now
    ) {
        if (this.status == QuestionStatus.ARCHIVED) {
            throw new IllegalStateException("Câu hỏi đã lưu trữ không thể chỉnh sửa.");
        }
        apply(gradeLevel, topic, cognitiveLevel, content, explanation, suggestedScore, status, optionInputs, now);
    }

    public void archive(Instant now) {
        this.status = QuestionStatus.ARCHIVED;
        this.updatedAt = now;
    }

    private void apply(
            int gradeLevel,
            String topic,
            CognitiveLevel cognitiveLevel,
            String content,
            String explanation,
            BigDecimal suggestedScore,
            QuestionStatus status,
            List<OptionInput> optionInputs,
            Instant now
    ) {
        if (gradeLevel < 1 || gradeLevel > 12) throw new IllegalArgumentException("Khối lớp phải từ 1 đến 12.");
        if (status == QuestionStatus.ARCHIVED) throw new IllegalArgumentException("Hãy dùng thao tác lưu trữ riêng.");
        if (suggestedScore != null && suggestedScore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Điểm gợi ý phải lớn hơn 0.");
        }
        validateOptions(optionInputs);
        this.type = QuestionType.SINGLE_CHOICE;
        this.gradeLevel = gradeLevel;
        this.topic = topic.trim();
        this.cognitiveLevel = cognitiveLevel;
        this.content = content.trim();
        this.contentNormalized = normalize(content);
        this.explanation = blankToNull(explanation);
        this.suggestedScore = suggestedScore;
        this.status = status;
        this.updatedAt = now;
        this.options.clear();
        for (int index = 0; index < optionInputs.size(); index++) {
            var option = optionInputs.get(index);
            this.options.add(new QuestionOption(this, option.key(), option.content(), option.correct(), index));
        }
    }

    private void validateOptions(List<OptionInput> options) {
        if (options == null || options.size() < 2) throw new IllegalArgumentException("Cần ít nhất hai phương án.");
        if (options.stream().filter(OptionInput::correct).count() != 1) {
            throw new IllegalArgumentException("Câu trắc nghiệm phải có đúng một đáp án đúng.");
        }
        var keys = options.stream().map(option -> option.key().trim().toUpperCase()).distinct().count();
        if (keys != options.size()) throw new IllegalArgumentException("Ký hiệu phương án không được trùng.");
        if (options.stream().anyMatch(option -> option.content() == null || option.content().isBlank())) {
            throw new IllegalArgumentException("Nội dung phương án không được để trống.");
        }
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public UUID getId() { return id; }
    public QuestionType getType() { return type; }
    public int getGradeLevel() { return gradeLevel; }
    public String getTopic() { return topic; }
    public CognitiveLevel getCognitiveLevel() { return cognitiveLevel; }
    public String getContent() { return content; }
    public String getExplanation() { return explanation; }
    public BigDecimal getSuggestedScore() { return suggestedScore; }
    public QuestionStatus getStatus() { return status; }
    public List<QuestionOption> getOptions() { return List.copyOf(options); }
    public long getVersion() { return version; }

    public record OptionInput(String key, String content, boolean correct) {
    }
}
