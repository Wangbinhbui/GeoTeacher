package com.geoteacher.question.application;

import com.geoteacher.question.api.QuestionDetailResponse;
import com.geoteacher.question.api.QuestionRequest;
import com.geoteacher.question.api.QuestionSummaryResponse;
import com.geoteacher.question.domain.Question;
import com.geoteacher.question.domain.QuestionStatus;
import com.geoteacher.question.domain.QuestionType;
import com.geoteacher.question.infrastructure.QuestionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Điều phối CRUD question; domain giữ invariant đáp án, còn service giữ version và loại đã hỗ trợ. */
@Service
public class QuestionService {

    private static final int SEARCH_LIMIT = 100;
    private final QuestionRepository repository;
    private final Clock clock;

    public QuestionService(QuestionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<QuestionSummaryResponse> search(
            String query,
            QuestionType type,
            QuestionStatus status,
            Integer gradeLevel
    ) {
        var normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return repository.search(normalized, type, status, gradeLevel, PageRequest.of(0, SEARCH_LIMIT)).stream()
                .map(QuestionSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuestionDetailResponse detail(UUID id) {
        return QuestionDetailResponse.from(get(id));
    }

    @Transactional
    public QuestionDetailResponse create(QuestionRequest request) {
        verifySupportedType(request.type());
        try {
            return QuestionDetailResponse.from(repository.saveAndFlush(Question.createSingleChoice(
                    request.gradeLevel(), request.topic(), request.cognitiveLevel(), request.content(),
                    request.explanation(), score(request.suggestedScore()), request.status(), options(request), now()
            )));
        } catch (IllegalArgumentException exception) {
            throw invalid(exception.getMessage());
        }
    }

    @Transactional
    public QuestionDetailResponse update(UUID id, QuestionRequest request) {
        verifySupportedType(request.type());
        var question = get(id);
        verifyVersion(question.getVersion(), request.version());
        try {
            question.updateSingleChoice(
                    request.gradeLevel(), request.topic(), request.cognitiveLevel(), request.content(),
                    request.explanation(), score(request.suggestedScore()), request.status(), options(request), now()
            );
            return QuestionDetailResponse.from(repository.saveAndFlush(question));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw invalid(exception.getMessage());
        }
    }

    @Transactional
    public QuestionDetailResponse archive(UUID id, long version) {
        var question = get(id);
        verifyVersion(question.getVersion(), version);
        question.archive(now());
        return QuestionDetailResponse.from(repository.saveAndFlush(question));
    }

    private Question get(UUID id) {
        return repository.findDetailById(id).orElseThrow(() -> new QuestionException(
                "QUESTION_NOT_FOUND", QuestionErrorType.NOT_FOUND, "Không tìm thấy câu hỏi."
        ));
    }

    private void verifySupportedType(QuestionType type) {
        if (type != QuestionType.SINGLE_CHOICE) {
            throw new QuestionException(
                    "QUESTION_TYPE_NOT_IMPLEMENTED", QuestionErrorType.INVALID_STATE,
                    "Lát cắt hiện tại mới hỗ trợ câu trắc nghiệm một đáp án."
            );
        }
    }

    private void verifyVersion(long current, Long requested) {
        if (requested == null || current != requested) {
            throw new QuestionException(
                    "OPTIMISTIC_CONFLICT", QuestionErrorType.CONFLICT,
                    "Dữ liệu đã thay đổi. Hãy tải lại trước khi lưu."
            );
        }
    }

    private List<Question.OptionInput> options(QuestionRequest request) {
        return request.options().stream()
                .map(option -> new Question.OptionInput(option.key(), option.content(), option.correct()))
                .toList();
    }

    private BigDecimal score(String value) {
        return value == null || value.isBlank() ? null : new BigDecimal(value);
    }

    private QuestionException invalid(String message) {
        return new QuestionException("QUESTION_INVALID", QuestionErrorType.INVALID_STATE, message);
    }

    private Instant now() { return Instant.now(clock); }
}
