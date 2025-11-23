package Fruit.Web.repositories;

import Fruit.Web.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // ✅ FIX: Cho phép users không có role vẫn hiển thị
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN UserRole ur ON ur.userId = u.id " +
           "LEFT JOIN Role r ON r.id = ur.roleId " +
           "WHERE (r.code IS NULL OR r.code = 'CUSTOMER') AND " +
           "(:q IS NULL OR :q = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(u.phone) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<User> searchUsers(@Param("q") String query, Pageable pageable);
    
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}