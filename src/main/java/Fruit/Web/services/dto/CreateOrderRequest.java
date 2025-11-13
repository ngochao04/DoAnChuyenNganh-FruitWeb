package Fruit.Web.services.dto;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequest {

    public Long userId;
    public String recipientName;
    public String phone;
    public String addressLine1;
    public String ward;
    public String district;
    public String province;
    public String note;

    public String paymentMethod; // "COD", "VNPAY", ...

    public BigDecimal shippingFee;
    public BigDecimal discountTotal;

    public List<Item> items;

    public static class Item {
        public Long productId;
        public Long variantId;
        public Integer quantity;
    }
}
