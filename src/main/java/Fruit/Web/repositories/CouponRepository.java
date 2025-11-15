package Fruit.Web.repositories;

import Fruit.Web.models.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    @Query("SELECT c FROM Coupon c WHERE " +
           "(:q IS NULL OR :q = '' OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Coupon> searchCoupons(@Param("q") String query, Pageable pageable);
}