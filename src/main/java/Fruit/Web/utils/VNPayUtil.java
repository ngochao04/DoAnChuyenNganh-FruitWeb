package Fruit.Web.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayUtil {

    /**
     * ✅ HMAC SHA512 - CHÍNH XÁC THEO TÀI LIỆU VNPAY
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Convert sang hex (chữ thường)
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }

    /**
     * ✅ TẠO HASH TỪ CÁC PARAMS
     * QUY TẮC VNPAY:
     * 1. Sắp xếp params theo thứ tự alphabet (A-Z)
     * 2. Loại bỏ vnp_SecureHash và vnp_SecureHashType
     * 3. Loại bỏ các field có giá trị rỗng
     * 4. Nối các param: key1=value1&key2=value2&...
     * 5. HMAC SHA512 với Hash Secret
     */
    public static String hashAllFields(Map<String, String> fields, String hashSecret) {
        // Tạo TreeMap để tự động sort theo alphabet
        Map<String, String> sortedFields = new TreeMap<>();
        
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // ✅ BỎ QUA: vnp_SecureHash, vnp_SecureHashType và giá trị rỗng
            if (value != null && !value.isEmpty() 
                && !key.equals("vnp_SecureHash") 
                && !key.equals("vnp_SecureHashType")) {
                sortedFields.put(key, value);
            }
        }
        
        // Tạo chuỗi hash data
        StringBuilder hashData = new StringBuilder();
        boolean isFirst = true;
        
        for (Map.Entry<String, String> entry : sortedFields.entrySet()) {
            if (!isFirst) {
                hashData.append('&');
            }
            hashData.append(entry.getKey());
            hashData.append('=');
            hashData.append(entry.getValue());
            isFirst = false;
        }
        
        String hashDataStr = hashData.toString();
        
        // ✅ DEBUG - In ra để kiểm tra
        System.out.println("=== HASH DATA DEBUG ===");
        System.out.println("Hash Data String: " + hashDataStr);
        System.out.println("Hash Secret: " + hashSecret);
        String hash = hmacSHA512(hashSecret, hashDataStr);
        System.out.println("Generated Hash: " + hash);
        System.out.println("======================");
        
        return hash;
    }

    /**
     * ✅ TẠO PAYMENT URL
     */
    public static String getPaymentURL(Map<String, String> params, String baseUrl) 
            throws UnsupportedEncodingException {
        
        // Sort params theo alphabet
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder query = new StringBuilder();
        boolean isFirst = true;
        
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (!isFirst) {
                    query.append('&');
                }
                // ✅ URL Encode
                query.append(URLEncoder.encode(fieldName, "UTF-8"));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, "UTF-8"));
                isFirst = false;
            }
        }
        
        String finalUrl = baseUrl + "?" + query.toString();
        
        System.out.println("=== PAYMENT URL ===");
        System.out.println(finalUrl);
        System.out.println("==================");
        
        return finalUrl;
    }
}