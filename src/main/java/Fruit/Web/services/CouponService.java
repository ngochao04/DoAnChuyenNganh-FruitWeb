package Fruit.Web.services;

import Fruit.Web.models.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponService {

    Page<Coupon> search(String query, Pageable pageable);

    Coupon get(Long id);

    Coupon create(Coupon coupon);

    Coupon update(Long id, Coupon coupon);

    void toggleActive(Long id, Boolean isActive);

    void delete(Long id);
}