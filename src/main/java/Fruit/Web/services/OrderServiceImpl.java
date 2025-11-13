package Fruit.Web.services;

import Fruit.Web.services.dto.CreateOrderRequest;
import Fruit.Web.models.Order;
import Fruit.Web.models.OrderItem;
import Fruit.Web.models.ProductVariant;
import Fruit.Web.repositories.OrderItemRepository;
import Fruit.Web.repositories.OrderRepository;
import Fruit.Web.repositories.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final ProductVariantRepository variantRepo;
    private final InventoryService inventoryService;

    public OrderServiceImpl(OrderRepository orderRepo,
                            OrderItemRepository itemRepo,
                            ProductVariantRepository variantRepo,
                            InventoryService inventoryService) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.variantRepo = variantRepo;
        this.inventoryService = inventoryService;
    }

    // ===== ADMIN LIST =====
    @Override
    public Page<Order> listAdminOrders(String q, Pageable pageable) {
        if (StringUtils.hasText(q)) {
            return orderRepo.searchAdmin(q, pageable);
        }
        return orderRepo.findAll(pageable);
    }

    // ===== XEM CHI TIẾT 1 ĐƠN =====
    @Override
    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        Order o = orderRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));
        // ép Hibernate load collection nếu là LAZY
        o.getItems().size();
        return o;
    }

    // ===== TẠO ĐƠN (GIỮ NGUYÊN LOGIC CŨ) =====
    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        // Ở đây em GIỮ NGUYÊN body method cũ của em
        // Anh chỉ sửa lại kiểu parameter cho khớp interface thôi

        // VÍ DỤ (đây chỉ là minh hoạ, thay bằng code của em):
        /*
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setRecipientName(req.getRecipientName());
        ...
        return orderRepo.save(order);
        */
        throw new UnsupportedOperationException("TODO: dán lại body createOrder cũ của em vào đây");
    }
}
