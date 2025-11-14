package Fruit.Web.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class CustomerAdminController {

    // Trang quản lý KHÁCH HÀNG
    @GetMapping("/customers")
    public String customers(Model model) {
        return "admin/customers";   // trỏ tới templates/admin/customers.html
    }
}