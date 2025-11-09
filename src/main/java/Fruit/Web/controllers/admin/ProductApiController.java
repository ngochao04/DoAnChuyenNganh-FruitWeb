package Fruit.Web.controllers.admin;

import Fruit.Web.models.Category;
import Fruit.Web.models.Product;
import Fruit.Web.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/products")
public class ProductApiController {
  private final ProductService service;
  public ProductApiController(ProductService service){ this.service = service; }

  // ===== DTO nhận từ FE =====
  public static class ProductPayload {
    public String  name;
    public String  slug;
    public String  shortDesc;
    public String  longDesc;
    public String  brand;
    public String  origin;
    public String  mainImageUrl;
    public Boolean active;

    // GIÁ & TỒN kho gốc (mô hình base-price)
    public BigDecimal basePrice;
    public BigDecimal baseCompareAtPrice;
    public Integer    baseStockQty;

    // Danh mục gửi lên dưới dạng id
    public List<Long> categoryIds;
  }

  // Helper: map DTO -> Entity (chỉ các field cho phép sửa ở form sản phẩm)
  private Product fromPayload(ProductPayload d){
    Product p = new Product();
    p.setName(d.name);
    p.setSlug(d.slug);
    p.setShortDesc(d.shortDesc);
    p.setLongDesc(d.longDesc);
    p.setBrand(d.brand);
    p.setOrigin(d.origin);
    p.setMainImageUrl(d.mainImageUrl);
    p.setActive(d.active);

    // Base price/compare/stock
    p.setBasePrice(d.basePrice);
    p.setBaseCompareAtPrice(d.baseCompareAtPrice);
    if (d.baseStockQty != null) p.setBaseStockQty(d.baseStockQty);

    // Danh mục: biến List<Long> -> Set<Category> (chỉ set id)
    if (d.categoryIds != null) {
      Set<Category> cats = d.categoryIds.stream()
          .filter(Objects::nonNull)
          .map(id -> {
            Category c = new Category();
            c.setId(id);
            return c;
          })
          .collect(Collectors.toSet());
      // Product không có setter categories -> add từng cái
      cats.forEach(p::addCategory);
    }
    return p;
  }

  @GetMapping
  public Page<Map<String,Object>> list(
      @RequestParam(defaultValue="0") int page,
      @RequestParam(defaultValue="10") int size,
      @RequestParam(required=false) String q){
    return service.search(q, PageRequest.of(page, size))
      .map(p -> {
        var m = new LinkedHashMap<String,Object>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("slug", p.getSlug());
        m.put("brand", p.getBrand());
        m.put("origin", p.getOrigin());
        m.put("mainImageUrl", p.getMainImageUrl());

        // Hiển thị giá theo mô hình: nếu có basePrice thì show thẳng; nếu có biến thể thì show min–max
        m.put("basePrice",          p.getBasePrice());
        m.put("priceMin",           p.getPriceMin());
        m.put("priceMax",           p.getPriceMax());
        m.put("stockTotal",         p.getStockTotal());
        m.put("updatedAt",          p.getUpdatedAt());
        m.put("active",             p.getActive());
        return m;
      });
  }

  @GetMapping("/{id}")
public Map<String, Object> get(@PathVariable Long id) {
  Product p = service.get(id);

  Map<String, Object> m = new LinkedHashMap<>();
  m.put("id",            p.getId());
  m.put("name",          p.getName());
  m.put("slug",          p.getSlug());
  m.put("brand",         p.getBrand());
  m.put("origin",        p.getOrigin());
  m.put("shortDesc",     p.getShortDesc());
  m.put("longDesc",      p.getLongDesc());
  m.put("mainImageUrl",  p.getMainImageUrl());
  m.put("active",        p.getActive());

  // Giá & tồn cơ bản
  m.put("basePrice",          p.getBasePrice());
  m.put("baseCompareAtPrice", p.getBaseCompareAtPrice());
  m.put("baseStockQty",       p.getBaseStockQty());

  // Chỉ trả về id danh mục để form preselect
  m.put("categoryIds", p.getCategories()
                        .stream()
                        .map(Category::getId)
                        .toList());

  m.put("updatedAt",    p.getUpdatedAt());
  return m;
}

  @PostMapping
  public Product create(@RequestBody ProductPayload payload){
    Product p = fromPayload(payload);
    return service.create(p);
  }

  @PutMapping("/{id}")
  public Product update(@PathVariable Long id, @RequestBody ProductPayload payload){
    Product p = fromPayload(payload);
    return service.update(id, p);
  }

  @PatchMapping("/{id}/active")
  public void toggle(@PathVariable Long id, @RequestParam boolean v){ service.toggleActive(id, v); }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id){ service.delete(id); }
}
