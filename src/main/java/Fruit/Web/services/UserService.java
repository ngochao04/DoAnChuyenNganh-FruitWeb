package Fruit.Web.services;

import Fruit.Web.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * Lấy danh sách users (admin) - có tìm kiếm & phân trang
     */
    Page<User> listAdminUsers(String query, Pageable pageable);

    /**
     * Lấy chi tiết 1 user
     */
    User getUser(Long id);

    /**
     * Cập nhật trạng thái active/inactive
     */
    User updateUserStatus(Long id, Boolean isActive);

    /**
     * Xóa user (nếu cần)
     */
    void deleteUser(Long id);
}