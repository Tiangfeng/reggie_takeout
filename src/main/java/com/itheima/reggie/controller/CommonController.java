package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

// 文件上传与下载
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        log.info(file.toString());

        // 获取文件后缀。
        String originalFilename = file.getOriginalFilename();
        String fileNameSuffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 用UUID重新生成文件名。
        String fileName = UUID.randomUUID() + fileNameSuffix;

        // 创建一个目录对象。
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);
    }

    // 文件下载
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        ServletOutputStream outputStream = null;
        try (FileInputStream inputStream = new FileInputStream(new File(basePath + name))) {
            outputStream = response.getOutputStream();
            response.setContentType("image/jpg");

            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
