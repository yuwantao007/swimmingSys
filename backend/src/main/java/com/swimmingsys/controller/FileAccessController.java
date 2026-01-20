package com.swimmingsys.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 文件访问控制器 - 用于提供上传文件的访问服务
 */
@Slf4j
@RestController
@Api(tags = "文件访问")
@RequestMapping("/api/v1/files")
public class FileAccessController {

    /**
     * 获取文章相关的上传文件
     */
    @ApiOperation("获取文章相关的上传文件")
    @GetMapping("/access/{filePath:.+}")
    public ResponseEntity<Resource> getArticleFile(@PathVariable String filePath) {
        return getFileInternal(filePath, "article");
    }
    
    @ApiOperation("获取通用上传文件")
    @GetMapping("/access/{category}/{filePath:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String category, @PathVariable String filePath) {
        return getFileInternal(category + "/" + filePath, category);
    }
    
    /**
     * 内部文件访问方法
     *
     * @param filePath 文件路径
     * @param category 文件类别
     * @return 文件资源
     */
    private ResponseEntity<Resource> getFileInternal(String filePath, String category) {
        try {
            log.info("尝试访问{}文件: {}", category, filePath);
            
            // 构建安全的文件路径
            String userDir = System.getProperty("user.dir");
            log.info("当前工作目录: {}", userDir);

            Path uploadDir;

            if ("article".equals(category)) {
                // 文章资料上传文件存储在 uploads/article/ 目录下
                uploadDir = Paths.get(userDir, "uploads", "article");
            } else {
                // 其他类型文件存储在 uploads/{category}/ 目录下
                uploadDir = Paths.get(userDir, "uploads", category);
            }

            log.info("基础上传目录: {}", uploadDir.toString());

            // 尝试多个可能的路径位置
            List<Path> possiblePaths = new ArrayList<>();

            // 1. 原始路径
            String normalizedFilePath = filePath.replace("/", File.separator).replace("\\", File.separator);
            possiblePaths.add(uploadDir.resolve(normalizedFilePath).normalize());

            // 2. 标准化日期格式路径
            String standardizedPath = standardizeDatePath(filePath);
            String normalizedStandardizedPath = null;
            if (!standardizedPath.equals(filePath)) {
                normalizedStandardizedPath = standardizedPath.replace("/", File.separator).replace("\\", File.separator);
                possiblePaths.add(uploadDir.resolve(normalizedStandardizedPath).normalize());
            }

            // 3. 尝试相对项目根目录的路径（如果应用在子目录运行）
            Path projectRootUploadDir;
            if (userDir.endsWith("backend")) {
                // 如果当前在backend目录下运行，尝试上级目录的uploads
                String parentDir = userDir.substring(0, userDir.lastIndexOf("\\backend"));
                if ("article".equals(category)) {
                    projectRootUploadDir = Paths.get(parentDir, "uploads", "article");
                } else {
                    projectRootUploadDir = Paths.get(parentDir, "uploads", category);
                }
            } else {
                projectRootUploadDir = uploadDir;
            }

            possiblePaths.add(projectRootUploadDir.resolve(normalizedFilePath).normalize());

            if (!standardizedPath.equals(filePath) && normalizedStandardizedPath != null) {
                possiblePaths.add(projectRootUploadDir.resolve(normalizedStandardizedPath).normalize());
            }

            File file = null;
            Path foundPath = null;

            for (Path path : possiblePaths) {
                file = path.toFile();
                log.info("尝试路径: {}", path.toString());

                // 确保请求的文件在上传目录内（防止路径遍历攻击）
                if (path.startsWith(uploadDir.normalize()) || path.startsWith(projectRootUploadDir.normalize())) {
                    if (file.exists() && file.isFile()) {
                        log.info("在路径 {} 找到文件", path.toString());
                        foundPath = path;
                        break;
                    }
                } else {
                    log.warn("路径不在允许的目录范围内: {}", path.toString());
                }
            }

            if (file == null || !file.exists() || !file.isFile()) {
                log.warn("在所有可能的路径中都未找到文件");

                // 最后尝试在当前工作目录的各级子目录中查找
                Path tempPath = Paths.get("uploads", "article", normalizedFilePath);
                File tempFile = tempPath.toFile();
                if (tempFile.exists() && tempFile.isFile()) {
                    file = tempFile;
                    foundPath = tempPath;
                    log.info("在相对路径 {} 找到文件", tempPath.toString());
                } else {
                    // 尝试标准化格式
                    tempPath = Paths.get("uploads", "article", standardizedPath.replace("/", File.separator));
                    tempFile = tempPath.toFile();
                    if (tempFile.exists() && tempFile.isFile()) {
                        file = tempFile;
                        foundPath = tempPath;
                        log.info("在相对路径 {} 找到文件", tempPath.toString());
                    }
                }
            }

            if (file == null || !file.exists() || !file.isFile()) {
                log.warn("请求的文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // 根据文件扩展名确定内容类型
            String contentType = Files.probeContentType(foundPath);
            if (contentType == null) {
                // 根据文件扩展名手动设置内容类型
                String fileExtension = getFileExtension(file.getName()).toLowerCase();
                contentType = mapExtensionToMediaType(fileExtension);
                log.info("使用手动映射的内容类型: {}", contentType);
            } else {
                log.info("探测到的内容类型: {}", contentType);
            }

            // 创建资源
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            log.info("成功返回文件: {}", file.getName());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("获取文件时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 标准化日期路径格式，处理可能的日期格式不匹配问题
     * 例如：将 2026/01/20 转换为 2026/1/20
     *
     * @param filePath 文件路径
     * @return 标准化后的路径
     */
    private String standardizeDatePath(String filePath) {
        // 正则表达式匹配日期路径格式，如 2026/01/20 或 2026/1/20
        // 匹配格式：四位数字/两位或一位数字/两位或一位数字/
        Pattern datePattern = Pattern.compile("(\\d{4})/(0?\\d+)/0?(\\d+)/");
        Matcher matcher = datePattern.matcher(filePath);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String year = matcher.group(1);
            String month = String.valueOf(Integer.parseInt(matcher.group(2))); // 移除前导零
            String day = String.valueOf(Integer.parseInt(matcher.group(3)));   // 移除前导零
            
            matcher.appendReplacement(result, year + "/" + month + "/" + day + "/");
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * 根据文件扩展名映射到媒体类型
     *
     * @param extension 文件扩展名
     * @return 媒体类型
     */
    private String mapExtensionToMediaType(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG_VALUE;
            case "png":
                return MediaType.IMAGE_PNG_VALUE;
            case "gif":
                return MediaType.IMAGE_GIF_VALUE;
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            case "txt":
                return MediaType.TEXT_PLAIN_VALUE;
            case "pdf":
                return MediaType.APPLICATION_PDF_VALUE;
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}