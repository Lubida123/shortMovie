package com.example.shortmovie.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.shortmovie.dto.PlayRecordDTO;
import com.example.shortmovie.dto.VideoUploadDTO;
import com.example.shortmovie.service.CollectService;
import com.example.shortmovie.service.InteractionService;
import com.example.shortmovie.service.LikeService;
import com.example.shortmovie.service.VideoService;
import com.example.shortmovie.utils.R;
import com.example.shortmovie.vo.CollectStatusVO;
import com.example.shortmovie.vo.LikeStatusVO;
import com.example.shortmovie.vo.PageVO;
import com.example.shortmovie.vo.VideoDetailVO;
import com.example.shortmovie.vo.VideoInteractionVO;
import com.example.shortmovie.vo.VideoUploadVO;
import com.example.shortmovie.vo.VideoVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 视频管理控制器
 */
@Tag(name = "视频接口")
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {
    
    private final VideoService videoService;
    private final LikeService likeService;
    private final CollectService collectService;
    private final InteractionService interactionService;
    
    @Operation(summary = "上传视频", description = "上传视频文件并创建视频记录")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
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
        // 从认证信息中获取用户ID（如果未认证，使用测试用户ID 1）
        Long userId = 2302L;  // 默认测试用户
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                userId = Long.parseLong(authentication.getName());
            } catch (Exception e) {
                // 使用默认测试用户
            }
        }
        
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
    
    @Operation(summary = "点赞/取消点赞", description = "切换视频的点赞状态，需要用户登录")
    @PostMapping("/{videoId}/like")
    public R<LikeStatusVO> toggleLike(
            @Parameter(description = "视频ID", required = true)
            @PathVariable Long videoId,
            
            Authentication authentication
    ) {
        // 从认证信息中获取用户ID
        Long userId = Long.parseLong(authentication.getName());
        
        LikeStatusVO result = likeService.toggleLike(userId, videoId);
        return R.ok(result);
    }
    
    @Operation(summary = "收藏/取消收藏", description = "切换视频的收藏状态，需要用户登录")
    @PostMapping("/{videoId}/collect")
    public R<CollectStatusVO> toggleCollect(
            @Parameter(description = "视频ID", required = true)
            @PathVariable Long videoId,
            
            Authentication authentication
    ) {
        // 从认证信息中获取用户ID
        Long userId = Long.parseLong(authentication.getName());
        
        CollectStatusVO result = collectService.toggleCollect(userId, videoId);
        return R.ok(result);
    }
    
    @Operation(summary = "查询交互状态", description = "查询用户对视频的点赞和收藏状态，支持未登录用户访问")
    @GetMapping("/{videoId}/interaction")
    public R<VideoInteractionVO> getInteractionStatus(
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
        
        VideoInteractionVO result = interactionService.getInteractionStatus(userId, videoId);
        return R.ok(result);
    }
}
