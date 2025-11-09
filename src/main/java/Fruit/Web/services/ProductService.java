package Fruit.Web.services;

import Fruit.Web.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
  Page<Product> search(String q, Pageable p);
  Product get(Long id);
  Product create(Product in);
  Product update(Long id, Product in);
  void delete(Long id);
  Product toggleActive(Long id, boolean active);
}
