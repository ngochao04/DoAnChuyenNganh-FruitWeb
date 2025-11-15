package Fruit.Web.controllers.admin;

import Fruit.Web.repositories.OrderRepository;
import Fruit.Web.repositories.UserRepository;
import Fruit.Web.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardApiController {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;

    // API lấy thống kê tổng quan
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Tính toán thời gian 7 ngày trước
        OffsetDateTime sevenDaysAgoOffset = OffsetDateTime.now().minusDays(7);
        LocalDateTime sevenDaysAgoLocal = LocalDateTime.now().minusDays(7);
        
        // Tổng số sản phẩm
        long totalProducts = productRepository.count();
        stats.put("totalProducts", totalProducts);
        
        // Đơn hàng gần đây trong 7 ngày (Order dùng OffsetDateTime)
        long recentOrders = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(sevenDaysAgoOffset))
                .count();
        stats.put("recentOrders", recentOrders);
        
        // Khách hàng mới trong 7 ngày (User dùng LocalDateTime)
        long newCustomers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(sevenDaysAgoLocal))
                .count();
        stats.put("newCustomers", newCustomers);
        
        return stats;
    }

    // API lấy dữ liệu biểu đồ doanh thu
    @GetMapping("/revenue-chart")
    public Map<String, Object> getRevenueChart(@RequestParam(defaultValue = "7") int days) {
        LocalDate today = LocalDate.now();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.toString());
            
            // Tạo khoảng thời gian cho ngày đó (dùng OffsetDateTime)
            OffsetDateTime startOfDay = date.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            OffsetDateTime endOfDay = date.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            
            // Tính tổng doanh thu trong ngày
            BigDecimal dailyRevenue = orderRepository.findAll().stream()
                    .filter(o -> o.getCreatedAt() != null)
                    .filter(o -> !o.getCreatedAt().isBefore(startOfDay) && o.getCreatedAt().isBefore(endOfDay))
                    .map(o -> o.getGrandTotal())
                    .filter(total -> total != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            revenues.add(dailyRevenue);
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("dates", dates);
        chartData.put("revenues", revenues);
        
        return chartData;
    }

    // API lấy đơn hàng gần nhất
    @GetMapping("/recent-orders")
    public List<Map<String, Object>> getRecentOrders(@RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> orderList = new ArrayList<>();
        
        orderRepository.findAll().stream()
                .filter(order -> order.getCreatedAt() != null)
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit)
                .forEach(order -> {
                    Map<String, Object> orderData = new HashMap<>();
                    orderData.put("id", order.getId());
                    orderData.put("orderNo", order.getOrderNo());
                    
                    // Lấy tên người nhận từ recipientName
                    String customerName = order.getRecipientName();
                    if (customerName == null || customerName.trim().isEmpty()) {
                        customerName = "Khách hàng #" + order.getId();
                    }
                    orderData.put("customerName", customerName);
                    
                    orderData.put("total", order.getGrandTotal() != null ? order.getGrandTotal() : BigDecimal.ZERO);
                    
                    // Xử lý status (enum -> string)
                    String statusText = "Chưa xác định";
                    if (order.getStatus() != null) {
                        statusText = order.getStatus().toString();
                    }
                    orderData.put("status", statusText);
                    
                    orderData.put("createdAt", order.getCreatedAt());
                    orderList.add(orderData);
                });
        
        return orderList;
    }

    // API lấy sản phẩm bán chạy (optional - để dành cho tương lai)
    @GetMapping("/top-products")
    public List<Map<String, Object>> getTopProducts(@RequestParam(defaultValue = "5") int limit) {
        List<Map<String, Object>> productList = new ArrayList<>();
        
        // TODO: Cần thêm logic thống kê từ OrderItem
        // Hiện tại trả về danh sách rỗng
        
        return productList;
    }
}