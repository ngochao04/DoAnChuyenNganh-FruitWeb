package Fruit.Web.repositories;

import Fruit.Web.models.Product;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
  boolean existsBySlug(String slug);
  Optional<Product> findById(Long id);

  Page<Product> findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(
      String nameKey, String slugKey, Pageable pageable);
      long countByActive(Boolean active);
      long countByBaseStockQtyLessThan(Integer threshold);
}
