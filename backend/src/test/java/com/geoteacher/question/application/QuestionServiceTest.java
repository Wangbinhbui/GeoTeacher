package com.geoteacher.question.application;

import com.geoteacher.question.api.QuestionOptionRequest;
import com.geoteacher.question.api.QuestionRequest;
import com.geoteacher.question.domain.CognitiveLevel;
import com.geoteacher.question.domain.Question;
import com.geoteacher.question.domain.QuestionStatus;
import com.geoteacher.question.domain.QuestionType;
import com.geoteacher.question.infrastructure.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock private QuestionRepository repository;
    private QuestionService service;

    @BeforeEach
    void setUp() {
        service = new QuestionService(
                repository,
                Clock.fixed(Instant.parse("2026-10-13T00:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void createsSingleChoiceWithExactlyOneCorrectAnswer() {
        when(repository.saveAndFlush(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(request(List.of(
                new QuestionOptionRequest("A", "Đồng bằng sông Hồng", false),
                new QuestionOptionRequest("B", "Đồng bằng sông Cửu Long", true),
                new QuestionOptionRequest("C", "Tây Nguyên", false),
                new QuestionOptionRequest("D", "Đông Nam Bộ", false)
        )));

        assertThat(result.options()).hasSize(4);
        assertThat(result.options()).filteredOn(option -> option.correct()).hasSize(1);
    }

    @Test
    void rejectsSingleChoiceWithTwoCorrectAnswers() {
        assertThatThrownBy(() -> service.create(request(List.of(
                new QuestionOptionRequest("A", "Phương án A", true),
                new QuestionOptionRequest("B", "Phương án B", true)
        ))))
                .isInstanceOf(QuestionException.class)
                .extracting(exception -> ((QuestionException) exception).getCode())
                .isEqualTo("QUESTION_INVALID");
    }

    @Test
    void rejectsQuestionTypesNotImplementedInThisSlice() {
        var request = new QuestionRequest(
                QuestionType.ESSAY, 10, "Địa lí dân cư", CognitiveLevel.APPLICATION,
                "Phân tích đặc điểm dân số.", null, "1.000", QuestionStatus.DRAFT,
                List.of(new QuestionOptionRequest("A", "Tạm", true)), null
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(QuestionException.class)
                .extracting(exception -> ((QuestionException) exception).getCode())
                .isEqualTo("QUESTION_TYPE_NOT_IMPLEMENTED");
    }

    private QuestionRequest request(List<QuestionOptionRequest> options) {
        return new QuestionRequest(
                QuestionType.SINGLE_CHOICE, 10, "Địa lí dân cư", CognitiveLevel.RECOGNITION,
                "Vùng nào có sản lượng lúa lớn nhất Việt Nam?", "Đồng bằng sông Cửu Long dẫn đầu.",
                "0.250", QuestionStatus.ACTIVE, options, null
        );
    }
}
