package Fruit.Web.controllers.admin;

import Fruit.Web.services.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class SettingAdminController {

    @Autowired
    private SettingService settingService;

    // Hiển thị trang cấu hình
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        return "admin/settings";
    }

    // API: Lấy tất cả settings
    @GetMapping("/api/settings")
    @ResponseBody
    public ResponseEntity<Map<String, Map<String, Object>>> getAllSettings() {
        Map<String, Map<String, Object>> settings = settingService.getAllSettingsGrouped();
        return ResponseEntity.ok(settings);
    }

    // API: Lấy settings theo category
    @GetMapping("/api/settings/{category}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSettingsByCategory(@PathVariable String category) {
        Map<String, Object> settings = settingService.getSettingsByCategory(category);
        return ResponseEntity.ok(settings);
    }

    // API: Cập nhật settings
    @PostMapping("/api/settings")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateSettings(@RequestBody Map<String, String> settings) {
        // TODO: Lấy userId từ session/authentication
        Long userId = 1L; // Hardcode tạm thời, sau này lấy từ authentication
        
        try {
            settingService.updateMultipleSettings(settings, userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cập nhật cấu hình thành công");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Lỗi: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API: Test kết nối thanh toán
    @PostMapping("/api/settings/test-payment")
    @ResponseBody
    public ResponseEntity<Map<String, String>> testPaymentConnection(@RequestBody Map<String, String> config) {
        Map<String, String> response = new HashMap<>();
        String gateway = config.get("gateway");
        
        try {
            if ("momo".equalsIgnoreCase(gateway)) {
                // Test MoMo connection
                String partnerCode = config.get("partnerCode");
                String accessKey = config.get("accessKey");
                String secretKey = config.get("secretKey");
                
                if (partnerCode == null || partnerCode.trim().isEmpty() ||
                    accessKey == null || accessKey.trim().isEmpty() ||
                    secretKey == null || secretKey.trim().isEmpty()) {
                    response.put("status", "error");
                    response.put("message", "Vui lòng nhập đầy đủ thông tin MoMo (Partner Code, Access Key, Secret Key)");
                    return ResponseEntity.ok(response);
                }
                
                // TODO: Implement actual MoMo API test call here
                // For now, just validate the fields are not empty
                response.put("status", "success");
                response.put("message", "Thông tin MoMo hợp lệ! (Lưu ý: Cần implement API test thực tế)");
                
            } else if ("vnpay".equalsIgnoreCase(gateway)) {
                // Test VNPay connection
                String tmnCode = config.get("tmnCode");
                String hashSecret = config.get("hashSecret");
                
                if (tmnCode == null || tmnCode.trim().isEmpty() ||
                    hashSecret == null || hashSecret.trim().isEmpty()) {
                    response.put("status", "error");
                    response.put("message", "Vui lòng nhập đầy đủ thông tin VNPay (TMN Code, Hash Secret)");
                    return ResponseEntity.ok(response);
                }
                
                // TODO: Implement actual VNPay API test call here
                // For now, just validate the fields are not empty
                response.put("status", "success");
                response.put("message", "Thông tin VNPay hợp lệ! (Lưu ý: Cần implement API test thực tế)");
                
            } else {
                response.put("status", "error");
                response.put("message", "Gateway không được hỗ trợ: " + gateway);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi test kết nối: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}