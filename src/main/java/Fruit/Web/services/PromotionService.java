package Fruit.Web.services;

import Fruit.Web.models.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PromotionService {

    Page<Promotion> search(String query, Pageable pageable);

    Promotion get(Long id);

    Promotion create(Promotion promotion);

    Promotion update(Long id, Promotion promotion);

    void toggleActive(Long id, Boolean isActive);

    void delete(Long id);
}