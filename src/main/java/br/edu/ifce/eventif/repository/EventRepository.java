package br.edu.ifce.eventif.repository;

import br.edu.ifce.eventif.entity.Event;
import br.edu.ifce.eventif.entity.EventStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByStatusOrderByStartDateAscIdAsc(EventStatus status, Pageable pageable);
    Page<Event> findByStatusAndCategoryIdOrderByStartDateAscIdAsc(EventStatus status, Long categoryId, Pageable pageable);
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByOrganizerIdAndStatus(Long organizerId, EventStatus status);
    boolean existsByOrganizerIdAndStatus(Long organizerId, EventStatus status);
    boolean existsByCategoryId(Long categoryId);
    Optional<Event> findFirstByTitleAndOrganizerId(String title, Long organizerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
