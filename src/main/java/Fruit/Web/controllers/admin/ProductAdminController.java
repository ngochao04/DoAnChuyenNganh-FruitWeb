package Fruit.Web.controllers.admin;

import Fruit.Web.services.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller 
@RequestMapping("/admin")
public class ProductAdminController {
  private final CategoryService categoryService;
  
  public ProductAdminController(CategoryService categoryService) { 
    this.categoryService = categoryService; 
  }

  @GetMapping("/products")
  public String listPage(){ 
    return "admin/products"; 
  }

  @GetMapping({"/products/new", "/products/{id}"})
  public String form(@PathVariable(required = false) Long id, Model model){
    model.addAttribute("productId", id);
    
    // ✅ THÊM DÒNG NÀY - Truyền categories vào model
    model.addAttribute("allCategories", categoryService.findAll(true)); // onlyActive = true
    
    return "admin/product-form";
  }
  
  @GetMapping("/products/{id}/variants")
  public String variants(@PathVariable Long id, Model model) {
    model.addAttribute("productId", id);
    return "admin/product-variants";
  }
}