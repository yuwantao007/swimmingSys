package com.swimmingsys.controller;

import com.swimmingsys.common.Result;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.annotation.AuthCheck;
import com.swimmingsys.utils.FileUploadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文章资料文件上传控制器
 */
@Api(tags = "文章资料文件上传")
@RestController
@RequestMapping("/api/v1/articles/files")
@Slf4j
public class ArticleFileController {

    // 上传目录
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/article/";

    /**
     * 上传文件（仅管理员）
     *
     * @param file 上传的文件
     * @return 统一响应结果
     */
    @ApiOperation("上传文件（仅管理员，仅支持图片和文本格式）")
    @PostMapping("/upload")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("收到文件上传请求，文件名: {}, 文件大小: {}", file.getOriginalFilename(), file.getSize());
            
            // 验证文件是否为空
            if (file.isEmpty()) {
                log.warn("上传的文件为空");
                return Result.error("上传的文件不能为空");
            }

            // 上传文件
            String filePath = FileUploadUtil.uploadFile(file, UPLOAD_DIR);
            log.info("文件上传成功，保存路径: {}", filePath);

            return Result.success("上传成功", filePath);
        } catch (IOException e) {
            log.error("IO异常导致文件上传失败: {}", e.getMessage(), e);
            return Result.error("文件上传失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("非法参数导致文件上传失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("未知异常导致文件上传失败: {}", e.getMessage(), e);
            return Result.error("文件上传过程中发生错误: " + e.getMessage());
        }
    }
}