package Fruit.Web.controllers.admin;

import Fruit.Web.models.InventoryLog;
import Fruit.Web.models.ProductVariant;
import Fruit.Web.services.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class VariantApiController {
  private final InventoryService inv;

  public VariantApiController(InventoryService inv) { this.inv = inv; }

  // === List biến thể theo productId (TRẢ VỀ LIST để khớp JS) ===
  @GetMapping("/products/{pid}/variants")
  public List<Map<String,Object>> list(@PathVariable Long pid,
                                       @RequestParam(required = false) String q) {
    // có thể giới hạn size lớn một chút, hoặc bạn tự viết findAll(pid)
    return inv.listVariants(pid, q, PageRequest.of(0, 1000))
              .getContent()
              .stream()
              .map(v -> {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("id", v.getId());
                m.put("sku", v.getSku());
                m.put("optionName", v.getOptionName());
                m.put("unit", v.getUnit());
                m.put("price", v.getPrice());
                m.put("compareAtPrice", v.getCompareAtPrice());
                m.put("weightKg", v.getWeightKg());
                m.put("inStock", v.getInStock());
                m.put("stockQty", v.getStockQty());
                m.put("updatedAt", v.getUpdatedAt());
                return m;
              })
              .collect(Collectors.toList());
  }

  @GetMapping("/variants/{id}")
  public ProductVariant get(@PathVariable Long id){ return inv.getVariant(id); }

  @PostMapping("/products/{pid}/variants")
  public ProductVariant create(@PathVariable Long pid, @RequestBody ProductVariant in) {
    return inv.createVariant(pid, in);
  }

  @PutMapping("/variants/{id}")
  public ProductVariant update(@PathVariable Long id, @RequestBody ProductVariant in){
    return inv.updateVariant(id, in);
  }

  @DeleteMapping("/variants/{id}")
  public void delete(@PathVariable Long id){ inv.deleteVariant(id); }

  // === Tồn kho (khớp JS: PATCH) ===
  @PatchMapping("/variants/{id}/stock")
  public ProductVariant adjust(@PathVariable Long id,
                               @RequestParam int delta,
                               @RequestParam(required=false) String note){
    return inv.adjustStock(id, delta, note);
  }

  @PatchMapping("/variants/{id}/in-stock")
  public ProductVariant toggle(@PathVariable Long id, @RequestParam("v") boolean value){
    return inv.setInStock(id, value);
  }

  @GetMapping("/variants/{id}/logs")
  public Page<InventoryLog> logs(@PathVariable Long id,
                                 @RequestParam(defaultValue="0") int page,
                                 @RequestParam(defaultValue="10") int size){
    return inv.logs(id, PageRequest.of(page,size));
  }
}
