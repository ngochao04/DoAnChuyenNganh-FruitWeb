package Fruit.Web.repositories;

import Fruit.Web.models.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE " +
           "(:q IS NULL OR :q = '' OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Promotion> searchPromotions(@Param("q") String query, Pageable pageable);
}