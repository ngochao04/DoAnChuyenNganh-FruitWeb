package Fruit.Web.controllers.admin;

import Fruit.Web.models.Category;
import Fruit.Web.services.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryApiController {

    private final CategoryService service;

    public CategoryApiController(CategoryService service) {
        this.service = service;
    }

    // Danh sách + tìm kiếm + phân trang
    @GetMapping
    public Page<Map<String, Object>> listApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q) {

        return service.search(q, PageRequest.of(page, size))
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",        c.getId());
                    m.put("name",      c.getName());
                    m.put("slug",      c.getSlug());
                    m.put("description", c.getDescription());
                    m.put("parentId",  c.getParentId());
                    m.put("sortOrder", c.getSortOrder());
                    m.put("active",    c.getActive());
                    return m;
                });
    }

    // API tối giản cho form sản phẩm (multi-select)
    @GetMapping("/all-min")
    public List<Map<String, Object>> allMin(
            @RequestParam(defaultValue = "true") boolean onlyActive) {
        return service.findAll(onlyActive).stream()
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",   c.getId());
                    m.put("name", c.getName());
                    m.put("slug", c.getSlug());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // CRUD
    @GetMapping("/{id}")
    public Category get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    public Category create(@RequestBody Category c) { return service.create(c); }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Category c) { return service.update(id, c); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }

    // Bật/tắt trạng thái
    @PatchMapping("/{id}/active")
    public void toggleActive(@PathVariable Long id, @RequestParam("v") boolean value) {
        service.toggleActive(id, value);
    }
}
