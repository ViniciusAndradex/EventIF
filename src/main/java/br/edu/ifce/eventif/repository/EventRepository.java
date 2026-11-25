package br.edu.ifce.eventif.repository;

import br.edu.ifce.eventif.entity.Event;
import br.edu.ifce.eventif.entity.EventStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatusOrderByStartDateAscIdAsc(EventStatus status, Pageable pageable);
    Page<Event> findByStatusAndCategoryIdOrderByStartDateAscIdAsc(EventStatus status, Long categoryId, Pageable pageable);
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByOrganizerIdAndStatus(Long organizerId, EventStatus status);
    boolean existsByOrganizerIdAndStatus(Long organizerId, EventStatus status);
    Optional<Event> findFirstByTitleAndOrganizerId(String title, Long organizerId);
}
