package Fruit.Web.repositories;

import Fruit.Web.models.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

  // Tìm tất cả biến thể theo productId (không lọc)
  Page<ProductVariant> findByProduct_Id(Long productId, Pageable pageable);

  // Tìm kiếm có nhóm điều kiện đúng: productId = :pid AND (optionName LIKE :q OR sku LIKE :q)
  @Query("""
         select v
         from ProductVariant v
         where v.product.id = :pid
           and ( lower(coalesce(v.optionName, '')) like lower(concat('%', :q, '%'))
              or lower(coalesce(v.sku, ''))        like lower(concat('%', :q, '%')) )
         """)
  Page<ProductVariant> searchByProductAndKeyword(@Param("pid") Long productId,
                                                 @Param("q")   String keyword,
                                                 Pageable pageable);

  // (Tuỳ chọn) kiểm tra trùng SKU trong cùng 1 sản phẩm
  boolean existsByProduct_IdAndSkuIgnoreCase(Long productId, String sku);
}
