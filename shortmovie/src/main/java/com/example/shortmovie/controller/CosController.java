package com.example.shortmovie.controller;

import com.example.shortmovie.utils.TencentCosUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cos")
public class CosController {

    @Autowired
    private TencentCosUtil cosUtil;

    /**
     * 暴露的文件上传接口，用于测试,实际实现在TencentCosStoragesService
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            String fileUrl = cosUtil.uploadFile(file);
            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("data", fileUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "上传失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 文件删除接口
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam("filePath") String filePath) {
        Map<String, Object> result = new HashMap<>();
        try {
            cosUtil.deleteFile(filePath);
            result.put("code", 200);
            result.put("msg", "删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}