package Fruit.Web.controllers.admin;

import Fruit.Web.services.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

  private final CategoryService service;
  public AdminController(CategoryService service){ this.service = service; }

  @GetMapping({"", "/"})
  public String adminHome(){ return "admin/index"; }

  @GetMapping("/categories")
  public String categoriesPage(){ return "admin/categories"; }

  @GetMapping({"/categories/new", "/categories/{id}"})
  public String categoryForm(@PathVariable(value = "id", required = false) Long id,
                             Model model) {
    model.addAttribute("parents", service.findParents());
    if (id != null) model.addAttribute("categoryId", id);
    return "admin/category-form";
  }
}
