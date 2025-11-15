package Fruit.Web.repositories;

import Fruit.Web.models.Order;
import Fruit.Web.models.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.OffsetDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderNo(String orderNo);

    // LIST cho trang admin: tìm theo orderNo / tên người nhận / phone
    @Query("""
           select o
           from Order o
           where (:q is null or :q = ''
                  or lower(coalesce(o.orderNo, ''))       like lower(concat('%', :q, '%'))
                  or lower(coalesce(o.recipientName, '')) like lower(concat('%', :q, '%'))
                  or lower(coalesce(o.phone, ''))         like lower(concat('%', :q, '%'))
           )
           order by o.createdAt desc
           """)
    Page<Order> searchAdmin(@Param("q") String q, Pageable pageable);
    List<Order> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);
    List<Order> findByCreatedAtBetweenAndStatusNot(OffsetDateTime start, OffsetDateTime end, OrderStatus status);
}
