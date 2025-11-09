package Fruit.Web.models;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "inventory_logs")
public class InventoryLog {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "variant_id")
  private ProductVariant variant;

  @Column(name = "delta") private Integer delta;           // +10, -5, ...
  @Column(name = "note")  private String note;             // Lý do điều chỉnh
  @Column(name = "created_at") private OffsetDateTime createdAt;

  // ==== getters / setters ====
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public ProductVariant getVariant() { return variant; }
  public void setVariant(ProductVariant variant) { this.variant = variant; }

  public Integer getDelta() { return delta; }
  public void setDelta(Integer delta) { this.delta = delta; }

  public String getNote() { return note; }
  public void setNote(String note) { this.note = note; }

  public OffsetDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
