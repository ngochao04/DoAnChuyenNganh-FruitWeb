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
    @Query("""
           select 
             v.id             as id,
             p.id             as productId,
             p.name           as productName,
             v.sku            as sku,
             v.optionName     as optionName,
             v.unit           as unit,
             v.price          as price,
             v.compareAtPrice as compareAtPrice,
             v.weightKg       as weightKg,
             v.inStock        as inStock,
             v.stockQty       as stockQty,
             v.updatedAt      as updatedAt
           from ProductVariant v
           join v.product p
           where (:pid is null or p.id = :pid)
             and (
                  :kw is null
                  or lower(coalesce(v.optionName, '')) like :kw
                  or lower(coalesce(v.sku,        '')) like :kw
                  or lower(coalesce(p.name,       '')) like :kw
             )
           """)
    Page<VariantRow> searchAllRows(@Param("pid") Long productId,
                                   @Param("kw")  String keywordPattern,
                                   Pageable pageable);
                                   
    Optional<ProductVariant> findById(Long id);
}
