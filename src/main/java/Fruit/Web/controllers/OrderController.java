package Fruit.Web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderController {

    /**
     * Trang danh sách đơn hàng của tôi
     */
    @GetMapping("/orders")
    public String myOrders(Model model) {
        model.addAttribute("activePage", "orders");
        return "my-orders";
    }

    /**
     * Trang chi tiết đơn hàng
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("orderId", id);
        model.addAttribute("activePage", "orders");
        return "order-detail";
    }
}