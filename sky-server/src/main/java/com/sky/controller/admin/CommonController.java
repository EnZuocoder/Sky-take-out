package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliyunOssOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    @Autowired
    private  AliyunOssOperator aliyunOssOperator;
    @PostMapping("/upload")
    Result upload(MultipartFile file)  {
        log.info("上传文件:{}",file.getOriginalFilename());
        String url = null;
        try {
            url = aliyunOssOperator.upload(file.getBytes(), file.getOriginalFilename());
            return Result.success(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
