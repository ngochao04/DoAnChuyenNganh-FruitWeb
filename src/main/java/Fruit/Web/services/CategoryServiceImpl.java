package Fruit.Web.services;

import Fruit.Web.models.Category;
import Fruit.Web.repositories.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    public CategoryServiceImpl(CategoryRepository repo) {
        this.repo = repo;
    }

    @Override
    public Page<Category> search(String q, Pageable pageable) {
        if (StringUtils.hasText(q)) {
            return repo.findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(q, q, pageable);
        }
        return repo.findAll(pageable);
    }

    @Override
    public List<Category> findParents() {
        return repo.findByParentIdIsNullOrderBySortOrderAscNameAsc();
    }

    @Override
    public List<Category> findAll(boolean onlyActive) {
        if (onlyActive) {
            return repo.findByActiveTrueOrderBySortOrderAscNameAsc();
        }
        return repo.findAll(
                Sort.by("sortOrder").ascending()
                    .and(Sort.by("name").ascending())
        );
    }

    @Override
    public Category get(Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Category not found"));
    }

    @Override
    public Category create(Category in) {
        if (!StringUtils.hasText(in.getSlug())) {
            in.setSlug(toSlug(in.getName()));
        }
        if (repo.existsBySlug(in.getSlug())) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }
        if (in.getActive() == null) in.setActive(Boolean.TRUE);
        return repo.save(in);
    }

    @Override
    public Category update(Long id, Category in) {
        Category c = get(id);
        c.setName(in.getName());
        c.setDescription(in.getDescription());
        c.setParentId(in.getParentId());
        c.setSortOrder(in.getSortOrder());
        if (in.getActive() != null) c.setActive(in.getActive());

        String newSlug = StringUtils.hasText(in.getSlug()) ? in.getSlug() : toSlug(in.getName());
        if (!newSlug.equals(c.getSlug()) && repo.existsBySlug(newSlug)) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }
        c.setSlug(newSlug);
        return repo.save(c);
    }

    @Override
    public void delete(Long id) { repo.deleteById(id); }

    @Override
    @Transactional
    public void toggleActive(Long id, boolean value) {
        Category c = repo.findById(id).orElseThrow();
        c.setActive(value);
        repo.save(c);
    }

    // Helper: tạo slug
    public static String toSlug(String input) {
        if (!StringUtils.hasText(input)) return "";
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(normalized).replaceAll("");
        slug = slug.replaceAll("[^a-zA-Z0-9\\-]", "")
                   .replaceAll("-{2,}", "-")
                   .replaceAll("(^-|-$)", "")
                   .toLowerCase(Locale.ROOT);
        return slug;
    }
}
