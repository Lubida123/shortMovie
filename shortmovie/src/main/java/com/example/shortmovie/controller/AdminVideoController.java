package com.example.shortmovie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.shortmovie.entity.Video;
import com.example.shortmovie.mapper.VideoMapper;
import com.example.shortmovie.utils.R;
import com.example.shortmovie.utils.VideoConverter;
import com.example.shortmovie.vo.AdminVideoVO;
import com.example.shortmovie.vo.VideoStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "管理员视频接口", description = "管理员视频相关接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminVideoController {

    private final VideoMapper videoMapper;

    /**
     * 获取视频列表（管理员视角）
     */
    @Operation(summary = "获取视频列表", description = "获取视频列表，支持搜索和筛选")
    @GetMapping("/video/list")
    public R<Map<String, Object>> getVideoList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词（标题/作者）") @RequestParam(required = false) String keyword,
            @Parameter(description = "审核状态") @RequestParam(required = false) String status,
            @Parameter(description = "分类") @RequestParam(required = false) String category) {

        Page<Video> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Video::getTitle, keyword)
                    .or()
                    .like(Video::getAuthorName, keyword)
            );
        }

        // 状态筛选
        if (StringUtils.hasText(status)) {
            Integer auditStatus = VideoConverter.convertAuditStatusToInt(status);
            wrapper.eq(Video::getAuditStatus, auditStatus);
        }

        // 分类筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(Video::getCategory, category);
        }

        wrapper.orderByDesc(Video::getCreateTime);

        Page<Video> videoPage = videoMapper.selectPage(page, wrapper);
        List<AdminVideoVO> videoVOs = VideoConverter.convertToAdminVideoVOList(videoPage.getRecords());

        Map<String, Object> result = new HashMap<>();
        result.put("list", videoVOs);
        result.put("total", videoPage.getTotal());

        return R.ok(result);
    }

    /**
     * 获取视频统计数据
     */
    @Operation(summary = "获取视频统计数据", description = "获取各状态视频的统计数量")
    @GetMapping("/video/stats")
    public R<VideoStatsVO> getVideoStats() {
        VideoStatsVO stats = new VideoStatsVO();

        // 总视频数
        stats.setTotalVideos(videoMapper.selectCount(null));

        // 待审核
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getAuditStatus, 0);
        stats.setPendingVideos(videoMapper.selectCount(wrapper));

        // 已通过
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getAuditStatus, 1);
        stats.setApprovedVideos(videoMapper.selectCount(wrapper));

        // 已拒绝
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getAuditStatus, 2);
        stats.setRejectedVideos(videoMapper.selectCount(wrapper));

        return R.ok(stats);
    }

    /**
     * 审核通过视频
     */
    @Operation(summary = "审核通过视频", description = "将视频状态设置为已通过")
    @PostMapping("/video/approve/{id}")
    public R<Void> approveVideo(@PathVariable Long id) {
        Video video = videoMapper.selectById(id);
        if (video == null) {
            throw new ValidationException("视频不存在");
        }

        video.setAuditStatus(1); // 已通过
        videoMapper.updateById(video);
        log.info("审核通过视频: id={}, title={}", id, video.getTitle());

        return R.ok();
    }

    /**
     * 审核拒绝视频
     */
    @Operation(summary = "审核拒绝视频", description = "将视频状态设置为已拒绝")
    @PostMapping("/video/reject/{id}")
    public R<Void> rejectVideo(@PathVariable Long id) {
        Video video = videoMapper.selectById(id);
        if (video == null) {
            throw new ValidationException("视频不存在");
        }

        video.setAuditStatus(2); // 已拒绝
        videoMapper.updateById(video);
        log.info("审核拒绝视频: id={}, title={}", id, video.getTitle());

        return R.ok();
    }

    /**
     * 删除视频
     */
    @Operation(summary = "删除视频", description = "删除视频（逻辑删除）")
    @DeleteMapping("/video/delete/{id}")
    public R<Void> deleteVideo(@PathVariable Long id) {
        Video video = videoMapper.selectById(id);
        if (video == null) {
            throw new ValidationException("视频不存在");
        }

        videoMapper.deleteById(id);
        log.info("删除视频: id={}, title={}", id, video.getTitle());

        return R.ok();
    }

    /**
     * 批量审核通过
     */
    @Operation(summary = "批量审核通过", description = "批量将视频状态设置为已通过")
    @PostMapping("/video/batch/approve")
    public R<Void> batchApproveVideos(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Object> idObjects = (List<Object>) params.get("ids");

        if (idObjects == null || idObjects.isEmpty()) {
            throw new ValidationException("视频ID列表不能为空");
        }

        List<Long> ids = idObjects.stream()
                .map(obj -> obj instanceof Integer ? ((Integer) obj).longValue() : (Long) obj)
                .collect(java.util.stream.Collectors.toList());

        for (Long id : ids) {
            Video video = videoMapper.selectById(id);
            if (video != null) {
                video.setAuditStatus(1);
                videoMapper.updateById(video);
            }
        }

        log.info("批量审核通过视频: count={}", ids.size());
        return R.ok();
    }

    /**
     * 批量审核拒绝
     */
    @Operation(summary = "批量审核拒绝", description = "批量将视频状态设置为已拒绝")
    @PostMapping("/video/batch/reject")
    public R<Void> batchRejectVideos(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Object> idObjects = (List<Object>) params.get("ids");

        if (idObjects == null || idObjects.isEmpty()) {
            throw new ValidationException("视频ID列表不能为空");
        }

        List<Long> ids = idObjects.stream()
                .map(obj -> obj instanceof Integer ? ((Integer) obj).longValue() : (Long) obj)
                .collect(java.util.stream.Collectors.toList());

        for (Long id : ids) {
            Video video = videoMapper.selectById(id);
            if (video != null) {
                video.setAuditStatus(2);
                videoMapper.updateById(video);
            }
        }

        log.info("批量审核拒绝视频: count={}", ids.size());
        return R.ok();
    }

    /**
     * 批量删除视频
     */
    @Operation(summary = "批量删除视频", description = "批量删除视频（逻辑删除）")
    @PostMapping("/video/batch/delete")
    public R<Void> batchDeleteVideos(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Object> idObjects = (List<Object>) params.get("ids");

        if (idObjects == null || idObjects.isEmpty()) {
            throw new ValidationException("视频ID列表不能为空");
        }

        List<Long> ids = idObjects.stream()
                .map(obj -> obj instanceof Integer ? ((Integer) obj).longValue() : (Long) obj)
                .collect(java.util.stream.Collectors.toList());

        videoMapper.deleteBatchIds(ids);
        log.info("批量删除视频: count={}", ids.size());

        return R.ok();
    }
}
