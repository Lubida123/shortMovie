package com.example.shortmovie.controller;

import com.example.shortmovie.dto.PlayRecordDTO;
import com.example.shortmovie.dto.VideoUploadDTO;
import com.example.shortmovie.service.VideoService;
import com.example.shortmovie.utils.R;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoDetailVO;
import com.example.shortmovie.vo.VideoUploadVO;
import com.example.shortmovie.vo.VideoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频管理控制器
 */
@Tag(name = "视频接口")
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {
    
    private final VideoService videoService;
    
    @Operation(summary = "上传视频")
    @PostMapping("/upload")
    public R<VideoUploadVO> uploadVideo(
            @Parameter(description = "视频文件", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "视频标题", required = true)
            @RequestParam("title") String title,
            
            @Parameter(description = "视频描述")
            @RequestParam(value = "description", required = false) String description,
            
            @Parameter(description = "视频分类")
            @RequestParam(value = "category", required = false) String category,
            
            @Parameter(description = "视频标签（逗号分隔）")
            @RequestParam(value = "tags", required = false) String tags,
            
            @Parameter(description = "视频时长（秒）")
            @RequestParam(value = "duration", required = false) Integer duration,
            
            Authentication authentication
    ) {
        // 从认证信息中获取用户ID
        Long userId = Long.parseLong(authentication.getName());
        
        // 构建 DTO
        VideoUploadDTO dto = new VideoUploadDTO();
        dto.setTitle(title);
        dto.setDescription(description);
        dto.setCategory(category);
        dto.setTags(tags);
        dto.setDuration(duration);
        
        // 上传视频
        VideoUploadVO result = videoService.uploadVideo(file, dto, userId);
        
        return R.ok(result);
    }
    
    @Operation(summary = "获取视频列表")
    @GetMapping("/list")
    public R<PageVO<VideoVO>> getVideoList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize,
            
            Authentication authentication
    ) {
        // 获取当前用户ID（可能为null，表示未登录）
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                userId = Long.parseLong(authentication.getName());
            } catch (Exception e) {
                // 未登录或认证信息无效，userId保持为null
            }
        }
        
        PageVO<VideoVO> result = videoService.getVideoList(pageNum, pageSize, userId);
        return R.ok(result);
    }
    
    @Operation(summary = "获取视频详情")
    @GetMapping("/{videoId}")
    public R<VideoDetailVO> getVideoDetail(
            @Parameter(description = "视频ID", required = true)
            @PathVariable Long videoId,
            
            Authentication authentication
    ) {
        // 获取当前用户ID（可能为null，表示未登录）
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                userId = Long.parseLong(authentication.getName());
            } catch (Exception e) {
                // 未登录或认证信息无效，userId保持为null
            }
        }
        
        VideoDetailVO result = videoService.getVideoDetail(videoId, userId);
        return R.ok(result);
    }
    
    @Operation(summary = "记录视频播放")
    @PostMapping("/{videoId}/play")
    public R<Void> recordPlay(
            @Parameter(description = "视频ID", required = true)
            @PathVariable Long videoId,
            
            @Parameter(description = "播放记录信息")
            @RequestBody(required = false) PlayRecordDTO dto
    ) {
        // 增加播放次数
        videoService.incrementPlayCount(videoId);
        
        return R.ok(null);
    }
}
