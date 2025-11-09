package Fruit.Web.controllers.admin;

import Fruit.Web.services.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller @RequestMapping("/admin")
public class ProductAdminController {
  private final CategoryService categoryService;
  public ProductAdminController(CategoryService categoryService) { this.categoryService = categoryService; }

  @GetMapping("/products")
  public String listPage(){ return "admin/products"; } // templates/admin/products.html

  @GetMapping({"/products/new", "/products/{id}"})
  public String form(@PathVariable(required = false) Long id, Model model){
    model.addAttribute("productId", id);
    model.addAttribute("categories", categoryService.findParents()); // hoặc service trả toàn bộ cây
    return "admin/product-form"; // templates/admin/product-form.html
  }
  @GetMapping("/products/{id}/variants")
  public String variants(@PathVariable Long id, Model model) {
    model.addAttribute("productId", id);
    return "admin/product-variants";   // tên file .html bạn vừa tạo
  }

}
