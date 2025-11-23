package Fruit.Web.controllers;

import Fruit.Web.models.Role;
import Fruit.Web.models.User;
import Fruit.Web.models.UserRole;
import Fruit.Web.repositories.RoleRepository;
import Fruit.Web.repositories.UserRepository;
import Fruit.Web.repositories.UserRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthApiController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public AuthApiController(UserRepository userRepository, 
                           RoleRepository roleRepository,
                           UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * ✅ ĐĂNG KÝ TÀI KHOẢN MỚI - ĐÃ SỬA
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (request.fullName == null || request.fullName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Họ tên không được để trống");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.email == null || !request.email.contains("@")) {
            response.put("success", false);
            response.put("message", "Email không hợp lệ");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.password == null || request.password.length() < 6) {
            response.put("success", false);
            response.put("message", "Mật khẩu phải có ít nhất 6 ký tự");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra email đã tồn tại
        if (userRepository.findByEmail(request.email).isPresent()) {
            response.put("success", false);
            response.put("message", "Email đã được sử dụng");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Tạo user mới
        User user = new User();
        user.setFullName(request.fullName.trim());
        user.setEmail(request.email.toLowerCase().trim());
        user.setPasswordHash(hashPassword(request.password));
        user.setPhone(request.phone != null ? request.phone.trim() : "");
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // ✅ TỰ ĐỘNG GÁN ROLE CUSTOMER
        try {
            Role customerRole = roleRepository.findByCode("CUSTOMER")
                .orElseGet(() -> {
                    // Nếu chưa có role CUSTOMER, tạo mới
                    Role newRole = new Role();
                    newRole.setCode("CUSTOMER");
                    newRole.setName("Khách hàng");
                    return roleRepository.save(newRole);
                });
            
            UserRole userRole = new UserRole();
            userRole.setUserId(savedUser.getId());
            userRole.setRoleId(customerRole.getId());
            userRoleRepository.save(userRole);
            
            System.out.println("✅ Assigned CUSTOMER role to user: " + savedUser.getEmail());
        } catch (Exception e) {
            System.err.println("⚠️ Failed to assign role, but user created: " + e.getMessage());
            e.printStackTrace();
        }

        // Tạo token đơn giản (trong production nên dùng JWT)
        String token = "TOKEN_" + UUID.randomUUID().toString();

        response.put("success", true);
        response.put("message", "Đăng ký thành công");
        response.put("token", token);
        response.put("user", Map.of(
                "id", savedUser.getId(),
                "fullName", savedUser.getFullName(),
                "email", savedUser.getEmail(),
                "phone", savedUser.getPhone() != null ? savedUser.getPhone() : ""
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ ĐĂNG NHẬP
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (request.email == null || request.email.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Email không được để trống");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.password == null || request.password.isEmpty()) {
            response.put("success", false);
            response.put("message", "Mật khẩu không được để trống");
            return ResponseEntity.badRequest().body(response);
        }

        // Tìm user
        Optional<User> userOpt = userRepository.findByEmail(request.email.toLowerCase().trim());

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email hoặc mật khẩu không đúng");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        User user = userOpt.get();

        // Kiểm tra mật khẩu
        if (!verifyPassword(request.password, user.getPasswordHash())) {
            response.put("success", false);
            response.put("message", "Email hoặc mật khẩu không đúng");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Kiểm tra tài khoản bị khóa
        if (!user.getIsActive()) {
            response.put("success", false);
            response.put("message", "Tài khoản đã bị khóa. Vui lòng liên hệ admin");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // Tạo token
        String token = "TOKEN_" + UUID.randomUUID().toString();

        response.put("success", true);
        response.put("message", "Đăng nhập thành công");
        response.put("token", token);
        response.put("user", Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "phone", user.getPhone() != null ? user.getPhone() : ""
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ ĐĂNG XUẤT
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đăng xuất thành công");
        return ResponseEntity.ok(response);
    }

    // ========== DTO Classes ==========

    public static class RegisterRequest {
        public String fullName;
        public String email;
        public String password;
        public String phone;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    // ========== Helper Methods ==========

    /**
     * Hash password đơn giản (trong production nên dùng BCrypt)
     */
    private String hashPassword(String password) {
        // Đơn giản hóa - trong thực tế nên dùng BCryptPasswordEncoder
        return "HASHED_" + password.hashCode();
    }

    /**
     * Verify password
     */
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        return hashedPassword.equals("HASHED_" + rawPassword.hashCode());
    }
}