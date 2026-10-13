package com.geoteacher.question.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/** Phương án thuộc aggregate Question; không có repository/API ghi riêng để giữ invariant đúng một đáp án. */
@Entity
@Table(name = "question_options")
public class QuestionOption {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "option_key", nullable = false, length = 10)
    private String optionKey;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int position;

    protected QuestionOption() {
    }

    QuestionOption(Question question, String optionKey, String content, boolean correct, int position) {
        this.id = UUID.randomUUID();
        this.question = question;
        this.optionKey = optionKey.trim().toUpperCase();
        this.content = content.trim();
        this.correct = correct;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public String getOptionKey() {
        return optionKey;
    }

    public String getContent() {
        return content;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getPosition() {
        return position;
    }
}
