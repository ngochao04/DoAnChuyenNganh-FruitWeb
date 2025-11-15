package Fruit.Web.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class PromotionAdminController {

    @GetMapping("/promotions")
    public String listPage() {
        return "admin/promotions";
    }

    @GetMapping({"/promotions/new", "/promotions/{id}"})
    public String form(@PathVariable(required = false) Long id, Model model) {
        model.addAttribute("promotionId", id);
        return "admin/promotion-form";
    }
}