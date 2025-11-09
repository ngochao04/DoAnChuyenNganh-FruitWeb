package Fruit.Web.services;

import Fruit.Web.models.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    Page<Category> search(String q, Pageable pageable);

    List<Category> findParents();

    // NEW: trả danh sách category (lọc theo active nếu cần)
    List<Category> findAll(boolean onlyActive);

    Category get(Long id);

    Category create(Category in);

    Category update(Long id, Category in);

    void delete(Long id);

    // NEW: bật/tắt trạng thái
    void toggleActive(Long id, boolean value);

    // Optional: tiện ích slugify (nếu bạn đang dùng)
    static String toSlug(String input) { return CategoryServiceImpl.toSlug(input); }
}
