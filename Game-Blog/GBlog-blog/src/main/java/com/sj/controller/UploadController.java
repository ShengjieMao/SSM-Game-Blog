package com.sj.controller;

import com.sj.domain.ResponseResult;
import com.sj.service.UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
public class UploadController {

    @Resource
    private UploadService uploadService;

    @PostMapping("/upload")
    public ResponseResult uploadImg(MultipartFile img){
       return uploadService.uploadImg(img);
    }
}