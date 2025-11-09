package Fruit.Web.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "product_variants")
public class ProductVariant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  private String sku;

  @Column(name = "option_name")
  private String optionName;       // ví dụ: 500g, 1kg

  private String unit;             // ví dụ: gói, kg, hộp…

  private BigDecimal price;

  @Column(name = "compare_at_price")
  private BigDecimal compareAtPrice;

  @Column(name = "weight_kg")
  private BigDecimal weightKg;

  @Column(name = "in_stock")
  private Boolean inStock = Boolean.TRUE;

  @Column(name = "stock_qty")
  private Integer stockQty;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  // --- Auto timestamps (tùy chọn) ---
  @PrePersist
  @PreUpdate
  void touchUpdatedAt() {
    this.updatedAt = OffsetDateTime.now();
  }

  // ===== Getters/Setters =====
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Product getProduct() { return product; }
  public void setProduct(Product product) { this.product = product; }

  public String getSku() { return sku; }
  public void setSku(String sku) { this.sku = sku; }

  public String getOptionName() { return optionName; }
  public void setOptionName(String optionName) { this.optionName = optionName; }

  public String getUnit() { return unit; }
  public void setUnit(String unit) { this.unit = unit; }

  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal price) { this.price = price; }

  public BigDecimal getCompareAtPrice() { return compareAtPrice; }
  public void setCompareAtPrice(BigDecimal compareAtPrice) { this.compareAtPrice = compareAtPrice; }

  public BigDecimal getWeightKg() { return weightKg; }
  public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

  public Boolean getInStock() { return inStock; }
  public void setInStock(Boolean inStock) { this.inStock = inStock; }

  public Integer getStockQty() { return stockQty; }
  public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }

  public OffsetDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
