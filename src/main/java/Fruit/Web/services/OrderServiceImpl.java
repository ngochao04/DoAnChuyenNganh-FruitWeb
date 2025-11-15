package Fruit.Web.services;

import Fruit.Web.services.dto.CreateOrderRequest;
import Fruit.Web.models.Order;
import Fruit.Web.models.OrderItem;
import Fruit.Web.models.OrderStatus;
import Fruit.Web.models.PaymentMethod;
import Fruit.Web.models.PaymentStatus;
import Fruit.Web.models.Product;
import Fruit.Web.models.ProductVariant;
import Fruit.Web.repositories.OrderItemRepository;
import Fruit.Web.repositories.OrderRepository;
import Fruit.Web.repositories.ProductRepository;
import Fruit.Web.repositories.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final ProductVariantRepository variantRepo;
    private final InventoryService inventoryService;
    private final ProductRepository productRepo;

    public OrderServiceImpl(OrderRepository orderRepo,
                            OrderItemRepository itemRepo,
                            ProductVariantRepository variantRepo,
                            InventoryService inventoryService,ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.variantRepo = variantRepo;
        this.inventoryService = inventoryService;
        this.productRepo = productRepo;
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
            Order order = new Order();
            
            // Generate unique order number
            order.setOrderNo("ORD" + System.currentTimeMillis());
            
            // Set customer info
            order.setUserId(req.userId);
            order.setRecipientName(req.recipientName);
            order.setPhone(req.phone);
            order.setAddressLine1(req.addressLine1);
            order.setWard(req.ward);
            order.setDistrict(req.district);
            order.setProvince(req.province);
            order.setNote(req.note);
            
            // Set payment info
            order.setPaymentMethod(PaymentMethod.valueOf(req.paymentMethod));
            order.setPaymentStatus(PaymentStatus.UNPAID);
            order.setStatus(OrderStatus.PENDING);
            
            // Calculate totals
            BigDecimal itemsTotal = BigDecimal.ZERO;
            
            for (CreateOrderRequest.Item item : req.items) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProductId(item.productId);
                orderItem.setQuantity(item.quantity);
                orderItem.setDiscount(BigDecimal.ZERO);
                
                // Get variant or product
                if (item.variantId != null) {
                    ProductVariant variant = variantRepo.findById(item.variantId)
                        .orElseThrow(() -> new RuntimeException("Variant not found"));
                    
                    // Check stock availability
                    if (!variant.getInStock() || variant.getStockQty() < item.quantity) {
                        throw new RuntimeException("Insufficient stock for variant: " + variant.getOptionName());
                    }
                    
                    orderItem.setVariant(variant);
                    orderItem.setUnitPrice(variant.getPrice());
                    orderItem.setTitleSnapshot(variant.getOptionName()); // ✅ Fix: getOptionName() thay vì getUnit()
                    orderItem.setUnit(variant.getUnit());
                    
                    // Calculate line total
                    orderItem.setLineTotal(variant.getPrice().multiply(BigDecimal.valueOf(item.quantity)));
                    
                    // Update stock (adjust negative)
                    inventoryService.adjustStock(variant.getId(), -item.quantity, "Bán hàng - " + order.getOrderNo());
                    
                } else {
                    // Handle base product (no variant)
                    Product product = productRepo.findById(item.productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                    
                    // Check stock availability
                    if (product.getBaseStockQty() < item.quantity) {
                        throw new RuntimeException("Insufficient stock for product: " + product.getName());
                    }
                    
                    orderItem.setUnitPrice(product.getBasePrice());
                    orderItem.setTitleSnapshot(product.getName());
                    
                    // Calculate line total
                    orderItem.setLineTotal(product.getBasePrice().multiply(BigDecimal.valueOf(item.quantity)));
                    
                    // Update base stock (manual update vì không có variant)
                    product.setBaseStockQty(product.getBaseStockQty() - item.quantity);
                    productRepo.save(product);
                }
                
                itemsTotal = itemsTotal.add(orderItem.getLineTotal());
                order.getItems().add(orderItem);
            }
            
            // Set order totals
            order.setItemsTotal(itemsTotal);
            order.setShippingFee(req.shippingFee != null ? req.shippingFee : BigDecimal.valueOf(30000));
            order.setDiscountTotal(req.discountTotal != null ? req.discountTotal : BigDecimal.ZERO);
            order.setGrandTotal(
                itemsTotal
                    .add(order.getShippingFee())
                    .subtract(order.getDiscountTotal())
            );
            
            // Save order
            return orderRepo.save(order);
        }
}
