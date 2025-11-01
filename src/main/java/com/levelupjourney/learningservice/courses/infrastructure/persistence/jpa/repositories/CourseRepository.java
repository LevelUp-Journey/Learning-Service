package com.levelupjourney.learningservice.courses.infrastructure.persistence.jpa.repositories;

import com.levelupjourney.learningservice.courses.domain.model.aggregates.Course;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    
    @Query("SELECT DISTINCT c FROM Course c " +
           "LEFT JOIN FETCH c.authors " +
           "LEFT JOIN c.topics t " +
           "WHERE (:title IS NULL OR LOWER(CAST(c.title AS string)) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%'))) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (COALESCE(:topicIds) IS NULL OR t.id IN :topicIds) " +
           "AND (COALESCE(:authorIds) IS NULL OR EXISTS (SELECT 1 FROM c.authors a WHERE a.authorId IN :authorIds)) " +
           "AND c.status <> 'DELETED' " +
           "ORDER BY c.createdAt DESC")
    List<Course> searchCourses(
            @Param("title") String title,
            @Param("topicIds") List<UUID> topicIds,
            @Param("authorIds") List<String> authorIds,
            @Param("status") EntityStatus status
    );
}
