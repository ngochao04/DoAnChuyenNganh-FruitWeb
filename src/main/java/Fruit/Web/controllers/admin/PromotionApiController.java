package Fruit.Web.controllers.admin;

import Fruit.Web.models.Promotion;
import Fruit.Web.services.PromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/promotions")
public class PromotionApiController {

    private final PromotionService service;

    public PromotionApiController(PromotionService service) {
        this.service = service;
    }

    // DTO for create/update
    public static class PromotionPayload {
        public String name;
        public String description;
        public LocalDateTime startsAt;
        public LocalDateTime endsAt;
        public BigDecimal minOrderAmount;
        public BigDecimal shippingDiscount;
        public BigDecimal amountOff;
        public BigDecimal percentOff;
        public Boolean isActive;
    }

    private Promotion fromPayload(PromotionPayload payload) {
        Promotion p = new Promotion();
        p.setName(payload.name);
        p.setDescription(payload.description);
        p.setStartsAt(payload.startsAt);
        p.setEndsAt(payload.endsAt);
        p.setMinOrderAmount(payload.minOrderAmount);
        p.setShippingDiscount(payload.shippingDiscount);
        p.setAmountOff(payload.amountOff);
        p.setPercentOff(payload.percentOff);
        p.setIsActive(payload.isActive != null ? payload.isActive : true);
        return p;
    }

    @GetMapping
    public Page<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q) {
        return service.search(q, PageRequest.of(page, size))
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("description", p.getDescription());
                    m.put("startsAt", p.getStartsAt());
                    m.put("endsAt", p.getEndsAt());
                    m.put("minOrderAmount", p.getMinOrderAmount());
                    m.put("shippingDiscount", p.getShippingDiscount());
                    m.put("amountOff", p.getAmountOff());
                    m.put("percentOff", p.getPercentOff());
                    m.put("isActive", p.getIsActive());
                    m.put("createdAt", p.getCreatedAt());
                    m.put("updatedAt", p.getUpdatedAt());
                    return m;
                });
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Promotion p = service.get(id);
        
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("description", p.getDescription());
        m.put("startsAt", p.getStartsAt());
        m.put("endsAt", p.getEndsAt());
        m.put("minOrderAmount", p.getMinOrderAmount());
        m.put("shippingDiscount", p.getShippingDiscount());
        m.put("amountOff", p.getAmountOff());
        m.put("percentOff", p.getPercentOff());
        m.put("isActive", p.getIsActive());
        m.put("createdAt", p.getCreatedAt());
        m.put("updatedAt", p.getUpdatedAt());
        return m;
    }

    @PostMapping
    public Promotion create(@RequestBody PromotionPayload payload) {
        Promotion p = fromPayload(payload);
        return service.create(p);
    }

    @PutMapping("/{id}")
    public Promotion update(@PathVariable Long id, @RequestBody PromotionPayload payload) {
        Promotion p = fromPayload(payload);
        return service.update(id, p);
    }

    @PatchMapping("/{id}/active")
    public void toggle(@PathVariable Long id, @RequestParam boolean v) {
        service.toggleActive(id, v);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}