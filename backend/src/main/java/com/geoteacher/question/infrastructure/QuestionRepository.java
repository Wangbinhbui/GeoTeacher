package com.geoteacher.question.infrastructure;

import com.geoteacher.question.domain.Question;
import com.geoteacher.question.domain.QuestionStatus;
import com.geoteacher.question.domain.QuestionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @Query("""
            select question from Question question
            where (:query = '' or question.contentNormalized like concat('%', :query, '%')
                   or lower(question.topic) like concat('%', :query, '%'))
              and (:type is null or question.type = :type)
              and (:status is null or question.status = :status)
              and (:gradeLevel is null or question.gradeLevel = :gradeLevel)
            order by question.updatedAt desc
            """)
    List<Question> search(
            @Param("query") String query,
            @Param("type") QuestionType type,
            @Param("status") QuestionStatus status,
            @Param("gradeLevel") Integer gradeLevel,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "options")
    @Query("select question from Question question where question.id = :id")
    Optional<Question> findDetailById(@Param("id") UUID id);
}
