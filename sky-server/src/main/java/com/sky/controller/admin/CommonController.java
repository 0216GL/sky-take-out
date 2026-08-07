package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/admin/common")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取后缀名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            //构造新文件名称（UUID，防止重名）
            String objectName = UUID.randomUUID().toString() + extension;

            //文件请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            log.info("文件上传成功：{}", filePath);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }
        return Result.error("文件上传失败");
    }
}
