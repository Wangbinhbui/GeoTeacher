package com.geoteacher.classroom.infrastructure;

import com.geoteacher.classroom.domain.Student;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    @Query("""
            select student from Student student
            where :query = ''
               or student.studentCodeNormalized like concat('%', :query, '%')
               or student.fullNameNormalized like concat('%', :query, '%')
            order by student.fullName asc
            """)
    List<Student> search(@Param("query") String query, Pageable pageable);

    boolean existsByStudentCodeNormalized(String studentCodeNormalized);

    boolean existsByStudentCodeNormalizedAndIdNot(String studentCodeNormalized, UUID id);
}
