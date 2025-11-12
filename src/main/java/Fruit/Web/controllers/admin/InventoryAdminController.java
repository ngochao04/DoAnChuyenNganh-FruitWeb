package Fruit.Web.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class InventoryAdminController {

  // Trang quản lý kho tổng hợp
  @GetMapping("/inventory")
  public String inventory(Model model) {
    return "admin/inventory"; // templates/admin/inventory.html
  }
}
