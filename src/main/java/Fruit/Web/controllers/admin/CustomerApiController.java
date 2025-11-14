package Fruit.Web.controllers.admin;

import Fruit.Web.models.Address;
import Fruit.Web.models.User;
import Fruit.Web.repositories.AddressRepository;
import Fruit.Web.services.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/customers")
public class CustomerApiController {

    private final UserService userService;
    private final AddressRepository addressRepository;

    public CustomerApiController(UserService userService, AddressRepository addressRepository) {
        this.userService = userService;
        this.addressRepository = addressRepository;
    }

    // =================== LIST KHÁCH HÀNG (TRANG ADMIN) ===================
    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> pg = userService.listAdminUsers(q, pageable);

        // Map từng User -> Map field đơn giản
        List<Map<String, Object>> content = pg.getContent().stream()
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", u.getId());
                    m.put("fullName", u.getFullName());
                    m.put("email", u.getEmail());
                    m.put("phone", u.getPhone());
                    m.put("isActive", u.getIsActive());
                    m.put("emailVerified", u.getEmailVerified());
                    m.put("createdAt", u.getCreatedAt());
                    m.put("updatedAt", u.getUpdatedAt());
                    
                    // Đếm số địa chỉ
                    int addressCount = addressRepository.findByUserId(u.getId()).size();
                    m.put("addressCount", addressCount);
                    
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("pageable", pg.getPageable());
        result.put("size", pg.getSize());
        result.put("number", pg.getNumber());
        result.put("totalElements", pg.getTotalElements());
        result.put("totalPages", pg.getTotalPages());
        result.put("numberOfElements", pg.getNumberOfElements());
        return result;
    }

    // =================== CHI TIẾT 1 KHÁCH HÀNG (DÙNG CHO POPUP) ===================
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Long id) {
        User u = userService.getUser(id);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("fullName", u.getFullName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("isActive", u.getIsActive());
        m.put("emailVerified", u.getEmailVerified());
        m.put("createdAt", u.getCreatedAt());
        m.put("updatedAt", u.getUpdatedAt());

        // Lấy danh sách địa chỉ của user
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(id);
        List<Map<String, Object>> addressList = addresses.stream()
                .map(this::mapAddress)
                .collect(Collectors.toList());
        m.put("addresses", addressList);

        return m;
    }

    private Map<String, Object> mapAddress(Address addr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", addr.getId());
        m.put("recipientName", addr.getRecipientName());
        m.put("phone", addr.getPhone());
        m.put("line1", addr.getLine1());
        m.put("ward", addr.getWard());
        m.put("district", addr.getDistrict());
        m.put("province", addr.getProvince());
        m.put("note", addr.getNote());
        m.put("isDefault", addr.getIsDefault());
        m.put("createdAt", addr.getCreatedAt());
        return m;
    }

    // =================== CẬP NHẬT TRẠNG THÁI KHÁCH HÀNG ===================
    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload
    ) {
        Boolean isActive = payload.get("isActive");
        if (isActive == null) {
            throw new IllegalArgumentException("Missing isActive field");
        }

        User updated = userService.updateUserStatus(id, isActive);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("id", updated.getId());
        result.put("isActive", updated.getIsActive());
        result.put("message", isActive ? "Đã kích hoạt tài khoản" : "Đã vô hiệu hóa tài khoản");
        return result;
    }

    // =================== XÓA KHÁCH HÀNG (TÙY CHỌN) ===================
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Đã xóa khách hàng");
        return result;
    }
}