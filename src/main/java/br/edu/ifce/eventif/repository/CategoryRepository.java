package br.edu.ifce.eventif.repository;

import br.edu.ifce.eventif.entity.Category;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findFirstByName(String name);
    List<Category> findAllByOrderByNameAscIdAsc();
}
