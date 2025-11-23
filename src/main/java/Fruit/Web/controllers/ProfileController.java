package Fruit.Web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("activePage", "profile");
        return "profile";
    }
}