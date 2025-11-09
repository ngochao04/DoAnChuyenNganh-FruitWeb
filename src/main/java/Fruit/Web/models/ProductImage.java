package Fruit.Web.models;

import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  private String url;

  @Column(name = "sort_order")
  private Integer sortOrder = 0;

  // ===== Getters/Setters =====
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Product getProduct() { return product; }
  public void setProduct(Product product) { this.product = product; }

  public String getUrl() { return url; }
  public void setUrl(String url) { this.url = url; }

  public Integer getSortOrder() { return sortOrder; }
  public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
