package Fruit.Web.controllers.admin;

import Fruit.Web.models.Order;
import Fruit.Web.models.OrderItem;
import Fruit.Web.models.OrderStatus;
import Fruit.Web.models.PaymentMethod;
import Fruit.Web.models.PaymentStatus;
import Fruit.Web.models.ProductVariant;
import Fruit.Web.services.OrderService;
import Fruit.Web.services.dto.CreateOrderRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
public class OrderApiController {

    private final OrderService orderService;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    // =================== LIST ĐƠN HÀNG (TRANG ADMIN) ===================
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> pg = orderService.listAdminOrders(q, pageable);

        // Map từng Order -> Map field đơn giản + label tiếng Việt
        List<Map<String, Object>> content = pg.getContent().stream()
                .map(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", o.getId());
                    m.put("orderNo", o.getOrderNo());
                    m.put("recipientName", o.getRecipientName());
                    m.put("phone", o.getPhone());

                    // Trạng thái đơn
                    m.put("status", enumName(o.getStatus()));
                    m.put("statusLabel", toStatusLabel(o.getStatus()));

                    // Thanh toán
                    m.put("paymentMethod", enumName(o.getPaymentMethod()));
                    m.put("paymentMethodLabel", toPaymentMethodLabel(o.getPaymentMethod()));
                    m.put("paymentStatus", enumName(o.getPaymentStatus()));
                    m.put("paymentStatusLabel", toPaymentStatusLabel(o.getPaymentStatus()));

                    // Tổng tiền
                    m.put("itemsTotal", o.getItemsTotal());
                    m.put("shippingFee", o.getShippingFee());
                    m.put("discountTotal", o.getDiscountTotal());
                    m.put("grandTotal", o.getGrandTotal());

                    // Thời gian
                    m.put("createdAt", o.getCreatedAt());
                    m.put("updatedAt", o.getUpdatedAt());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("pageable", pg.getPageable());
        result.put("size", pg.getSize());
        result.put("number", pg.getNumber());
        result.put("totalElements", pg.getTotalElements());
        result.put("totalPages", pg.getTotalPages());
        result.put("numberOfElements", pg.getNumberOfElements());
        return result;
    }

    // =================== CHI TIẾT 1 ĐƠN (DÙNG CHO POPUP) ===================
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        Order o = orderService.getOrder(id);   // nếu không có sẽ throw

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("orderNo", o.getOrderNo());

        // Khách hàng
        m.put("recipientName", o.getRecipientName());
        m.put("phone", o.getPhone());
        m.put("addressLine1", o.getAddressLine1());
        m.put("ward", o.getWard());
        m.put("district", o.getDistrict());
        m.put("province", o.getProvince());
        m.put("note", o.getNote());

        // Trạng thái đơn
        m.put("status", enumName(o.getStatus()));
        m.put("statusLabel", toStatusLabel(o.getStatus()));

        // Thanh toán
        m.put("paymentMethod", enumName(o.getPaymentMethod()));
        m.put("paymentMethodLabel", toPaymentMethodLabel(o.getPaymentMethod()));
        m.put("paymentStatus", enumName(o.getPaymentStatus()));
        m.put("paymentStatusLabel", toPaymentStatusLabel(o.getPaymentStatus()));

        // Tổng tiền
        m.put("itemsTotal", o.getItemsTotal());
        m.put("shippingFee", o.getShippingFee());
        m.put("discountTotal", o.getDiscountTotal());
        m.put("grandTotal", o.getGrandTotal());

        // Thời gian
        m.put("createdAt", o.getCreatedAt());
        m.put("updatedAt", o.getUpdatedAt());

        // Items trong đơn – chỉ map dữ liệu gọn, tránh lôi cả Product & Variant full tree
        List<Map<String, Object>> items = o.getItems().stream()
                .map(this::mapOrderItem)
                .collect(Collectors.toList());
        m.put("items", items);

        return m;
    }

    private Map<String, Object> mapOrderItem(OrderItem it) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", it.getId());
        m.put("productId", it.getProductId());

        ProductVariant v = it.getVariant();
        m.put("variantId", v != null ? v.getId() : null);

        m.put("titleSnapshot", it.getTitleSnapshot());
        m.put("unit", it.getUnit());
        m.put("quantity", it.getQuantity());
        m.put("unitPrice", it.getUnitPrice());
        m.put("discount", it.getDiscount());
        m.put("lineTotal", it.getLineTotal());
        return m;
    }

    // =================== TẠO ĐƠN MỚI ===================
    @PostMapping
    public Map<String, Object> create(@RequestBody CreateOrderRequest req) {
        Order o = orderService.createOrder(req);
        // Tạo xong trả lại chi tiết đơn (đã map sẵn)
        return get(o.getId());
    }

    // =================== HÀM MAP ENUM -> STRING & LABEL TIẾNG VIỆT ===================

    private String enumName(Enum<?> e) {
        return e != null ? e.name() : null;
    }

    private String toStatusLabel(OrderStatus status) {
        if (status == null) return "Không rõ";
        switch (status) {
            case PENDING:   return "CHỜ XÁC NHẬN";
            case CONFIRMED: return "ĐÃ XÁC NHẬN";
            case SHIPPED:   return "ĐÃ GỬI";
            case COMPLETED: return "HOÀN THÀNH";
            case CANCELLED: return "ĐÃ HỦY";
            default:        return status.name();
        }
    }

    private String toPaymentMethodLabel(PaymentMethod method) {
        if (method == null) return "Không rõ";
        switch (method) {
            case COD:     return "THANH TOÁN KHI NHẬN HÀNG (COD)";
            case VNPAY:   return "VNPAY";
            case MOMO:    return "MOMO";
            case BANKING: return "CHUYỂN KHOẢN NGÂN HÀNG";
            default:      return method.name();
        }
    }

    private String toPaymentStatusLabel(PaymentStatus ps) {
        if (ps == null) return "Không rõ";
        switch (ps) {
            case UNPAID:   return "CHƯA THANH TOÁN";
            case PAID:     return "ĐÃ THANH TOÁN";
            case REFUNDED: return "ĐÃ HOÀN TIỀN";
            default:       return ps.name();
        }
    }
}
