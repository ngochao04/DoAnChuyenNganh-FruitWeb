package Fruit.Web.services;

import Fruit.Web.models.Order;
import Fruit.Web.services.dto.CreateOrderRequest; // import đúng package DTO cũ của em
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    // LIST cho trang admin / đơn hàng
    Page<Order> listAdminOrders(String q, Pageable pageable);

    // xem chi tiết 1 đơn
    Order getOrder(Long id);

    // Giữ nguyên logic createOrder(...) cũ
    Order createOrder(CreateOrderRequest req);
}
