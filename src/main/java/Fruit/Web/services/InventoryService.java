package Fruit.Web.services;

import Fruit.Web.models.InventoryLog;
import Fruit.Web.models.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
  Page<ProductVariant> listVariants(Long productId, String q, Pageable pageable);
  ProductVariant getVariant(Long id);
  ProductVariant createVariant(Long productId, ProductVariant in);
  ProductVariant updateVariant(Long id, ProductVariant in);
  void deleteVariant(Long id);

  // tồn kho
  ProductVariant adjustStock(Long variantId, int delta, String note);
  ProductVariant setInStock(Long variantId, boolean value);

  Page<InventoryLog> logs(Long variantId, Pageable pageable);
}
