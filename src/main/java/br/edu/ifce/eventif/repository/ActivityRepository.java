package br.edu.ifce.eventif.repository;

import br.edu.ifce.eventif.entity.Activity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByEventId(Long eventId);
    long countByEventId(Long eventId);
    Optional<Activity> findFirstByTitleAndEventId(String title, Long eventId);
}
