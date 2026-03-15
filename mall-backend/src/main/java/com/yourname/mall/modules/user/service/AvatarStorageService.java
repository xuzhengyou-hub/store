package com.yourname.mall.modules.user.service;

import com.yourname.mall.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Component
public class AvatarStorageService {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path avatarDir;

    public AvatarStorageService(@Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDir) {
        this.avatarDir = Paths.get(avatarDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.avatarDir);
        } catch (IOException ex) {
            throw new IllegalStateException("初始化头像目录失败", ex);
        }
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择头像图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("头像必须是图片文件");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = avatarDir.resolve(fileName).normalize();

        if (!target.startsWith(avatarDir)) {
            throw new BusinessException("非法文件路径");
        }

        try {
            Files.createDirectories(avatarDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException("保存头像失败，请稍后重试");
        }

        return fileName;
    }

    public Resource load(String fileName) {
        String safeName = Paths.get(fileName).getFileName().toString();
        Path target = avatarDir.resolve(safeName).normalize();
        if (!target.startsWith(avatarDir) || !Files.exists(target)) {
            throw new BusinessException("头像不存在");
        }
        return new PathResource(target);
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "png";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        return SUPPORTED_EXTENSIONS.contains(extension) ? extension : "png";
    }
}
