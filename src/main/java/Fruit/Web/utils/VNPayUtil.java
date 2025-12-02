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
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }

    /**
     * ✅ TẠO CHUỖI HASH TỪ CÁC THAM SỐ
     * Quan trọng: Phải sắp xếp theo thứ tự alphabet
     */
    public static String hashAllFields(Map<String, String> fields, String hashSecret) {
        // 1. Sắp xếp các key theo alphabet
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        
        // 2. Tạo chuỗi query string
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            
            // Chỉ thêm field có giá trị không rỗng
            if (fieldValue != null && fieldValue.length() > 0) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
                
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        
        // 3. Tạo HMAC SHA512
        return hmacSHA512(hashSecret, sb.toString());
    }

    /**
     * ✅ TẠO URL THANH TOÁN
     * Encoding đúng chuẩn UTF-8
     */
    public static String getPaymentURL(Map<String, String> params, String baseUrl) 
            throws UnsupportedEncodingException {
        
        // Sắp xếp params
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            
            if (fieldValue != null && fieldValue.length() > 0) {
                // ✅ QUAN TRỌNG: URLEncode với UTF-8
                query.append(URLEncoder.encode(fieldName, "UTF-8"));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, "UTF-8"));
                
                if (itr.hasNext()) {
                    query.append('&');
                }
            }
        }
        
        return baseUrl + "?" + query.toString();
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}