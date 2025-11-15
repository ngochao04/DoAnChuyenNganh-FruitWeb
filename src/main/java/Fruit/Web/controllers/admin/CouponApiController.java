package Fruit.Web.controllers.admin;

import Fruit.Web.models.Coupon;
import Fruit.Web.services.CouponService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupons")
public class CouponApiController {

    private final CouponService service;

    public CouponApiController(CouponService service) {
        this.service = service;
    }

    public static class CouponPayload {
        public String code;
        public String description;
        public LocalDateTime startsAt;
        public LocalDateTime endsAt;
        public Integer maxRedemptions;
        public BigDecimal amountOff;
        public BigDecimal percentOff;
        public BigDecimal minOrderAmount;
        public Boolean isActive;
    }

    private Coupon fromPayload(CouponPayload payload) {
        Coupon c = new Coupon();
        c.setCode(payload.code != null ? payload.code.toUpperCase().trim() : null);
        c.setDescription(payload.description);
        c.setStartsAt(payload.startsAt);
        c.setEndsAt(payload.endsAt);
        c.setMaxRedemptions(payload.maxRedemptions);
        c.setAmountOff(payload.amountOff);
        c.setPercentOff(payload.percentOff);
        c.setMinOrderAmount(payload.minOrderAmount);
        c.setIsActive(payload.isActive != null ? payload.isActive : true);
        return c;
    }

    @GetMapping
    public Page<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q) {
        return service.search(q, PageRequest.of(page, size))
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId());
                    m.put("code", c.getCode());
                    m.put("description", c.getDescription());
                    m.put("startsAt", c.getStartsAt());
                    m.put("endsAt", c.getEndsAt());
                    m.put("maxRedemptions", c.getMaxRedemptions());
                    m.put("redemptions", c.getRedemptions());
                    m.put("amountOff", c.getAmountOff());
                    m.put("percentOff", c.getPercentOff());
                    m.put("minOrderAmount", c.getMinOrderAmount());
                    m.put("isActive", c.getIsActive());
                    m.put("createdAt", c.getCreatedAt());
                    m.put("updatedAt", c.getUpdatedAt());
                    return m;
                });
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Coupon c = service.get(id);
        
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("code", c.getCode());
        m.put("description", c.getDescription());
        m.put("startsAt", c.getStartsAt());
        m.put("endsAt", c.getEndsAt());
        m.put("maxRedemptions", c.getMaxRedemptions());
        m.put("redemptions", c.getRedemptions());
        m.put("amountOff", c.getAmountOff());
        m.put("percentOff", c.getPercentOff());
        m.put("minOrderAmount", c.getMinOrderAmount());
        m.put("isActive", c.getIsActive());
        m.put("createdAt", c.getCreatedAt());
        m.put("updatedAt", c.getUpdatedAt());
        return m;
    }

    @PostMapping
    public Coupon create(@RequestBody CouponPayload payload) {
        Coupon c = fromPayload(payload);
        return service.create(c);
    }

    @PutMapping("/{id}")
    public Coupon update(@PathVariable Long id, @RequestBody CouponPayload payload) {
        Coupon c = fromPayload(payload);
        return service.update(id, c);
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