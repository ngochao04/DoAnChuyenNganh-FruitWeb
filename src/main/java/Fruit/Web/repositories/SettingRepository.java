package Fruit.Web.repositories;

import Fruit.Web.models.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    Optional<Setting> findByKey(String key);
    List<Setting> findByCategory(String category);
    List<Setting> findByIsPublic(Boolean isPublic);
}