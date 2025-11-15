package Fruit.Web.services;

import Fruit.Web.models.Coupon;
import Fruit.Web.repositories.CouponRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public Page<Coupon> search(String query, Pageable pageable) {
        return couponRepository.searchCoupons(query, pageable);
    }

    @Override
    public Coupon get(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));
    }

    @Override
    public Coupon create(Coupon coupon) {
        // Check if code already exists
        if (coupon.getCode() != null && couponRepository.findByCode(coupon.getCode()).isPresent()) {
            throw new RuntimeException("Mã giảm giá đã tồn tại: " + coupon.getCode());
        }
        return couponRepository.save(coupon);
    }

    @Override
    public Coupon update(Long id, Coupon coupon) {
        Coupon existing = get(id);
        
        // Check if code changed and new code exists
        if (!existing.getCode().equals(coupon.getCode())) {
            if (couponRepository.findByCode(coupon.getCode()).isPresent()) {
                throw new RuntimeException("Mã giảm giá đã tồn tại: " + coupon.getCode());
            }
        }
        
        existing.setCode(coupon.getCode());
        existing.setDescription(coupon.getDescription());
        existing.setStartsAt(coupon.getStartsAt());
        existing.setEndsAt(coupon.getEndsAt());
        existing.setMaxRedemptions(coupon.getMaxRedemptions());
        existing.setAmountOff(coupon.getAmountOff());
        existing.setPercentOff(coupon.getPercentOff());
        existing.setMinOrderAmount(coupon.getMinOrderAmount());
        existing.setIsActive(coupon.getIsActive());
        
        return couponRepository.save(existing);
    }

    @Override
    public void toggleActive(Long id, Boolean isActive) {
        Coupon coupon = get(id);
        coupon.setIsActive(isActive);
        couponRepository.save(coupon);
    }

    @Override
    public void delete(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
    }
}