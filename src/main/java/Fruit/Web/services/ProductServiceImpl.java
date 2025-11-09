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

    // Bind danh mục nếu client gửi kèm (categories chỉ cần id)
    bindCategoriesIfPresent(in, null);

    // KHÔNG động tới variants/images ở bước tạo qua form sản phẩm cơ bản
    // (chúng do module Biến thể & Kho quản lý)

    return repo.save(in);
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
    
    bindCategoriesIfPresent(in, db);
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
   * Bind danh mục:
   * - Nếu src.getCategories() == null -> bỏ qua (không thay đổi danh mục hiện có)
   * - Nếu có -> thay thế toàn bộ bằng danh mục từ id được gửi lên.
   */
  private void bindCategoriesIfPresent(Product src, Product owner) {
    if (src.getCategories() == null) return; // client không gửi -> giữ nguyên

    Product target = (owner != null ? owner : src);
    // thay bằng set mới theo id
    Set<Category> newCats = new HashSet<>();
    for (Category c : src.getCategories()) {
      if (c != null && c.getId() != null) {
        newCats.add(catRepo.findById(c.getId()).orElseThrow(NoSuchElementException::new));
      }
    }
    target.getCategories().clear();
    target.getCategories().addAll(newCats);
  }
}
