package Fruit.Web.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class OrderAdminController {

    // Trang quản lý ĐƠN HÀNG
    @GetMapping("/orders")
    public String orders(Model model) {
        // nếu sau này cần truyền gì cho Thymeleaf thì add vào model
        return "admin/orders";   // trỏ tới templates/admin/orders.html
    }
}
