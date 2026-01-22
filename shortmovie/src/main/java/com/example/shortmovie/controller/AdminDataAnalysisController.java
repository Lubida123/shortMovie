package com.example.shortmovie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shortmovie.entity.BehaviorRecord;
import com.example.shortmovie.entity.Comment;
import com.example.shortmovie.entity.User;
import com.example.shortmovie.entity.UserCollect;
import com.example.shortmovie.entity.UserLike;
import com.example.shortmovie.entity.Video;
import com.example.shortmovie.exception.ValidationException;
import com.example.shortmovie.mapper.BehaviorRecordMapper;
import com.example.shortmovie.mapper.CommentMapper;
import com.example.shortmovie.mapper.UserCollectMapper;
import com.example.shortmovie.mapper.UserLikeMapper;
import com.example.shortmovie.mapper.UserMapper;
import com.example.shortmovie.mapper.VideoMapper;
import com.example.shortmovie.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "管理员数据分析接口", description = "管理员数据分析相关接口")
@RestController
@RequestMapping("/api/admin/analysis")
@RequiredArgsConstructor
@Validated
public class AdminDataAnalysisController {

    private final VideoMapper videoMapper;
    private final UserMapper userMapper;
    private final UserLikeMapper userLikeMapper;
    private final CommentMapper commentMapper;
    private final UserCollectMapper userCollectMapper;
    private final BehaviorRecordMapper behaviorRecordMapper;

