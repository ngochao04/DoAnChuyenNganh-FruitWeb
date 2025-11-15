package Fruit.Web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Trang chủ
    @GetMapping({"/", "/index"})
    public String home(Model model) {
        model.addAttribute("activePage", "home");
        return "index";
    }

    // Cửa hàng
    @GetMapping("/shop")
    public String shop(Model model) {
        model.addAttribute("activePage", "shop");
        return "shop";
    }

    // Chi tiết sản phẩm
    @GetMapping("/shop-detail")
    public String shopDetail(Model model) {
        model.addAttribute("activePage", "shop");
        return "shop-detail";
    }

    // Giỏ hàng
    @GetMapping("/cart")
    public String cart(Model model) {
        return "cart";
    }

    // Thanh toán
    @GetMapping("/checkout")
    public String checkout(Model model) {
        return "checkout";
    }

    // Liên hệ
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("activePage", "contact");
        return "contact";
    }
}