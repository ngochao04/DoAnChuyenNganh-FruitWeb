package Fruit.Web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.upload.dir}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Truy cập ảnh qua /uploads/**  (vd: /uploads/20250313_abc.jpg)
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadDir + "/");
  }
}
