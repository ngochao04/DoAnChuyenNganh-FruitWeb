package Fruit.Web.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class ReportAdminController {

    @GetMapping("/reports")
    public String reports() {
        return "admin/reports";
    }
}