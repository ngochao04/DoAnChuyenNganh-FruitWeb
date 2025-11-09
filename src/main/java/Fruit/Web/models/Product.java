package Fruit.Web.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "products")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String slug;

  @Column(name = "short_desc")
  private String shortDesc;

  @Column(name = "long_desc")
  private String longDesc;

  private String brand;
  private String origin;

  @Column(name = "main_image_url")
  private String mainImageUrl;

  @Column(name = "is_active")
  private Boolean active = Boolean.TRUE;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  // ====== Giá/Tồn cơ bản (không cần biến thể vẫn bán được) ======
  @Column(name = "base_price")
  private BigDecimal basePrice;               // giá bán cơ bản

  @Column(name = "base_compare_at_price")
  private BigDecimal baseCompareAtPrice;      // giá gạch (nếu có)

  @Column(name = "base_stock_qty", nullable = false)
  private Integer baseStockQty = 0;           // tồn kho cơ bản

  // ================== Relationships ==================
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "product_categories",
      joinColumns = @JoinColumn(name = "product_id"),
      inverseJoinColumns = @JoinColumn(name = "category_id")
  )
  private Set<Category> categories = new HashSet<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductVariant> variants = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("sortOrder ASC, id ASC")
  private List<ProductImage> images = new ArrayList<>();

  // ================== Lifecycle ==================
  @PrePersist
  void onCreate() {
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = this.createdAt;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = OffsetDateTime.now();
  }

  // ================== Helpers (giá/tồn hiển thị) ==================
  // Quy ước:
  public BigDecimal getPriceMin() {
    if (variants != null && !variants.isEmpty()) {
      return variants.stream()
          .map(ProductVariant::getPrice)
          .filter(Objects::nonNull)
          .min(Comparator.naturalOrder())
          .orElse(null);
    }
    return basePrice; // không có biến thể -> dùng giá cơ bản
  }

  public BigDecimal getPriceMax() {
    if (variants != null && !variants.isEmpty()) {
      return variants.stream()
          .map(ProductVariant::getPrice)
          .filter(Objects::nonNull)
          .max(Comparator.naturalOrder())
          .orElse(null);
    }
    // không có biến thể -> nếu có giá so sánh thì trả về, ngược lại dùng basePrice
    return (baseCompareAtPrice != null) ? baseCompareAtPrice : basePrice;
  }

 public Integer getStockTotal() {
  if (variants != null && !variants.isEmpty()) {
    return variants.stream()
        .map(v -> v.getStockQty() == null ? 0 : v.getStockQty())
        .reduce(0, Integer::sum);
  }
  return baseStockQty == null ? 0 : baseStockQty;
}

  // ================== Getters ==================
  public Long getId() { return id; }
  public String getName() { return name; }
  public String getSlug() { return slug; }
  public String getShortDesc() { return shortDesc; }
  public String getLongDesc() { return longDesc; }
  public String getBrand() { return brand; }
  public String getOrigin() { return origin; }
  public String getMainImageUrl() { return mainImageUrl; }
  public Boolean getActive() { return active; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  public BigDecimal getBasePrice() { return basePrice; }
  public BigDecimal getBaseCompareAtPrice() { return baseCompareAtPrice; }
  public Integer getBaseStockQty() { return baseStockQty; }

  public Set<Category> getCategories() { return categories; }
  public List<ProductVariant> getVariants() { return variants; }
  public List<ProductImage> getImages() { return images; }

  // ================== Setters ==================
  public void setName(String name) { this.name = name; }
  public void setSlug(String slug) { this.slug = slug; }
  public void setShortDesc(String shortDesc) { this.shortDesc = shortDesc; }
  public void setLongDesc(String longDesc) { this.longDesc = longDesc; }
  public void setBrand(String brand) { this.brand = brand; }
  public void setOrigin(String origin) { this.origin = origin; }
  public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
  public void setActive(Boolean active) { this.active = active; }

  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
  public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

  public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
  public void setBaseCompareAtPrice(BigDecimal baseCompareAtPrice) { this.baseCompareAtPrice = baseCompareAtPrice; }
  public void setBaseStockQty(Integer baseStockQty) { this.baseStockQty = baseStockQty; }

  // ================== Add/Remove helpers ==================
  public void addCategory(Category c) {
    if (c != null) this.categories.add(c);
  }
  public void removeCategory(Category c) {
    if (c != null) this.categories.remove(c);
  }

  public void addVariant(ProductVariant v) {
    if (v != null) { this.variants.add(v); v.setProduct(this); }
  }
  public void removeVariant(ProductVariant v) {
    if (v != null) { this.variants.remove(v); v.setProduct(null); }
  }

  public void addImage(ProductImage i) {
    if (i != null) { this.images.add(i); i.setProduct(this); }
  }
  public void removeImage(ProductImage i) {
    if (i != null) { this.images.remove(i); i.setProduct(null); }
  }
}
