package Fruit.Web.repositories;

import Fruit.Web.models.Product;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
  boolean existsBySlug(String slug);
  Optional<Product> findById(Long id);

  Page<Product> findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(
      String nameKey, String slugKey, Pageable pageable);
      
  long countByActive(Boolean active);
  long countByBaseStockQtyLessThan(Integer threshold);
  
  // ✅ THÊM QUERY NÀY - Filter theo category
  @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN p.categories c
    WHERE (:categoryId IS NULL OR c.id = :categoryId)
    AND (:q IS NULL OR :q = '' OR 
         LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR 
         LOWER(p.slug) LIKE LOWER(CONCAT('%', :q, '%')))
    ORDER BY p.updatedAt DESC
  """)
  Page<Product> searchWithFilter(
      @Param("categoryId") Long categoryId, 
      @Param("q") String query, 
      Pageable pageable);
}