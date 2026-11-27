package br.edu.ifce.eventif.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @OneToMany(mappedBy = "category")
    private List<Event> events = new ArrayList<>();

    protected Category() {}
    public Category(String name) { this.name = name; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Event> getEvents() { return Collections.unmodifiableList(events); }
}
