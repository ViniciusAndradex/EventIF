package br.edu.ifce.eventif.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.Check;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "activities")
@Check(constraints = "capacity > 0")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;
    @Column(nullable = false)
    private String instructor;
    @Column(nullable = false)
    private LocalDateTime startDateTime;
    @Column(nullable = false)
    private LocalDateTime endDateTime;
    @Positive
    @Column(nullable = false)
    private Integer workloadMinutes;
    @Positive
    @Column(nullable = false)
    private Integer capacity;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @OneToMany(mappedBy = "activity")
    private List<Enrollment> enrollments = new ArrayList<>();

    protected Activity() {
    }

    public Activity(String title, ActivityType type, String instructor, LocalDateTime startDateTime,
            LocalDateTime endDateTime, Integer workloadMinutes, Integer capacity, Event event) {
        this.title = title;
        this.type = type;
        this.instructor = instructor;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.workloadMinutes = workloadMinutes;
        this.capacity = capacity;
        this.event = event;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Integer getWorkloadMinutes() {
        return workloadMinutes;
    }

    public void setWorkloadMinutes(Integer workloadMinutes) {
        this.workloadMinutes = workloadMinutes;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<Enrollment> getEnrollments() {
        return Collections.unmodifiableList(enrollments);
    }
}
