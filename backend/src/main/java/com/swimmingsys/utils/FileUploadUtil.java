package com.swimmingsys.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Slf4j
public class FileUploadUtil {

    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    // 允许的文本格式
    private static final List<String> ALLOWED_TEXT_TYPES = Arrays.asList(
            "txt", "doc", "docx", "pdf", "rtf"
    );

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param uploadDir 上传目录
     * @return 文件保存路径
     * @throws IOException
     */
    public static String uploadFile(MultipartFile file, String uploadDir) throws IOException {
        log.info("开始上传文件，原始文件名: {}", file.getOriginalFilename());
        
        // 验证文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 获取文件扩展名
        String extension = getFileExtension(originalFilename).toLowerCase();

        // 验证文件类型
        if (!isValidFileType(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension + "，仅支持图片和文本格式");
        }

        // 验证文件大小（例如限制为10MB）
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制，最大支持10MB");
        }

        // 创建上传目录
        File uploadDirectory = new File(uploadDir);
        log.info("上传目录路径: {}", uploadDirectory.getAbsolutePath());
        if (!uploadDirectory.exists()) {
            log.info("上传目录不存在，尝试创建: {}", uploadDirectory.getAbsolutePath());
            if (!uploadDirectory.mkdirs()) {
                log.warn("使用mkdirs()创建上传目录失败，尝试递归创建: {}", uploadDirectory.getAbsolutePath());
                // 如果创建失败，尝试递归创建
                ensureDirectoryExists(uploadDirectory);
            } else {
                log.info("上传目录创建成功: {}", uploadDirectory.getAbsolutePath());
            }
        }

        // 生成唯一文件名
        String uniqueFileName = generateUniqueFileName(originalFilename, extension);
        log.info("生成的唯一文件名: {}", uniqueFileName);

        // 构建完整路径
        String filePath = Paths.get(uploadDir, uniqueFileName).toString();
        log.info("完整文件路径: {}", filePath);

        // 确保目录存在
        File destFile = new File(filePath);
        File parentDir = destFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            log.info("目标目录不存在，尝试创建: {}", parentDir.getAbsolutePath());
            if (!parentDir.mkdirs()) {
                log.warn("使用mkdirs()创建目标目录失败，尝试递归创建: {}", parentDir.getAbsolutePath());
                // 如果 mkdirs() 失败，尝试逐级创建目录
                ensureDirectoryExists(parentDir);
            } else {
                log.info("目标目录创建成功: {}", parentDir.getAbsolutePath());
            }
        }

        // 再次确认目标目录存在
        if (parentDir != null && !parentDir.exists()) {
            String errorMsg = "无法创建上传目录: " + parentDir.getAbsolutePath();
            log.error(errorMsg);
            throw new IOException(errorMsg);
        }

        // 保存文件
        log.info("开始保存文件到: {}", destFile.getAbsolutePath());
        try {
            file.transferTo(destFile);
            log.info("文件保存成功: {}", destFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("文件保存失败: {}", e.getMessage(), e);
            throw e;
        }

        return uniqueFileName;
    }

    /**
     * 确保目录存在，使用递归方式创建所有父目录
     *
     * @param dir 要创建的目录
     * @return 是否成功创建
     */
    private static boolean ensureDirectoryExists(File dir) {
        if (dir == null) return false;
        
        if (dir.exists()) {
            return dir.isDirectory();
        }
        
        // 递归创建父目录
        File parent = dir.getParentFile();
        if (parent != null) {
            if (!ensureDirectoryExists(parent)) {
                return false;
            }
        }
        
        // 使用 mkdirs() 创建当前目录及所有必需的父目录
        return dir.mkdirs();
    }

    /**
     * 验证文件类型是否合法
     *
     * @param extension 文件扩展名
     * @return 是否合法
     */
    private static boolean isValidFileType(String extension) {
        return ALLOWED_IMAGE_TYPES.contains(extension) || ALLOWED_TEXT_TYPES.contains(extension);
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 生成唯一文件名
     *
     * @param originalFilename 原始文件名
     * @param extension 文件扩展名
     * @return 唯一文件名
     */
    private static String generateUniqueFileName(String originalFilename, String extension) {
        // 获取当前日期作为目录
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 生成UUID作为文件名
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 组合文件名
        return datePath + "/" + uuid + "." + extension;
    }

    /**
     * 获取文件类型（image或text）
     *
     * @param extension 文件扩展名
     * @return 文件类型
     */
    public static String getFileType(String extension) {
        if (ALLOWED_IMAGE_TYPES.contains(extension.toLowerCase())) {
            return "image";
        } else if (ALLOWED_TEXT_TYPES.contains(extension.toLowerCase())) {
            return "text";
        }
        return "other";
    }
}