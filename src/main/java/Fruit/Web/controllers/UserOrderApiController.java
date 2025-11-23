package Fruit.Web.controllers;

import Fruit.Web.models.Order;
import Fruit.Web.models.OrderItem;
import Fruit.Web.models.OrderStatus;
import Fruit.Web.models.PaymentMethod;
import Fruit.Web.models.PaymentStatus;
import Fruit.Web.models.ProductVariant;
import Fruit.Web.repositories.OrderRepository;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class UserOrderApiController {

    private final OrderRepository orderRepository;

    public UserOrderApiController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * ✅ LẤY DANH SÁCH ĐƠN HÀNG CỦA USER
     */
    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return orders.stream()
                .map(this::mapOrderSummary)
                .collect(Collectors.toList());
    }

    /**
     * ✅ LẤY CHI TIẾT 1 ĐƠN HÀNG
     */
    @GetMapping("/{id}")
    public Map<String, Object> getOrderDetail(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        return mapOrderDetail(order);
    }

    /**
     * Map Order -> Summary (cho danh sách)
     */
    private Map<String, Object> mapOrderSummary(Order o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("orderNo", o.getOrderNo());
        m.put("status", o.getStatus() != null ? o.getStatus().name() : "PENDING");
        m.put("statusLabel", toStatusLabel(o.getStatus()));
        m.put("grandTotal", o.getGrandTotal());
        m.put("itemCount", o.getItems() != null ? o.getItems().size() : 0);
        m.put("createdAt", o.getCreatedAt());
        
        // Payment info
        m.put("paymentMethod", o.getPaymentMethod() != null ? o.getPaymentMethod().name() : null);
        m.put("paymentStatus", o.getPaymentStatus() != null ? o.getPaymentStatus().name() : "UNPAID");
        
        return m;
    }

    /**
     * Map Order -> Detail (đầy đủ thông tin)
     */
    private Map<String, Object> mapOrderDetail(Order o) {
        Map<String, Object> m = new LinkedHashMap<>();
        
        // Thông tin cơ bản
        m.put("id", o.getId());
        m.put("orderNo", o.getOrderNo());
        m.put("status", o.getStatus() != null ? o.getStatus().name() : "PENDING");
        m.put("statusLabel", toStatusLabel(o.getStatus()));
        
        // Thông tin giao hàng
        m.put("recipientName", o.getRecipientName());
        m.put("phone", o.getPhone());
        m.put("addressLine1", o.getAddressLine1());
        m.put("ward", o.getWard());
        m.put("district", o.getDistrict());
        m.put("province", o.getProvince());
        m.put("note", o.getNote());
        
        // Thanh toán
        m.put("paymentMethod", o.getPaymentMethod() != null ? o.getPaymentMethod().name() : null);
        m.put("paymentMethodLabel", toPaymentMethodLabel(o.getPaymentMethod()));
        m.put("paymentStatus", o.getPaymentStatus() != null ? o.getPaymentStatus().name() : "UNPAID");
        m.put("paymentStatusLabel", toPaymentStatusLabel(o.getPaymentStatus()));
        
        // Tiền
        m.put("itemsTotal", o.getItemsTotal());
        m.put("shippingFee", o.getShippingFee());
        m.put("discountTotal", o.getDiscountTotal());
        m.put("grandTotal", o.getGrandTotal());
        
        // Thời gian
        m.put("createdAt", o.getCreatedAt());
        m.put("updatedAt", o.getUpdatedAt());
        
        // Items
        List<Map<String, Object>> items = o.getItems().stream()
                .map(this::mapOrderItem)
                .collect(Collectors.toList());
        m.put("items", items);
        
        return m;
    }

    /**
     * Map OrderItem
     */
    private Map<String, Object> mapOrderItem(OrderItem item) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", item.getId());
        m.put("productId", item.getProductId());
        
        ProductVariant variant = item.getVariant();
        m.put("variantId", variant != null ? variant.getId() : null);
        
        m.put("titleSnapshot", item.getTitleSnapshot());
        m.put("unit", item.getUnit());
        m.put("quantity", item.getQuantity());
        m.put("unitPrice", item.getUnitPrice());
        m.put("discount", item.getDiscount());
        m.put("lineTotal", item.getLineTotal());
        
        return m;
    }

    // ===== Helper methods =====
    
    private String toStatusLabel(OrderStatus status) {
        if (status == null) return "Chờ xác nhận";
        switch (status) {
            case PENDING:   return "Chờ xác nhận";
            case CONFIRMED: return "Đã xác nhận";
            case SHIPPED:   return "Đang giao";
            case COMPLETED: return "Hoàn thành";
            case CANCELLED: return "Đã hủy";
            default:        return status.name();
        }
    }

    private String toPaymentMethodLabel(PaymentMethod method) {
        if (method == null) return "-";
        switch (method) {
            case COD:     return "COD";
            case VNPAY:   return "VNPay";
            case MOMO:    return "MoMo";
            case BANKING: return "Chuyển khoản";
            default:      return method.name();
        }
    }

    private String toPaymentStatusLabel(PaymentStatus status) {
        if (status == null) return "Chưa thanh toán";
        switch (status) {
            case UNPAID:   return "Chưa thanh toán";
            case PAID:     return "Đã thanh toán";
            case REFUNDED: return "Đã hoàn tiền";
            default:       return status.name();
        }
    }
    /**
 * ✅ HỦY ĐƠN HÀNG (USER)
 */
@PatchMapping("/{id}/cancel")
public Map<String, Object> cancelOrder(@PathVariable Long id) {
    Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    
    // Kiểm tra trạng thái
    if (order.getStatus() != OrderStatus.PENDING && 
        order.getStatus() != OrderStatus.CONFIRMED) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("success", false);
        error.put("message", "Không thể hủy đơn hàng đã " + toStatusLabel(order.getStatus()).toLowerCase());
        return error;
    }
    
    // Cập nhật trạng thái
    order.setStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);
    
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("success", true);
    result.put("message", "Đã hủy đơn hàng thành công");
    result.put("order", mapOrderDetail(order));
    
    return result;
}
}