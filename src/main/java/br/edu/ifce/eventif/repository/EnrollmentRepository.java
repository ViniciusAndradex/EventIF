package br.edu.ifce.eventif.repository;

import br.edu.ifce.eventif.entity.Enrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUserIdAndActivityId(Long userId, Long activityId);
    Optional<Enrollment> findByUserIdAndActivityId(Long userId, Long activityId);
    long countByActivityId(Long activityId);
    List<Enrollment> findByActivityId(Long activityId);
    List<Enrollment> findByUserId(Long userId);
    List<Enrollment> findByUserIdAndActivityEventIdAndPresentTrue(Long userId, Long eventId);
}
