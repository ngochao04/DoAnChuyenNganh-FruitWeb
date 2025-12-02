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
     * Tạo URL thanh toán VNPay
     */
    public String createPaymentUrl(Order order, String ipAddress) 
            throws UnsupportedEncodingException {
        
        Map<String, String> vnpParams = new HashMap<>();
        
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        
        // Số tiền (VNPay yêu cầu nhân với 100, không có dấu phẩy)
        long amount = order.getGrandTotal().longValue() * 100;
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", order.getOrderNo()); // Mã đơn hàng
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderNo());
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddress);
        
        // Thời gian tạo
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        // Thời gian hết hạn (15 phút)
        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        
        // Tạo secure hash
        String signValue = VNPayUtil.hashAllFields(vnpParams, vnPayConfig.getHashSecret());
        vnpParams.put("vnp_SecureHash", signValue);
        
        // Tạo URL
        return VNPayUtil.getPaymentURL(vnpParams, vnPayConfig.getVnpUrl());
    }

    /**
     * Verify callback từ VNPay
     */
    public boolean verifyCallback(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        
        // Remove hash params
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        
        // Tính hash
        String signValue = VNPayUtil.hashAllFields(params, vnPayConfig.getHashSecret());
        
        return signValue.equals(vnpSecureHash);
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    public boolean isPaymentSuccess(String responseCode) {
        return "00".equals(responseCode);
    }
}