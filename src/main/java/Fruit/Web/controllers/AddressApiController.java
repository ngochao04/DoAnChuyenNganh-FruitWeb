package Fruit.Web.controllers;

import Fruit.Web.models.Address;
import Fruit.Web.repositories.AddressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressApiController {

    private final AddressRepository addressRepository;

    public AddressApiController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    /**
     * Lấy danh sách địa chỉ của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserAddresses(@PathVariable Long userId) {
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
        
        List<Map<String, Object>> result = addresses.stream()
                .map(this::mapAddress)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Thêm địa chỉ mới
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addAddress(@RequestBody AddressRequest request) {
        Map<String, Object> response = new HashMap<>();

        // Validate
        if (request.userId == null) {
            response.put("success", false);
            response.put("message", "User ID không được để trống");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.recipientName == null || request.recipientName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Tên người nhận không được để trống");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.phone == null || request.phone.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Số điện thoại không được để trống");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.line1 == null || request.line1.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Địa chỉ không được để trống");
            return ResponseEntity.badRequest().body(response);
        }

        // Nếu đặt làm mặc định, bỏ default của các địa chỉ khác
        if (request.isDefault != null && request.isDefault) {
            List<Address> existingAddresses = addressRepository.findByUserId(request.userId);
            for (Address addr : existingAddresses) {
                if (addr.getIsDefault()) {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                }
            }
        }

        // Tạo địa chỉ mới
        Address address = new Address();
        address.setUserId(request.userId);
        address.setRecipientName(request.recipientName.trim());
        address.setPhone(request.phone.trim());
        address.setLine1(request.line1.trim());
        address.setWard(request.ward != null ? request.ward.trim() : "");
        address.setDistrict(request.district != null ? request.district.trim() : "");
        address.setProvince(request.province != null ? request.province.trim() : "");
        address.setNote(request.note != null ? request.note.trim() : "");
        address.setIsDefault(request.isDefault != null ? request.isDefault : false);
        address.setCreatedAt(LocalDateTime.now());

        Address saved = addressRepository.save(address);

        response.put("success", true);
        response.put("message", "Thêm địa chỉ thành công");
        response.put("address", mapAddress(saved));

        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật địa chỉ
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAddress(
            @PathVariable Long id,
            @RequestBody AddressRequest request) {
        
        Map<String, Object> response = new HashMap<>();

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        // Nếu đặt làm mặc định, bỏ default của các địa chỉ khác
        if (request.isDefault != null && request.isDefault && !address.getIsDefault()) {
            List<Address> existingAddresses = addressRepository.findByUserId(address.getUserId());
            for (Address addr : existingAddresses) {
                if (addr.getIsDefault() && !addr.getId().equals(id)) {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                }
            }
        }

        // Update fields
        if (request.recipientName != null) {
            address.setRecipientName(request.recipientName.trim());
        }
        if (request.phone != null) {
            address.setPhone(request.phone.trim());
        }
        if (request.line1 != null) {
            address.setLine1(request.line1.trim());
        }
        if (request.ward != null) {
            address.setWard(request.ward.trim());
        }
        if (request.district != null) {
            address.setDistrict(request.district.trim());
        }
        if (request.province != null) {
            address.setProvince(request.province.trim());
        }
        if (request.note != null) {
            address.setNote(request.note.trim());
        }
        if (request.isDefault != null) {
            address.setIsDefault(request.isDefault);
        }

        Address saved = addressRepository.save(address);

        response.put("success", true);
        response.put("message", "Cập nhật địa chỉ thành công");
        response.put("address", mapAddress(saved));

        return ResponseEntity.ok(response);
    }

    /**
     * Xóa địa chỉ
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        if (!addressRepository.existsById(id)) {
            response.put("success", false);
            response.put("message", "Không tìm thấy địa chỉ");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        addressRepository.deleteById(id);

        response.put("success", true);
        response.put("message", "Xóa địa chỉ thành công");

        return ResponseEntity.ok(response);
    }

    /**
     * Đặt địa chỉ làm mặc định
     */
    @PatchMapping("/{id}/set-default")
    public ResponseEntity<Map<String, Object>> setDefaultAddress(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        // Bỏ default của các địa chỉ khác
        List<Address> existingAddresses = addressRepository.findByUserId(address.getUserId());
        for (Address addr : existingAddresses) {
            if (addr.getIsDefault() && !addr.getId().equals(id)) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }

        // Đặt làm mặc định
        address.setIsDefault(true);
        Address saved = addressRepository.save(address);

        response.put("success", true);
        response.put("message", "Đã đặt làm địa chỉ mặc định");
        response.put("address", mapAddress(saved));

        return ResponseEntity.ok(response);
    }

    // Helper method để map Address -> Map
    private Map<String, Object> mapAddress(Address addr) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", addr.getId());
        map.put("userId", addr.getUserId());
        map.put("recipientName", addr.getRecipientName());
        map.put("phone", addr.getPhone());
        map.put("line1", addr.getLine1());
        map.put("ward", addr.getWard());
        map.put("district", addr.getDistrict());
        map.put("province", addr.getProvince());
        map.put("note", addr.getNote());
        map.put("isDefault", addr.getIsDefault());
        map.put("createdAt", addr.getCreatedAt());
        return map;
    }

    // DTO cho request
    public static class AddressRequest {
        public Long userId;
        public String recipientName;
        public String phone;
        public String line1;
        public String ward;
        public String district;
        public String province;
        public String note;
        public Boolean isDefault;
    }
}