package Fruit.Web.services;

import Fruit.Web.config.VNPayConfig;
import Fruit.Web.models.Order;
import Fruit.Web.utils.VNPayUtil;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    private final VNPayConfig vnPayConfig;

    public VNPayService(VNPayConfig vnPayConfig) {
        this.vnPayConfig = vnPayConfig;
    }

    /**
     * ✅ TẠO URL THANH TOÁN VNPAY - ĐÃ FIX
     */
    public String createPaymentUrl(Order order, String ipAddress) 
        throws UnsupportedEncodingException {
    
    // 1. Tạo params - Dùng TreeMap để tự động sort
    Map<String, String> vnpParams = new TreeMap<>();
    
    vnpParams.put("vnp_Version", vnPayConfig.getVersion());
    vnpParams.put("vnp_Command", vnPayConfig.getCommand());
    vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
    
    // ✅ Amount: Nhân 100, KHÔNG có dấu phẩy
    long amount = order.getGrandTotal().longValue() * 100;
    vnpParams.put("vnp_Amount", String.valueOf(amount));
    
    vnpParams.put("vnp_CurrCode", "VND");
    vnpParams.put("vnp_TxnRef", order.getOrderNo()); // Mã đơn hàng - UNIQUE
    vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderNo());
    vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
    vnpParams.put("vnp_Locale", "vn");
    vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
    vnpParams.put("vnp_IpAddr", ipAddress);
    
    // ✅ Thời gian GMT+7
    Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
    formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    
    String vnpCreateDate = formatter.format(cld.getTime());
    vnpParams.put("vnp_CreateDate", vnpCreateDate);
    
    // Hết hạn sau 15 phút
    cld.add(Calendar.MINUTE, 15);
    String vnpExpireDate = formatter.format(cld.getTime());
    vnpParams.put("vnp_ExpireDate", vnpExpireDate);
    
    // ✅ QUAN TRỌNG: Tạo secure hash TRƯỚC khi add vào params
    String signValue = VNPayUtil.hashAllFields(vnpParams, vnPayConfig.getHashSecret());
    vnpParams.put("vnp_SecureHash", signValue);
    
    // ✅ Debug - In toàn bộ params
    System.out.println("=== ALL VNPAY PARAMS ===");
    vnpParams.forEach((k, v) -> System.out.println(k + " = " + v));
    System.out.println("=======================");
    
    return VNPayUtil.getPaymentURL(vnpParams, vnPayConfig.getVnpUrl());
}

    /**
     * ✅ VERIFY CALLBACK TỪ VNPAY
     */
    public boolean verifyCallback(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        
        // Tạo bản sao và xóa các field hash
        Map<String, String> fields = new TreeMap<>(params);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        
        // Tính hash từ dữ liệu nhận được
        String signValue = VNPayUtil.hashAllFields(fields, vnPayConfig.getHashSecret());
        
        System.out.println("=== VERIFY CALLBACK ===");
        System.out.println("Hash from VNPay: " + vnpSecureHash);
        System.out.println("Hash calculated: " + signValue);
        System.out.println("Match: " + signValue.equals(vnpSecureHash));
        
        return signValue.equals(vnpSecureHash);
    }

    /**
     * Kiểm tra thanh toán thành công
     */
    public boolean isPaymentSuccess(String responseCode) {
        return "00".equals(responseCode);
    }
}