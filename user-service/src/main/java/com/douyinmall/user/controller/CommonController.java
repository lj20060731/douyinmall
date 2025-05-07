package com.douyinmall.user.controller;


import com.douyinmall.common.result.Result;
import com.douyinmall.common.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")

@Slf4j
public class CommonController {
        @Autowired
        private AliOssUtil aliOssUtil;

        @RequestMapping("/upload")
        public Result<String> upload(MultipartFile file){
                log.info("文件上传：{}",file);
                //获取原始文件名
                String originalFilename = file.getOriginalFilename();
                //获取文件后缀名
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                //使用uuid重新生成文件名，防止文件名称重复造成文件覆盖
                String objectName= UUID.randomUUID().toString()+extension;
                //调用阿里云oss工具类进行文件上传
                String filepath = null;
                try {
                        filepath = aliOssUtil.upload(file.getBytes(), objectName);
                        //返回上传文件的路径
                        return Result.success(filepath);
                } catch (IOException e) {
                        log.error("文件上传失败：{}",e);
                }
                return Result.error("文件上传失败");
        }
}
