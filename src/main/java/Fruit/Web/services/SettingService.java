package Fruit.Web.services;

import Fruit.Web.models.Setting;
import Fruit.Web.repositories.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SettingService {

    @Autowired
    private SettingRepository settingRepository;

    // Lấy giá trị setting theo key
    public String getValue(String key, String defaultValue) {
        return settingRepository.findByKey(key)
                .map(Setting::getValue)
                .orElse(defaultValue);
    }

    // Lấy giá trị Boolean
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        String value = getValue(key, null);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    // Lấy giá trị Number
    public Integer getIntValue(String key, Integer defaultValue) {
        String value = getValue(key, null);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Lấy tất cả settings theo category
    public Map<String, Object> getSettingsByCategory(String category) {
        List<Setting> settings = settingRepository.findByCategory(category);
        Map<String, Object> result = new HashMap<>();
        
        for (Setting setting : settings) {
            result.put(setting.getKey(), convertValue(setting));
        }
        
        return result;
    }

    // Lấy tất cả categories với settings
    public Map<String, Map<String, Object>> getAllSettingsGrouped() {
        List<Setting> allSettings = settingRepository.findAll();
        Map<String, Map<String, Object>> grouped = new HashMap<>();
        
        for (Setting setting : allSettings) {
            String cat = setting.getCategory() != null ? setting.getCategory() : "OTHER";
            grouped.putIfAbsent(cat, new HashMap<>());
            
            Map<String, Object> settingData = new HashMap<>();
            settingData.put("value", convertValue(setting));
            settingData.put("type", setting.getType());
            settingData.put("description", setting.getDescription());
            
            grouped.get(cat).put(setting.getKey(), settingData);
        }
        
        return grouped;
    }

    // Cập nhật hoặc tạo mới setting
    @Transactional
    public Setting updateSetting(String key, String value, Long userId) {
        Optional<Setting> existingOpt = settingRepository.findByKey(key);
        
        Setting setting;
        if (existingOpt.isPresent()) {
            setting = existingOpt.get();
            setting.setValue(value);
        } else {
            setting = new Setting();
            setting.setKey(key);
            setting.setValue(value);
            setting.setType("STRING");
        }
        
        setting.setUpdatedBy(userId);
        return settingRepository.save(setting);
    }

    // Cập nhật nhiều settings cùng lúc
    @Transactional
    public void updateMultipleSettings(Map<String, String> settings, Long userId) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            updateSetting(entry.getKey(), entry.getValue(), userId);
        }
    }

    // Helper: Convert value theo type
    private Object convertValue(Setting setting) {
        if (setting.getValue() == null) return null;
        
        switch (setting.getType()) {
            case "BOOLEAN":
                return Boolean.parseBoolean(setting.getValue());
            case "NUMBER":
                try {
                    return Integer.parseInt(setting.getValue());
                } catch (NumberFormatException e) {
                    return setting.getValue();
                }
            case "JSON":
                return setting.getValue(); // Frontend sẽ parse JSON
            default:
                return setting.getValue();
        }
    }
}