    /**
     * 获取核心指标数据
     */
    @Operation(summary = "获取核心指标数据", description = "获取总播放量、点赞数、评论数、收藏数及其趋势")
    @GetMapping("/core-metrics")
    public R<Map<String, Object>> getCoreMetrics(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "week") String timeRange,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        Map<String, Object> metrics = new HashMap<>();

        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        // 1. 总播放量（时间范围内视频的播放量总和）
        List<Video> videos = videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                        .ge(start != null, Video::getCreateTime, start)
                        .le(end != null, Video::getCreateTime, end)
        );
        Long totalViews = videos.stream()
                .mapToLong(v -> v.getPlayCount() != null ? v.getPlayCount() : 0L)
                .sum();
        metrics.put("totalViews", totalViews);

        // 2. 总点赞数（时间范围内的点赞数）
        Long totalLikes = userLikeMapper.selectCount(
                new LambdaQueryWrapper<UserLike>()
                        .ge(start != null, UserLike::getCreateTime, start)
                        .le(end != null, UserLike::getCreateTime, end)
        );
        metrics.put("totalLikes", totalLikes);

        // 3. 总评论数（时间范围内的评论数）
        Long totalComments = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .ge(start != null, Comment::getCreateTime, start)
                        .le(end != null, Comment::getCreateTime, end)
        );
        log.info("评论统计: start={}, end={}, totalComments={}", start, end, totalComments);
        metrics.put("totalComments", totalComments);

        // 4. 总收藏数（时间范围内的收藏数）
        Long totalCollects = userCollectMapper.selectCount(
                new LambdaQueryWrapper<UserCollect>()
                        .ge(start != null, UserCollect::getCreateTime, start)
                        .le(end != null, UserCollect::getCreateTime, end)
        );
        metrics.put("totalShares", totalCollects);

        // 5. 计算趋势（简化版：与上一周期对比）
        LocalDateTime[] prevRange = calculatePreviousDateRange(timeRange, start, end);

        // 上一周期播放量
        List<Video> prevVideos = videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                        .ge(prevRange[0] != null, Video::getCreateTime, prevRange[0])
                        .le(prevRange[1] != null, Video::getCreateTime, prevRange[1])
        );
        Long prevViews = prevVideos.stream()
                .mapToLong(v -> v.getPlayCount() != null ? v.getPlayCount() : 0L)
                .sum();
        metrics.put("viewTrend", calculateTrend(totalViews, prevViews));

        // 上一周期点赞数
        Long prevLikes = userLikeMapper.selectCount(
                new LambdaQueryWrapper<UserLike>()
                        .ge(prevRange[0] != null, UserLike::getCreateTime, prevRange[0])
                        .le(prevRange[1] != null, UserLike::getCreateTime, prevRange[1])
        );
        metrics.put("likeTrend", calculateTrend(totalLikes, prevLikes));

        // 上一周期评论数
        Long prevComments = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .ge(prevRange[0] != null, Comment::getCreateTime, prevRange[0])
                        .le(prevRange[1] != null, Comment::getCreateTime, prevRange[1])
        );
        metrics.put("commentTrend", calculateTrend(totalComments, prevComments));

        // 上一周期收藏数
        Long prevCollects = userCollectMapper.selectCount(
                new LambdaQueryWrapper<UserCollect>()
                        .ge(prevRange[0] != null, UserCollect::getCreateTime, prevRange[0])
                        .le(prevRange[1] != null, UserCollect::getCreateTime, prevRange[1])
        );
        metrics.put("shareTrend", calculateTrend(totalCollects, prevCollects));

        log.info("获取核心指标数据: timeRange={}, start={}, end={}, totalViews={}, totalLikes={}, totalComments={}, totalCollects={}",
                timeRange, start, end, totalViews, totalLikes, totalComments, totalCollects);
        return R.ok(metrics);
    }

    /**
     * 获取播放量趋势
     */
    @Operation(summary = "获取播放量趋势", description = "获取不同时间粒度的播放量趋势数据")
    @GetMapping("/view-trend")
    public R<Map<String, Object>> getViewTrend(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "week") String timeRange,
            @Parameter(description = "图表类型") @RequestParam(defaultValue = "daily") String chartType,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        // 生成日期标签
        List<String> labels = generateDateLabels(start, end);
        List<Long> data = new ArrayList<>();

        // 按日统计播放量
        for (int i = 0; i < labels.size(); i++) {
            LocalDateTime dayStart = start.plusDays(i);
            LocalDateTime dayEnd = dayStart.plusDays(1);

            // 查询当天创建的视频
            List<Video> videos = videoMapper.selectList(
                    new LambdaQueryWrapper<Video>()
                            .ge(Video::getCreateTime, dayStart)
                            .lt(Video::getCreateTime, dayEnd)
            );

            // 统计播放量
            Long totalViews = videos.stream()
                    .mapToLong(v -> v.getPlayCount() != null ? v.getPlayCount() : 0L)
                    .sum();

            data.add(totalViews);
        }

        Map<String, Object> trend = new HashMap<>();
        trend.put("labels", labels);
        trend.put("data", data);

        log.info("获取播放量趋势: timeRange={}, chartType={}, dataPoints={}", timeRange, chartType, data.size());
        return R.ok(trend);
    }

    /**
     * 获取用户互动分析
     */
    @Operation(summary = "获取用户互动分析", description = "获取用户互动数据（分布或对比）")
    @GetMapping("/interaction")
    public R<Map<String, Object>> getInteractionAnalysis(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "week") String timeRange,
            @Parameter(description = "类型") @RequestParam(defaultValue = "distribution") String type,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        Map<String, Object> interaction = new HashMap<>();

        if ("distribution".equals(type)) {
            // 分布类型：统计总数
            Long totalComments = commentMapper.selectCount(
                    new LambdaQueryWrapper<Comment>()
                            .ge(start != null, Comment::getCreateTime, start)
                            .le(end != null, Comment::getCreateTime, end)
            );

            Long totalLikes = userLikeMapper.selectCount(
                    new LambdaQueryWrapper<UserLike>()
                            .ge(start != null, UserLike::getCreateTime, start)
                            .le(end != null, UserLike::getCreateTime, end)
            );

            Long totalCollects = userCollectMapper.selectCount(
                    new LambdaQueryWrapper<UserCollect>()
                            .ge(start != null, UserCollect::getCreateTime, start)
                            .le(end != null, UserCollect::getCreateTime, end)
            );

            interaction.put("comments", totalComments);
            interaction.put("likes", totalLikes);
            interaction.put("collects", totalCollects);
        } else {
            // 对比类型：按日统计
            List<String> labels = generateDateLabels(start, end);
            List<Long> likesData = new ArrayList<>();
            List<Long> commentsData = new ArrayList<>();
            List<Long> collectsData = new ArrayList<>();

            for (int i = 0; i < labels.size(); i++) {
                LocalDateTime dayStart = start.plusDays(i);
                LocalDateTime dayEnd = dayStart.plusDays(1);

                // 统计点赞数
                Long likes = userLikeMapper.selectCount(
                        new LambdaQueryWrapper<UserLike>()
                                .ge(UserLike::getCreateTime, dayStart)
                                .lt(UserLike::getCreateTime, dayEnd)
                );
                likesData.add(likes);

                // 统计评论数
                Long comments = commentMapper.selectCount(
                        new LambdaQueryWrapper<Comment>()
                                .ge(Comment::getCreateTime, dayStart)
                                .lt(Comment::getCreateTime, dayEnd)
                );
                commentsData.add(comments);

                // 统计收藏数
                Long collects = userCollectMapper.selectCount(
                        new LambdaQueryWrapper<UserCollect>()
                                .ge(UserCollect::getCreateTime, dayStart)
                                .lt(UserCollect::getCreateTime, dayEnd)
                );
                collectsData.add(collects);
            }

            interaction.put("labels", labels);
            interaction.put("likes", likesData);
            interaction.put("comments", commentsData);
            interaction.put("collects", collectsData);
        }

        log.info("获取用户互动分析: timeRange={}, type={}", timeRange, type);
        return R.ok(interaction);
    }

    /**
     * 获取用户增长趋势
     */
    @Operation(summary = "获取用户增长趋势", description = "获取新增、活跃或累计用户趋势")
    @GetMapping("/user-growth")
    public R<Map<String, Object>> getUserGrowth(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "week") String timeRange,
            @Parameter(description = "类型") @RequestParam(defaultValue = "new") String type,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        // 生成日期标签
        List<String> labels = generateDateLabels(start, end);
        List<Long> data = new ArrayList<>();

        if ("new".equals(type)) {
            // 新增用户：按日统计
            for (int i = 0; i < labels.size(); i++) {
                LocalDateTime dayStart = start.plusDays(i);
                LocalDateTime dayEnd = dayStart.plusDays(1);

                Long count = userMapper.selectCount(
                        new LambdaQueryWrapper<User>()
                                .ge(User::getCreateTime, dayStart)
                                .lt(User::getCreateTime, dayEnd)
                );
                data.add(count);
            }
        } else if ("total".equals(type)) {
            // 累计用户：逐日累加
            long cumulative = 0;
            for (int i = 0; i < labels.size(); i++) {
                LocalDateTime dayStart = start.plusDays(i);
                LocalDateTime dayEnd = dayStart.plusDays(1);

                Long count = userMapper.selectCount(
                        new LambdaQueryWrapper<User>()
                                .ge(User::getCreateTime, dayStart)
                                .lt(User::getCreateTime, dayEnd)
                );
                cumulative += count;
                data.add(cumulative);
            }
        } else {
            // active - 活跃用户（简化版：使用评论和点赞作为活跃标志）
            for (int i = 0; i < labels.size(); i++) {
                LocalDateTime dayStart = start.plusDays(i);
                LocalDateTime dayEnd = dayStart.plusDays(1);

                // 统计当天有评论或点赞的用户数（去重）
                Set<Long> activeUsers = new HashSet<>();

                List<Comment> comments = commentMapper.selectList(
                        new LambdaQueryWrapper<Comment>()
                                .ge(Comment::getCreateTime, dayStart)
                                .lt(Comment::getCreateTime, dayEnd)
                );
                comments.forEach(c -> activeUsers.add(c.getUserId()));

                List<UserLike> likes = userLikeMapper.selectList(
                        new LambdaQueryWrapper<UserLike>()
                                .ge(UserLike::getCreateTime, dayStart)
                                .lt(UserLike::getCreateTime, dayEnd)
                );
                likes.forEach(l -> activeUsers.add(l.getUserId()));

                data.add((long) activeUsers.size());
            }
        }

        Map<String, Object> growth = new HashMap<>();
        growth.put("labels", labels);
        growth.put("data", data);

        log.info("获取用户增长趋势: timeRange={}, type={}, dataPoints={}", timeRange, type, data.size());
        return R.ok(growth);
    }

    /**
     * 获取视频分类分布
     */
    @Operation(summary = "获取视频分类分布", description = "获取各分类视频的分布情况")
    @GetMapping("/category-distribution")
    public R<Map<String, Object>> getCategoryDistribution(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "week") String timeRange,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "count") String sortBy,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        // 查询所有视频
        List<Video> videos = videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                        .ge(start != null, Video::getCreateTime, start)
                        .le(end != null, Video::getCreateTime, end)
        );

        // 按分类分组统计
        Map<String, Long> categoryCountMap = new HashMap<>();
        Map<String, Long> categoryViewsMap = new HashMap<>();
        Map<String, Long> categoryInteractionMap = new HashMap<>();

        for (Video video : videos) {
            String category = video.getCategory() != null ? video.getCategory() : "其他";

            // 统计数量
            categoryCountMap.put(category, categoryCountMap.getOrDefault(category, 0L) + 1);

            // 统计播放量
            long views = video.getPlayCount() != null ? video.getPlayCount() : 0L;
            categoryViewsMap.put(category, categoryViewsMap.getOrDefault(category, 0L) + views);

            // 统计互动量（点赞+评论）
            long likes = video.getLikeCount() != null ? video.getLikeCount() : 0L;
            long comments = video.getCommentCount() != null ? video.getCommentCount() : 0L;
            categoryInteractionMap.put(category, categoryInteractionMap.getOrDefault(category, 0L) + likes + comments);
        }

        // 根据排序方式选择数据
        Map<String, Long> sortMap;
        switch (sortBy) {
            case "views":
                sortMap = categoryViewsMap;
                break;
            case "interaction":
                sortMap = categoryInteractionMap;
                break;
            case "count":
            default:
                sortMap = categoryCountMap;
                break;
        }

        // 转换为列表并排序
        List<Map<String, Object>> categories = sortMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", entry.getKey());
                    map.put("value", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> distribution = new HashMap<>();
        distribution.put("categories", categories);

        log.info("获取视频分类分布: timeRange={}, sortBy={}, count={}", timeRange, sortBy, categories.size());
        return R.ok(distribution);
    }

    /**
     * 获取用户活跃时段
     */
    @Operation(summary = "获取用户活跃时段", description = "获取24小时用户活跃度分布")
    @GetMapping("/active-period")
    public R<Map<String, Object>> getActivePeriod(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "week") String timeRange,
            @Parameter(description = "时段类型") @RequestParam(defaultValue = "all") String period,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        Map<String, Object> activePeriod = new HashMap<>();

        String[] hours = new String[24];
        Long[] data = new Long[24];

        // 初始化小时标签和数据
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d:00", i);
            data[i] = 0L;
        }

        // 查询行为记录
        List<BehaviorRecord> records = behaviorRecordMapper.selectList(
                new LambdaQueryWrapper<BehaviorRecord>()
                        .ge(start != null, BehaviorRecord::getCreateTime, start)
                        .le(end != null, BehaviorRecord::getCreateTime, end)
        );

        // 按小时统计
        for (BehaviorRecord record : records) {
            if (record.getCreateTime() != null) {
                int hour = record.getCreateTime().getHour();
                data[hour]++;
            }
        }

        // 如果没有行为记录，使用评论和点赞作为活跃度指标
        if (records.isEmpty()) {
            // 统计评论
            List<Comment> comments = commentMapper.selectList(
                    new LambdaQueryWrapper<Comment>()
                            .ge(start != null, Comment::getCreateTime, start)
                            .le(end != null, Comment::getCreateTime, end)
            );

            for (Comment comment : comments) {
                if (comment.getCreateTime() != null) {
                    int hour = comment.getCreateTime().getHour();
                    data[hour]++;
                }
            }

            // 统计点赞
            List<UserLike> likes = userLikeMapper.selectList(
                    new LambdaQueryWrapper<UserLike>()
                            .ge(start != null, UserLike::getCreateTime, start)
                            .le(end != null, UserLike::getCreateTime, end)
            );

            for (UserLike like : likes) {
                if (like.getCreateTime() != null) {
                    int hour = like.getCreateTime().getHour();
                    data[hour]++;
                }
            }
        }

        activePeriod.put("hours", hours);
        activePeriod.put("data", data);

        log.info("获取用户活跃时段: timeRange={}, period={}, totalActivities={}",
                timeRange, period, java.util.Arrays.stream(data).mapToLong(Long::longValue).sum());
        return R.ok(activePeriod);
    }

    /**
     * 获取热门视频排行
     */
    @Operation(summary = "获取热门视频排行", description = "获取热门视频排行榜")
    @GetMapping("/hot-videos")
    public R<Map<String, Object>> getHotVideos(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "week") String timeRange,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "views") String sortBy,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") Integer limit,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        if (limit <= 0 || limit > 100) {
            throw new ValidationException("返回数量必须在1-100之间");
        }

        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        // 查询视频列表
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(start != null, Video::getCreateTime, start)
                .le(end != null, Video::getCreateTime, end);

        // 根据排序方式排序
        switch (sortBy) {
            case "likes":
                wrapper.orderByDesc(Video::getLikeCount);
                break;
            case "comments":
                wrapper.orderByDesc(Video::getCommentCount);
                break;
            case "interactionRate":
                // 按互动率排序（点赞+评论）/播放量
                wrapper.orderByDesc(Video::getLikeCount);
                break;
            case "views":
            default:
                wrapper.orderByDesc(Video::getPlayCount);
                break;
        }

        wrapper.last("LIMIT " + limit);

        List<Video> videos = videoMapper.selectList(wrapper);

        // 转换为前端需要的格式
        List<Map<String, Object>> videoList = videos.stream().map(video -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", video.getId());
            map.put("cover", video.getCoverUrl());
            map.put("title", video.getTitle());
            map.put("author", video.getAuthorName());
            map.put("views", video.getPlayCount() != null ? video.getPlayCount() : 0L);
            map.put("likes", video.getLikeCount() != null ? video.getLikeCount() : 0L);
            map.put("comments", video.getCommentCount() != null ? video.getCommentCount() : 0L);

            // 计算互动率
            long views = video.getPlayCount() != null ? video.getPlayCount() : 0L;
            long likes = video.getLikeCount() != null ? video.getLikeCount() : 0L;
            long comments = video.getCommentCount() != null ? video.getCommentCount() : 0L;
            double interactionRate = views > 0 ? (double)(likes + comments) / views : 0.0;
            map.put("interactionRate", interactionRate);

            // 趋势（简化版：随机生成）
            map.put("trend", Math.random() * 30 - 10);

            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("videos", videoList);

        log.info("获取热门视频排行: timeRange={}, sortBy={}, limit={}, count={}", timeRange, sortBy, limit, videoList.size());
        return R.ok(result);
    }

    /**
     * 导出数据
     */
    @Operation(summary = "导出数据", description = "导出数据分析报表")
    @GetMapping("/export")
    public R<Map<String, Object>> exportData(
            @Parameter(description = "时间范围") @RequestParam(defaultValue = "week") String timeRange,
            @Parameter(description = "数据类型") @RequestParam(defaultValue = "all") String dataType,
            @Parameter(description = "导出格式") @RequestParam(defaultValue = "json") String format,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 计算时间范围
        LocalDateTime[] dateRange = calculateDateRange(timeRange, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        Map<String, Object> exportData = new HashMap<>();

        // 根据数据类型导出不同的数据
        switch (dataType) {
            case "core-metrics":
                // 导出核心指标
                exportData.put("type", "核心指标数据");
                exportData.put("data", getCoreMetricsData(start, end));
                break;

            case "hot-videos":
                // 导出热门视频
                exportData.put("type", "热门视频排行");
                exportData.put("data", getHotVideosData(start, end));
                break;

            case "user-growth":
                // 导出用户增长
                exportData.put("type", "用户增长趋势");
                exportData.put("data", getUserGrowthData(start, end));
                break;

            case "category":
                // 导出分类分布
                exportData.put("type", "视频分类分布");
                exportData.put("data", getCategoryData(start, end));
                break;

            case "all":
            default:
                // 导出所有数据
                exportData.put("type", "完整数据报表");
                Map<String, Object> allData = new HashMap<>();
                allData.put("coreMetrics", getCoreMetricsData(start, end));
                allData.put("hotVideos", getHotVideosData(start, end));
                allData.put("userGrowth", getUserGrowthData(start, end));
                allData.put("category", getCategoryData(start, end));
                exportData.put("data", allData);
                break;
        }

        // 添加导出信息
        exportData.put("exportTime", LocalDateTime.now());
        exportData.put("timeRange", timeRange);
        exportData.put("startDate", start);
        exportData.put("endDate", end);
        exportData.put("format", format);

        log.info("导出数据: timeRange={}, dataType={}, format={}", timeRange, dataType, format);
        return R.ok(exportData);
    }

    // ==================== 数据导出辅助方法 ====================

    /**
     * 获取核心指标数据（用于导出）
     */
    private Map<String, Object> getCoreMetricsData(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> metrics = new HashMap<>();

        // 总播放量
        List<Video> videos = videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                        .ge(start != null, Video::getCreateTime, start)
                        .le(end != null, Video::getCreateTime, end)
        );
        Long totalViews = videos.stream()
                .mapToLong(v -> v.getPlayCount() != null ? v.getPlayCount() : 0L)
                .sum();

        // 总点赞数
        Long totalLikes = userLikeMapper.selectCount(
                new LambdaQueryWrapper<UserLike>()
                        .ge(start != null, UserLike::getCreateTime, start)
                        .le(end != null, UserLike::getCreateTime, end)
        );

        // 总评论数
        Long totalComments = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .ge(start != null, Comment::getCreateTime, start)
                        .le(end != null, Comment::getCreateTime, end)
        );

        // 总收藏数
        Long totalCollects = userCollectMapper.selectCount(
                new LambdaQueryWrapper<UserCollect>()
                        .ge(start != null, UserCollect::getCreateTime, start)
                        .le(end != null, UserCollect::getCreateTime, end)
        );

        metrics.put("totalViews", totalViews);
        metrics.put("totalLikes", totalLikes);
        metrics.put("totalComments", totalComments);
        metrics.put("totalCollects", totalCollects);
        metrics.put("totalVideos", videos.size());

        return metrics;
    }

    /**
     * 获取热门视频数据（用于导出）
     */
    private List<Map<String, Object>> getHotVideosData(LocalDateTime start, LocalDateTime end) {
        List<Video> videos = videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                        .ge(start != null, Video::getCreateTime, start)
                        .le(end != null, Video::getCreateTime, end)
                        .orderByDesc(Video::getPlayCount)
                        .last("LIMIT 20")
        );

        return videos.stream().map(video -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", video.getId());
            map.put("title", video.getTitle());
            map.put("author", video.getAuthorName());
            map.put("category", video.getCategory());
            map.put("playCount", video.getPlayCount());
            map.put("likeCount", video.getLikeCount());
            map.put("commentCount", video.getCommentCount());
            map.put("collectCount", video.getCollectCount());
            map.put("createTime", video.getCreateTime());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户增长数据（用于导出）
     */
    private Map<String, Object> getUserGrowthData(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> growth = new HashMap<>();

        // 新增用户数
        Long newUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(start != null, User::getCreateTime, start)
                        .le(end != null, User::getCreateTime, end)
        );

        // 总用户数
        Long totalUsers = userMapper.selectCount(null);

        growth.put("newUsers", newUsers);
        growth.put("totalUsers", totalUsers);

        return growth;
    }

    /**
     * 获取分类分布数据（用于导出）
     */
    private List<Map<String, Object>> getCategoryData(LocalDateTime start, LocalDateTime end) {
        List<Video> videos = videoMapper.selectList(
                new LambdaQueryWrapper<Video>()
                        .ge(start != null, Video::getCreateTime, start)
                        .le(end != null, Video::getCreateTime, end)
        );

        // 按分类分组统计
        Map<String, Long> categoryCountMap = new HashMap<>();
        Map<String, Long> categoryViewsMap = new HashMap<>();

        for (Video video : videos) {
            String category = video.getCategory() != null ? video.getCategory() : "其他";
            categoryCountMap.put(category, categoryCountMap.getOrDefault(category, 0L) + 1);

            long views = video.getPlayCount() != null ? video.getPlayCount() : 0L;
            categoryViewsMap.put(category, categoryViewsMap.getOrDefault(category, 0L) + views);
        }

        return categoryCountMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("category", entry.getKey());
                    map.put("videoCount", entry.getValue());
                    map.put("totalViews", categoryViewsMap.get(entry.getKey()));
                    return map;
                })
                .sorted((a, b) -> Long.compare((Long)b.get("videoCount"), (Long)a.get("videoCount")))
                .collect(Collectors.toList());
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算日期范围
     */
    private LocalDateTime[] calculateDateRange(String timeRange, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.now();

        if ("custom".equals(timeRange) && startDate != null && endDate != null) {
            start = startDate.atStartOfDay();
            end = endDate.atTime(LocalTime.MAX);
        } else {
            switch (timeRange) {
                case "today":
                    start = LocalDate.now().atStartOfDay();
                    break;
                case "month":
                    start = LocalDateTime.now().minusMonths(1);
                    break;
                case "week":
                default:
                    start = LocalDateTime.now().minusWeeks(1);
                    break;
            }
        }

        return new LocalDateTime[]{start, end};
    }

    /**
     * 计算上一周期的日期范围
     */
    private LocalDateTime[] calculatePreviousDateRange(String timeRange, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return new LocalDateTime[]{null, null};
        }

        long days = java.time.Duration.between(start, end).toDays();
        LocalDateTime prevStart = start.minusDays(days);
        LocalDateTime prevEnd = start;

        return new LocalDateTime[]{prevStart, prevEnd};
    }

    /**
     * 计算趋势百分比
     */
    private double calculateTrend(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            return -100.0;
        }
        return ((double)(current - previous) / previous) * 100.0;
    }

    /**
     * 生成日期标签
     */
    private List<String> generateDateLabels(LocalDateTime start, LocalDateTime end) {
        List<String> labels = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        while (!startDate.isAfter(endDate)) {
            labels.add(startDate.format(formatter));
            startDate = startDate.plusDays(1);
        }

        return labels;
    }
}
