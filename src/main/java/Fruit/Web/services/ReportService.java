package Fruit.Web.services;

import Fruit.Web.models.Order;
import Fruit.Web.models.OrderStatus;
import Fruit.Web.repositories.OrderRepository;
import Fruit.Web.repositories.UserRepository;
import Fruit.Web.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // Báo cáo doanh thu
    public Map<String, Object> getRevenueReport(LocalDate startDate, LocalDate endDate, String groupBy) {
        OffsetDateTime start = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<Order> orders = orderRepository.findByCreatedAtBetweenAndStatusNot(start, end, OrderStatus.CANCELLED);

        // Tính tổng doanh thu
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getGrandTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng đơn hàng
        int totalOrders = orders.size();

        // Giá trị trung bình đơn hàng
        BigDecimal avgOrderValue = totalOrders > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Dữ liệu biểu đồ
        Map<String, BigDecimal> chartData = new LinkedHashMap<>();
        DateTimeFormatter formatter;

        switch (groupBy) {
            case "week":
                formatter = DateTimeFormatter.ofPattern("'W'ww/yyyy");
                break;
            case "month":
                formatter = DateTimeFormatter.ofPattern("MM/yyyy");
                break;
            default: // day
                formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        }

        for (Order order : orders) {
            String key = order.getCreatedAt().toLocalDate().format(formatter);
            chartData.merge(key, order.getGrandTotal(), BigDecimal::add);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("totalOrders", totalOrders);
        result.put("avgOrderValue", avgOrderValue);
        result.put("chartData", chartData);

        return result;
    }

    // Báo cáo đơn hàng theo trạng thái
    public Map<String, Object> getOrdersByStatusReport(LocalDate startDate, LocalDate endDate) {
        OffsetDateTime start = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        Map<String, Long> statusCount = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus() != null ? order.getStatus().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("statusCount", statusCount);
        result.put("totalOrders", orders.size());

        return result;
    }

    // Báo cáo khách hàng
    public Map<String, Object> getCustomersReport(LocalDate startDate, LocalDate endDate) {
        OffsetDateTime start = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        // Tổng số khách hàng
        long totalCustomers = userRepository.count();

        // Khách hàng mới trong khoảng thời gian
        long newCustomers = userRepository.countByCreatedAtBetween(
                start.toLocalDateTime(), 
                end.toLocalDateTime()
        );

        // Top 10 khách hàng theo số đơn hàng
        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);
        
        Map<Long, Long> userOrderCount = orders.stream()
                .filter(o -> o.getUserId() != null)
                .collect(Collectors.groupingBy(
                        Order::getUserId,
                        Collectors.counting()
                ));

        List<Map<String, Object>> topCustomers = userOrderCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("userId", entry.getKey());
                    customer.put("orderCount", entry.getValue());
                    return customer;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalCustomers", totalCustomers);
        result.put("newCustomers", newCustomers);
        result.put("topCustomers", topCustomers);

        return result;
    }

    // Báo cáo sản phẩm
    public Map<String, Object> getProductsReport() {
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByActive(true);
        
        // Sản phẩm sắp hết hàng (tồn kho < 10)
        long lowStockProducts = productRepository.countByBaseStockQtyLessThan(10);

        Map<String, Object> result = new HashMap<>();
        result.put("totalProducts", totalProducts);
        result.put("activeProducts", activeProducts);
        result.put("lowStockProducts", lowStockProducts);

        return result;
    }
}