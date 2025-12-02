package Fruit.Web.controllers;

import Fruit.Web.models.Order;
import Fruit.Web.models.OrderStatus;
import Fruit.Web.models.PaymentStatus;
import Fruit.Web.repositories.OrderRepository;
import Fruit.Web.services.VNPayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/vnpay")
public class VNPayController {

    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;

    public VNPayController(VNPayService vnPayService, OrderRepository orderRepository) {
        this.vnPayService = vnPayService;
        this.orderRepository = orderRepository;
    }

    /**
     * VNPay callback - Xử lý kết quả thanh toán
     */
    @GetMapping("/callback")
    public String vnpayCallback(@RequestParam Map<String, String> params, Model model) {
        
        System.out.println("=== VNPay Callback ===");
        params.forEach((key, value) -> System.out.println(key + " = " + value));
        
        // Verify signature
        boolean isValid = vnPayService.verifyCallback(params);
        
        if (!isValid) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Chữ ký không hợp lệ!");
            return "payment-result";
        }
        
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef"); // Order No
        String transactionNo = params.get("vnp_TransactionNo");
        String amount = params.get("vnp_Amount");
        
        // Tìm order
        Order order = orderRepository.findAll().stream()
                .filter(o -> o.getOrderNo().equals(txnRef))
                .findFirst()
                .orElse(null);
        
        if (order == null) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Không tìm thấy đơn hàng!");
            return "payment-result";
        }
        
        // Kiểm tra thanh toán thành công
        if (vnPayService.isPaymentSuccess(responseCode)) {
            // ✅ CẬP NHẬT ĐƠN HÀNG
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.SHIPPED); // ✅ TỰ ĐỘNG CHUYỂN SANG ĐANG GIAO
            orderRepository.save(order);
            
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("orderNo", txnRef);
            model.addAttribute("amount", Long.parseLong(amount) / 100);
            model.addAttribute("transactionNo", transactionNo);
            model.addAttribute("orderId", order.getId());
            
        } else {
            // ❌ THANH TOÁN THẤT BẠI
            model.addAttribute("success", false);
            model.addAttribute("message", "Thanh toán thất bại! Mã lỗi: " + responseCode);
            model.addAttribute("orderNo", txnRef);
        }
        
        return "payment-result";
    }
}