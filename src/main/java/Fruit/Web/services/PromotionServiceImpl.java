package Fruit.Web.services;

import Fruit.Web.models.Promotion;
import Fruit.Web.repositories.PromotionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionServiceImpl(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    public Page<Promotion> search(String query, Pageable pageable) {
        return promotionRepository.searchPromotions(query, pageable);
    }

    @Override
    public Promotion get(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
    }

    @Override
    public Promotion create(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    @Override
    public Promotion update(Long id, Promotion promotion) {
        Promotion existing = get(id);
        
        existing.setName(promotion.getName());
        existing.setDescription(promotion.getDescription());
        existing.setStartsAt(promotion.getStartsAt());
        existing.setEndsAt(promotion.getEndsAt());
        existing.setMinOrderAmount(promotion.getMinOrderAmount());
        existing.setShippingDiscount(promotion.getShippingDiscount());
        existing.setAmountOff(promotion.getAmountOff());
        existing.setPercentOff(promotion.getPercentOff());
        existing.setIsActive(promotion.getIsActive());
        
        return promotionRepository.save(existing);
    }

    @Override
    public void toggleActive(Long id, Boolean isActive) {
        Promotion promotion = get(id);
        promotion.setIsActive(isActive);
        promotionRepository.save(promotion);
    }

    @Override
    public void delete(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion not found with id: " + id);
        }
        promotionRepository.deleteById(id);
    }
}