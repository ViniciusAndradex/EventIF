package br.edu.ifce.eventif.repository;

import br.edu.ifce.eventif.entity.Enrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUserIdAndActivityId(Long userId, Long activityId);

    Optional<Enrollment> findByUserIdAndActivityId(Long userId, Long activityId);

    long countByActivityId(Long activityId);

    List<Enrollment> findByActivityIdOrderByUserNameAscIdAsc(Long activityId);

    List<Enrollment> findByUserIdOrderByActivityStartDateTimeAscIdAsc(Long userId);

    @Query("select e from Enrollment e join fetch e.activity a join fetch a.event event "
            + "where e.user.id = :userId "
            + "order by event.startDate, event.id, a.id")
    List<Enrollment> findByUserIdWithActivityAndEvent(@Param("userId") Long userId);
}
