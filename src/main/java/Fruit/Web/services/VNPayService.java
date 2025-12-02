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
        
        // 1. Tạo Map params - CHỈ CÁC FIELD CẦN THIẾT
        Map<String, String> vnpParams = new TreeMap<>(); // TreeMap tự động sort theo key
        
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        
        // ✅ SỐ TIỀN: Nhân 100, không có dấu phẩy, không có thập phân
        long amount = order.getGrandTotal().longValue() * 100;
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", order.getOrderNo()); // Mã giao dịch - UNIQUE
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderNo());
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);
        
        // ✅ THỜI GIAN: GMT+7 (Asia/Ho_Chi_Minh)
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        // Hết hạn sau 15 phút
        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        
        // ✅ QUAN TRỌNG: TẠO SECURE HASH
        String signValue = VNPayUtil.hashAllFields(vnpParams, vnPayConfig.getHashSecret());
        vnpParams.put("vnp_SecureHash", signValue);
        
        // ✅ DEBUG - In ra để kiểm tra
        System.out.println("=== VNPAY PAYMENT URL DEBUG ===");
        System.out.println("TmnCode: " + vnpParams.get("vnp_TmnCode"));
        System.out.println("Amount: " + vnpParams.get("vnp_Amount"));
        System.out.println("TxnRef: " + vnpParams.get("vnp_TxnRef"));
        System.out.println("CreateDate: " + vnpParams.get("vnp_CreateDate"));
        System.out.println("Hash Secret Length: " + vnPayConfig.getHashSecret().length());
        System.out.println("SecureHash: " + signValue);
        System.out.println("================================");
        
        // Tạo URL
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