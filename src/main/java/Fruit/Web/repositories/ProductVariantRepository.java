package Fruit.Web.repositories;

import Fruit.Web.models.ProductVariant;
import Fruit.Web.projections.VariantRow;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    // ========= dùng cho trang biến thể của 1 sản phẩm =========

    Page<ProductVariant> findByProduct_Id(Long productId, Pageable pageable);

    @Query("""
           select v
           from ProductVariant v
           where v.product.id = :pid
             and (
                :q is null
                or lower(coalesce(v.optionName, '')) like lower(concat('%', :q, '%'))
                or lower(coalesce(v.sku,        '')) like lower(concat('%', :q, '%'))
             )
           """)
    Page<ProductVariant> searchByProductAndKeyword(@Param("pid") Long productId,
                                                   @Param("q")   String keyword,
                                                   Pageable pageable);

    boolean existsByProduct_IdAndSkuIgnoreCase(Long productId, String sku);

    // ========= dùng cho trang KHO (join luôn product, có filter productId, q) =========
    // ✅ FIX: Hiển thị cả sản phẩm không có variant
    @Query("""
           select 
             COALESCE(v.id, -p.id)         as id,
             p.id                          as productId,
             p.name                        as productName,
             COALESCE(v.sku, 'BASE')       as sku,
             COALESCE(v.optionName, 'Sản phẩm gốc') as optionName,
             COALESCE(v.unit, '')          as unit,
             COALESCE(v.price, p.basePrice) as price,
             COALESCE(v.compareAtPrice, p.baseCompareAtPrice) as compareAtPrice,
             v.weightKg                    as weightKg,
             COALESCE(v.inStock, p.active) as inStock,
             COALESCE(v.stockQty, p.baseStockQty) as stockQty,
             COALESCE(v.updatedAt, p.updatedAt) as updatedAt
           from Product p
           left join p.variants v
           where (:pid is null or p.id = :pid)
             and (
                  :kw is null
                  or lower(coalesce(v.optionName, '')) like :kw
                  or lower(coalesce(v.sku,        '')) like :kw
                  or lower(coalesce(p.name,       '')) like :kw
             )
             and (v.id is not null or SIZE(p.variants) = 0)
           """)
    Page<VariantRow> searchAllRows(@Param("pid") Long productId,
                                   @Param("kw")  String keywordPattern,
                                   Pageable pageable);
                                   
    Optional<ProductVariant> findById(Long id);
}