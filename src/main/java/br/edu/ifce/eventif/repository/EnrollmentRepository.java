package br.edu.ifce.eventif.repository;

import br.edu.ifce.eventif.entity.Enrollment;
import br.edu.ifce.eventif.entity.EventStatus;
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
    List<Enrollment> findByUserIdAndActivityEventIdAndPresentTrue(Long userId, Long eventId);

    @Query("select e from Enrollment e join fetch e.activity a join fetch a.event event "
            + "where e.user.id = :userId and e.present = true and event.status = :status "
            + "order by event.startDate, event.id, a.id")
    List<Enrollment> findCertificateEnrollments(@Param("userId") Long userId, @Param("status") EventStatus status);
}
