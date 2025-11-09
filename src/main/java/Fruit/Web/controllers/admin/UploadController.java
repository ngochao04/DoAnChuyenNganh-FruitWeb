// src/main/java/Fruit/Web/controllers/admin/UploadApiController.java
package Fruit.Web.controllers.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin/uploads")
public class UploadController {

  @Value("${app.upload.dir:uploads}")
  private String uploadDir;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String,Object> upload(@RequestParam("file") MultipartFile file) throws Exception {
    if (file == null || file.isEmpty()) throw new IllegalArgumentException("File rỗng");
    String ct = Optional.ofNullable(file.getContentType()).orElse("");
    if (!ct.startsWith("image/")) throw new IllegalArgumentException("Chỉ nhận ảnh");

    // sub-folder theo ngày cho gọn
    String ymd = LocalDate.now().toString();           // 2025-11-08
    Path root = Paths.get(uploadDir).resolve(ymd);
    Files.createDirectories(root);

    String ext = Optional.ofNullable(StringUtils.getFilenameExtension(file.getOriginalFilename()))
        .map(s -> "." + s.toLowerCase()).orElse("");
    String name = UUID.randomUUID().toString().replace("-", "") + ext;

    Path dest = root.resolve(name).normalize();
    file.transferTo(dest);

    // URL public
    String url = "/uploads/" + ymd + "/" + name;

    Map<String, Object> res = new LinkedHashMap<>();
    res.put("url", url);
    res.put("filename", file.getOriginalFilename());
    res.put("size", file.getSize());
    res.put("contentType", ct);
    return res;
  }
}
