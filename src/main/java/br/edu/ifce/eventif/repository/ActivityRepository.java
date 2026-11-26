package br.edu.ifce.eventif.repository;

import br.edu.ifce.eventif.entity.Activity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    @org.springframework.data.jpa.repository.Query("select a.event.id from Activity a where a.id = :id")
    Optional<Long> findEventIdByActivityId(@org.springframework.data.repository.query.Param("id") Long id);
    List<Activity> findByEventId(Long eventId);
    long countByEventId(Long eventId);
    Optional<Activity> findFirstByTitleAndEventId(String title, Long eventId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from Activity a where a.id = :id")
    Optional<Activity> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
}
