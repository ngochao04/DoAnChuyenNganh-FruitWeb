package Fruit.Web.services;

import Fruit.Web.models.Category;
import Fruit.Web.models.Product;
import Fruit.Web.repositories.CategoryRepository;
import Fruit.Web.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.stream.Collectors;
import java.util.List;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class ProductServiceImpl implements ProductService {
  private final ProductRepository repo;
  private final CategoryRepository catRepo;

  public ProductServiceImpl(ProductRepository repo, CategoryRepository catRepo) {
    this.repo = repo;
    this.catRepo = catRepo;
  }

  @Override
  public Page<Product> search(String q, Pageable p) {
    if (StringUtils.hasText(q)) {
      return repo.findByNameContainingIgnoreCaseOrSlugContainingIgnoreCase(q, q, p);
    }
    return repo.findAll(p);
  }

  @Override
  public Product get(Long id) {
    return repo.findById(id).orElseThrow(NoSuchElementException::new);
  }

  @Transactional
  @Override
  public Product create(Product in) {
    // slug
    if (!StringUtils.hasText(in.getSlug())) {
      in.setSlug(CategoryService.toSlug(in.getName()));
    }
    if (repo.existsBySlug(in.getSlug())) {
      throw new IllegalArgumentException("Slug đã tồn tại");
    }

    // timestamps
    in.setCreatedAt(OffsetDateTime.now());
    in.setUpdatedAt(OffsetDateTime.now());

    // ✅ LOG TRƯỚC KHI REPLACE
    System.out.println("=== CREATE PRODUCT ===");
    System.out.println("Categories trước khi replace: " + in.getCategories().size());
    in.getCategories().forEach(c -> 
        System.out.println("  - ID: " + c.getId() + ", Name: " + c.getName())
    );

    // ✅ FIX: Replace category IDs với managed entities TRƯỚC KHI SAVE
    replaceWithManagedCategories(in);

    // ✅ LOG SAU KHI REPLACE
    System.out.println("Categories sau khi replace: " + in.getCategories().size());
    in.getCategories().forEach(c -> 
        System.out.println("  - ID: " + c.getId() + ", Name: " + c.getName())
    );

    Product saved = repo.save(in);
    
    // ✅ LOG SAU KHI SAVE
    System.out.println("Categories sau khi save: " + saved.getCategories().size());
    System.out.println("======================");
    
    return saved;
  }

  @Transactional
  @Override
  public Product update(Long id, Product in) {
    Product db = get(id);

    // fields cơ bản
    db.setName(in.getName());
    String newSlug = StringUtils.hasText(in.getSlug())
        ? in.getSlug()
        : CategoryService.toSlug(in.getName());
    db.setSlug(newSlug);

    db.setShortDesc(in.getShortDesc());
    db.setLongDesc(in.getLongDesc());
    db.setBrand(in.getBrand());
    db.setOrigin(in.getOrigin());
    db.setMainImageUrl(in.getMainImageUrl());

    if (in.getActive() != null) {
      db.setActive(in.getActive());
    }
    db.setBasePrice(in.getBasePrice());
    db.setBaseCompareAtPrice(in.getBaseCompareAtPrice());
    db.setBaseStockQty(in.getBaseStockQty() == null ? 0 : in.getBaseStockQty());
    
    // ✅ FIX: Update categories
    updateCategories(db, in.getCategories());
    
    db.setUpdatedAt(OffsetDateTime.now());
    return repo.save(db);
  }

  @Override
  public void delete(Long id) {
    repo.deleteById(id);
  }

  @Transactional
  @Override
  public Product toggleActive(Long id, boolean active) {
    Product p = get(id);
    p.setActive(active);
    p.setUpdatedAt(OffsetDateTime.now());
    return repo.save(p);
  }

  /**
   * ✅ PHƯƠNG PHÁP MỚI: Replace categories chỉ có ID với managed entities
   * Dùng cho cả CREATE và UPDATE
   */
  private void replaceWithManagedCategories(Product product) {
    if (product.getCategories() == null || product.getCategories().isEmpty()) {
      return; // Không có category nào
    }

    // Lấy danh sách ID từ categories hiện tại (chỉ có id, chưa managed)
    Set<Long> categoryIds = product.getCategories().stream()
        .filter(c -> c != null && c.getId() != null)
        .map(Category::getId)
        .collect(Collectors.toSet());

    // Clear tất cả
    product.getCategories().clear();

    // Fetch managed entities từ DB và add vào
    if (!categoryIds.isEmpty()) {
      List<Category> managedCategories = catRepo.findAllById(categoryIds);
      
      if (managedCategories.size() != categoryIds.size()) {
        throw new NoSuchElementException("Một số category không tồn tại");
      }
      
      product.getCategories().addAll(managedCategories);
    }
  }

  /**
   * ✅ DEPRECATED - Không dùng nữa
   */
  private void bindCategoriesIfPresent(Product product) {
    replaceWithManagedCategories(product);
  }

  /**
   * ✅ Update categories cho UPDATE - đơn giản hơn
   */
  private void updateCategories(Product product, Set<Category> newCategories) {
    if (newCategories == null) {
      return; // Không thay đổi
    }

    // Lấy IDs từ newCategories
    Set<Long> newIds = newCategories.stream()
        .filter(c -> c != null && c.getId() != null)
        .map(Category::getId)
        .collect(Collectors.toSet());

    // Clear và add managed entities
    product.getCategories().clear();
    
    if (!newIds.isEmpty()) {
      List<Category> managed = catRepo.findAllById(newIds);
      if (managed.size() != newIds.size()) {
        throw new NoSuchElementException("Một số category không tồn tại");
      }
      product.getCategories().addAll(managed);
    }
  }
}