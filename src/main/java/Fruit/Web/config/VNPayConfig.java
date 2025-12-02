package Fruit.Web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    
    // ✅ CHÍNH XÁC THEO EMAIL VNPAY
    @Value("${vnpay.tmn-code:QUMHVW1D}")
    private String tmnCode;
    
    // ✅ CHÍNH XÁC - KHÔNG CÓ KHOẢNG TRẮNG
    @Value("${vnpay.hash-secret:NMQ3LXWOJCNMA9YE5JVXDO9TFZEL717Q}")
    private String hashSecret;
    
    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpUrl;
    
    // ✅ QUAN TRỌNG: Phải là domain thực, không được localhost trong production
    @Value("${vnpay.return-url:http://localhost:8080/vnpay/callback}")
    private String returnUrl;
    
    @Value("${vnpay.version:2.1.0}")
    private String version;
    
    @Value("${vnpay.command:pay}")
    private String command;
    
    @Value("${vnpay.order-type:other}")
    private String orderType;
    
    // Getters
    public String getTmnCode() { return tmnCode; }
    public String getHashSecret() { return hashSecret; }
    public String getVnpUrl() { return vnpUrl; }
    public String getReturnUrl() { return returnUrl; }
    public String getVersion() { return version; }
    public String getCommand() { return command; }
    public String getOrderType() { return orderType; }
}