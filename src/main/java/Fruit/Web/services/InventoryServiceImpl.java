package Fruit.Web.services;

import Fruit.Web.models.InventoryLog;
import Fruit.Web.models.Product;
import Fruit.Web.models.ProductVariant;
import Fruit.Web.repositories.InventoryLogRepository;
import Fruit.Web.repositories.ProductRepository;
import Fruit.Web.repositories.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

@Service
public class InventoryServiceImpl implements InventoryService {

  private final ProductRepository productRepo;
  private final ProductVariantRepository variantRepo;
  private final InventoryLogRepository logRepo;

  public InventoryServiceImpl(ProductRepository pr, ProductVariantRepository vr, InventoryLogRepository lr) {
    this.productRepo = pr;
    this.variantRepo = vr;
    this.logRepo = lr;
  }

 @Override
public Page<ProductVariant> listVariants(Long productId, String q, Pageable pageable) {
    if (StringUtils.hasText(q)) {
        // ✨ gọi method mới đúng tên
        return variantRepo.searchByProductAndKeyword(productId, q, pageable);
    }
    return variantRepo.findByProduct_Id(productId, pageable);
}

  @Override
  public ProductVariant getVariant(Long id) {
    return variantRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Variant not found"));
  }

  @Override
  @Transactional
public ProductVariant createVariant(Long productId, ProductVariant in) {
    Product p = productRepo.findById(productId).orElseThrow();

    // (tuỳ chọn) chống trùng SKU trong cùng 1 sản phẩm
    if (StringUtils.hasText(in.getSku())
        && variantRepo.existsByProduct_IdAndSkuIgnoreCase(productId, in.getSku())) {
      throw new IllegalArgumentException("SKU đã tồn tại trong sản phẩm này");
    }

    in.setProduct(p);
    // nhận giá trị client nếu có; mặc định nếu null
    if (in.getInStock() == null) in.setInStock(Boolean.TRUE);
    if (in.getStockQty() == null) in.setStockQty(0);

    in.setUpdatedAt(OffsetDateTime.now());
    return variantRepo.save(in);
}

  @Override
  @Transactional
  public ProductVariant updateVariant(Long id, ProductVariant in) {
    ProductVariant v = getVariant(id);
    v.setSku(in.getSku());
    v.setOptionName(in.getOptionName());
    v.setUnit(in.getUnit());
    v.setPrice(in.getPrice());
    v.setCompareAtPrice(in.getCompareAtPrice());
    v.setWeightKg(in.getWeightKg());
    if (in.getInStock() != null) v.setInStock(in.getInStock());
    v.setUpdatedAt(OffsetDateTime.now());
    return variantRepo.save(v);
  }

  @Override
  public void deleteVariant(Long id) { variantRepo.deleteById(id); }

  @Override
  @Transactional
  public ProductVariant adjustStock(Long variantId, int delta, String note) {
    ProductVariant v = getVariant(variantId);
    int current = v.getStockQty() == null ? 0 : v.getStockQty();
    int updated = current + delta;
    if (updated < 0) updated = 0;
    v.setStockQty(updated);
    v.setUpdatedAt(OffsetDateTime.now());
    variantRepo.save(v);

    InventoryLog log = new InventoryLog();
    log.setVariant(v);
    log.setDelta(delta);
    log.setNote(note);
    log.setCreatedAt(OffsetDateTime.now());
    logRepo.save(log);
    return v;
  }

  @Override
  @Transactional
  public ProductVariant setInStock(Long variantId, boolean value) {
    ProductVariant v = getVariant(variantId);
    v.setInStock(value);
    v.setUpdatedAt(OffsetDateTime.now());
    return variantRepo.save(v);
  }

  @Override
  public Page<InventoryLog> logs(Long variantId, Pageable pageable) {
    return logRepo.findByVariant(getVariant(variantId), pageable);
  }
}
