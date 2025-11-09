package Fruit.Web.repositories;

import Fruit.Web.models.InventoryLog;
import Fruit.Web.models.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
  Page<InventoryLog> findByVariant(ProductVariant variant, Pageable pageable);
}
