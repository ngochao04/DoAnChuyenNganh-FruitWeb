package Fruit.Web.controllers.admin;

import Fruit.Web.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class ReportAdminController {

    @Autowired
    private ReportService reportService;

    // Trang báo cáo
    @GetMapping("/reports")
    public String reports() {
        return "admin/reports";
    }

    // API: Báo cáo doanh thu
    @GetMapping("/reports/revenue")
    @ResponseBody
    public Map<String, Object> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "day") String groupBy) {
        
        return reportService.getRevenueReport(startDate, endDate, groupBy);
    }

    // API: Báo cáo đơn hàng theo trạng thái
    @GetMapping("/reports/orders-by-status")
    @ResponseBody
    public Map<String, Object> getOrdersByStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return reportService.getOrdersByStatusReport(startDate, endDate);
    }

    // API: Báo cáo khách hàng
    @GetMapping("/reports/customers")
    @ResponseBody
    public Map<String, Object> getCustomersReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return reportService.getCustomersReport(startDate, endDate);
    }

    // API: Báo cáo sản phẩm
    @GetMapping("/reports/products")
    @ResponseBody
    public Map<String, Object> getProductsReport() {
        return reportService.getProductsReport();
    }
}