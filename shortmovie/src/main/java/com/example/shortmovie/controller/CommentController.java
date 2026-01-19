package com.example.shortmovie.controller;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.shortmovie.config.RateLimit;
import com.example.shortmovie.dto.CommentCreateDTO;
import com.example.shortmovie.service.CommentService;
import com.example.shortmovie.utils.R;
import com.example.shortmovie.vo.CommentDetailVO;
import com.example.shortmovie.vo.CommentVO;
import com.example.shortmovie.vo.PageVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 评论管理控制器
 * 
 * <p>提供视频评论的完整功能，包括：</p>
 * <ul>
 *   <li>发表一级评论和二级回复</li>
 *   <li>删除评论（支持级联删除）</li>
 *   <li>查询视频评论列表（树形结构）</li>
 *   <li>查询用户评论历史</li>
 *   <li>查询单条评论详情</li>
 * </ul>
 * 
 * <p>所有写操作（发表、删除）需要用户认证，查询操作部分需要认证。</p>
 * 
 * @author shortmovie
 * @version 1.0.0
 */
@Tag(name = "评论接口", description = "评论发表、删除、查询相关接口")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Validated
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * 发表评论或回复
     * 
     * <p>用户可以对视频发表一级评论，或对已有评论进行回复（二级评论）。</p>
     * 
     * <p><b>功能特性：</b></p>
     * <ul>
     *   <li>支持一级评论（parentId为null）和二级回复（parentId不为null）</li>
     *   <li>自动进行敏感词过滤，包含敏感词的评论将被拒绝</li>
     *   <li>评论内容长度限制为500字符</li>
     *   <li>评论成功后自动更新视频的评论计数</li>
     *   <li>评论行为会异步发送到Kafka进行数据分析</li>
     *   <li>限流保护：每个用户每分钟最多发表5条评论</li>
     * </ul>
     * 
     * @param dto 评论创建请求，包含视频ID、评论内容和可选的父评论ID
     * @param authentication Spring Security认证信息，自动注入
     * @return 创建成功的评论信息，包含评论ID、用户信息、创建时间等
     */
    @Operation(
        summary = "发表评论/回复", 
        description = "发表视频评论或回复其他评论。支持一级评论和二级回复，自动进行敏感词过滤。需要用户登录认证。",
        tags = {"评论接口"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "评论发表成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = R.class),
                examples = @ExampleObject(
                    name = "成功示例",
                    value = """
                    {
                      "code": 200,
                      "message": "success",
                      "data": {
                        "id": 1001,
                        "userId": 123,
                        "videoId": 456,
                        "parentId": null,
                        "content": "这个视频太棒了！",
                        "userName": "张三",
                        "userAvatar": "https://example.com/avatar.jpg",
                        "likeCount": 0,
                        "isLiked": false,
                        "createTime": "2026-01-19T10:30:00",
                        "replies": []
                      },
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "请求参数错误",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "评论内容为空",
                        value = """
                        {
                          "code": 400,
                          "message": "评论内容不能为空",
                          "data": null,
                          "timestamp": 1737270600000
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "评论内容超长",
                        value = """
                        {
                          "code": 400,
                          "message": "评论内容不能超过500字符",
                          "data": null,
                          "timestamp": 1737270600000
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "包含敏感词",
                        value = """
                        {
                          "code": 400,
                          "message": "评论内容包含敏感词，请修改后重试",
                          "data": null,
                          "timestamp": 1737270600000
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录或token无效",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 401,
                      "message": "用户未登录或token已过期",
                      "data": null,
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "视频或父评论不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 404,
                      "message": "视频不存在",
                      "data": null,
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429", 
            description = "请求过于频繁",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 429,
                      "message": "评论过于频繁，请稍后再试",
                      "data": null,
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        )
    })
    @SecurityRequirement(name = "Authorization")
    @RateLimit(maxRequests = 5, windowSeconds = 60, keyPrefix = "comment")
    @PostMapping
    public R<CommentVO> createComment(
            @Parameter(
                description = "评论创建请求，包含视频ID、评论内容和可选的父评论ID", 
                required = true,
                schema = @Schema(implementation = CommentCreateDTO.class)
            )
            @Valid @RequestBody CommentCreateDTO dto,
            
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        // 从认证信息中获取用户ID
        Long userId = Long.parseLong(authentication.getName());
        
        CommentVO result = commentService.createComment(userId, dto);
        return R.ok(result);
    }
    
    /**
     * 删除评论
     * 
     * <p>用户可以删除自己发表的评论。如果删除的是一级评论（父评论），
     * 系统会自动级联删除该评论下的所有二级回复。</p>
     * 
     * <p><b>功能特性：</b></p>
     * <ul>
     *   <li>只能删除自己发表的评论，不能删除他人评论</li>
     *   <li>删除父评论时自动级联删除所有子回复</li>
     *   <li>使用逻辑删除，数据不会真正从数据库中移除</li>
     *   <li>删除成功后自动更新视频的评论计数</li>
     * </ul>
     * 
     * @param commentId 要删除的评论ID
     * @param authentication Spring Security认证信息，自动注入
     * @return 删除成功的响应
     */
    @Operation(
        summary = "删除评论", 
        description = "删除自己发表的评论。如果是父评论会级联删除所有回复。需要用户登录认证。",
        tags = {"评论接口"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "评论删除成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 200,
                      "message": "success",
                      "data": null,
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录或token无效",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 401,
                      "message": "用户未登录或token已过期",
                      "data": null,
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403", 
            description = "无权限删除他人评论",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 403,
                      "message": "无权删除他人评论",
                      "data": null,
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "评论不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 404,
                      "message": "评论不存在",
                      "data": null,
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        )
    })
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/{commentId}")
    public R<Void> deleteComment(
            @Parameter(
                description = "要删除的评论ID", 
                required = true,
                example = "1001"
            )
            @PathVariable Long commentId,
            
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        // 从认证信息中获取用户ID
        Long userId = Long.parseLong(authentication.getName());
        
        commentService.deleteComment(userId, commentId);
        return R.ok(null);
    }
    
    /**
     * 查询视频评论列表
     * 
     * <p>分页查询指定视频的所有评论，返回树形结构，包含一级评论及其所有回复。</p>
     * 
     * <p><b>功能特性：</b></p>
     * <ul>
     *   <li>返回树形结构，一级评论包含其所有二级回复</li>
     *   <li>评论按创建时间倒序排列（最新的在前）</li>
     *   <li>支持分页查询，默认每页10条</li>
     *   <li>只返回未删除的评论</li>
     *   <li>热门视频的评论列表会被缓存以提升性能</li>
     * </ul>
     * 
     * @param videoId 视频ID
     * @param pageNum 页码，从1开始，默认为1
     * @param pageSize 每页大小，默认为10
     * @return 分页的评论列表，包含总数、页码等分页信息
     */
    @Operation(
        summary = "查询视频评论列表", 
        description = "分页查询视频的所有评论，返回树形结构（一级评论包含其所有回复）。评论按创建时间倒序排列。",
        tags = {"评论接口"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 200,
                      "message": "success",
                      "data": {
                        "pageNum": 1,
                        "pageSize": 10,
                        "total": 25,
                        "pages": 3,
                        "records": [
                          {
                            "id": 1001,
                            "userId": 123,
                            "videoId": 456,
                            "parentId": null,
                            "content": "这个视频太棒了！",
                            "userName": "张三",
                            "userAvatar": "https://example.com/avatar.jpg",
                            "likeCount": 5,
                            "isLiked": false,
                            "createTime": "2026-01-19T10:30:00",
                            "replies": [
                              {
                                "id": 1002,
                                "userId": 124,
                                "videoId": 456,
                                "parentId": 1001,
                                "content": "我也觉得很棒！",
                                "userName": "李四",
                                "userAvatar": "https://example.com/avatar2.jpg",
                                "likeCount": 2,
                                "isLiked": false,
                                "createTime": "2026-01-19T10:35:00",
                                "replies": []
                              }
                            ]
                          }
                        ]
                      },
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "视频不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 404,
                      "message": "视频不存在",
                      "data": null,
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/video/{videoId}")
    public R<PageVO<CommentVO>> getVideoComments(
            @Parameter(
                description = "视频ID", 
                required = true,
                example = "456"
            )
            @PathVariable Long videoId,
            
            @Parameter(
                description = "页码，从1开始",
                example = "1",
                schema = @Schema(type = "integer", minimum = "1", defaultValue = "1")
            )
            @RequestParam(defaultValue = "1") Integer pageNum,
            
            @Parameter(
                description = "每页大小，建议10-50之间",
                example = "10",
                schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10")
            )
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        PageVO<CommentVO> result = commentService.getVideoComments(videoId, pageNum, pageSize);
        return R.ok(result);
    }
    
    /**
     * 查询用户评论历史
     * 
     * <p>查询当前登录用户发表的所有评论，按时间倒序排列。</p>
     * 
     * <p><b>功能特性：</b></p>
     * <ul>
     *   <li>只返回当前登录用户的评论</li>
     *   <li>包含评论关联的视频标题信息</li>
     *   <li>评论按创建时间倒序排列</li>
     *   <li>支持分页查询</li>
     *   <li>自动过滤已删除视频的评论</li>
     * </ul>
     * 
     * @param pageNum 页码，从1开始，默认为1
     * @param pageSize 每页大小，默认为10
     * @param authentication Spring Security认证信息，自动注入
     * @return 分页的用户评论历史列表
     */
    @Operation(
        summary = "查询用户评论历史", 
        description = "查询当前登录用户发表的所有评论，按时间倒序排列。包含评论关联的视频信息。需要用户登录认证。",
        tags = {"评论接口"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 200,
                      "message": "success",
                      "data": {
                        "pageNum": 1,
                        "pageSize": 10,
                        "total": 15,
                        "pages": 2,
                        "records": [
                          {
                            "id": 1001,
                            "userId": 123,
                            "videoId": 456,
                            "videoTitle": "精彩的短视频",
                            "content": "这个视频太棒了！",
                            "userName": "张三",
                            "createTime": "2026-01-19T10:30:00"
                          },
                          {
                            "id": 1002,
                            "userId": 123,
                            "videoId": 457,
                            "videoTitle": "另一个有趣的视频",
                            "content": "非常有意思",
                            "userName": "张三",
                            "createTime": "2026-01-19T09:15:00"
                          }
                        ]
                      },
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "未登录或token无效",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 401,
                      "message": "用户未登录或token已过期",
                      "data": null,
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        )
    })
    @SecurityRequirement(name = "Authorization")
    @GetMapping("/user")
    public R<PageVO<CommentDetailVO>> getUserComments(
            @Parameter(
                description = "页码，从1开始",
                example = "1",
                schema = @Schema(type = "integer", minimum = "1", defaultValue = "1")
            )
            @RequestParam(defaultValue = "1") Integer pageNum,
            
            @Parameter(
                description = "每页大小，建议10-50之间",
                example = "10",
                schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10")
            )
            @RequestParam(defaultValue = "10") Integer pageSize,
            
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        // 从认证信息中获取用户ID
        Long userId = Long.parseLong(authentication.getName());
        
        PageVO<CommentDetailVO> result = commentService.getUserComments(userId, pageNum, pageSize);
        return R.ok(result);
    }
    
    /**
     * 查询评论详情
     * 
     * <p>查询单条评论的详细信息，包含其所有回复。</p>
     * 
     * <p><b>功能特性：</b></p>
     * <ul>
     *   <li>返回评论的完整信息</li>
     *   <li>如果是一级评论，包含其所有二级回复</li>
     *   <li>包含用户信息和点赞数据</li>
     * </ul>
     * 
     * @param commentId 评论ID
     * @return 评论详情，包含回复列表
     */
    @Operation(
        summary = "查询评论详情", 
        description = "查询单条评论的详细信息，包含其所有回复。如果是一级评论，会返回其下的所有二级回复。",
        tags = {"评论接口"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 200,
                      "message": "success",
                      "data": {
                        "id": 1001,
                        "userId": 123,
                        "videoId": 456,
                        "parentId": null,
                        "content": "这个视频太棒了！",
                        "userName": "张三",
                        "userAvatar": "https://example.com/avatar.jpg",
                        "likeCount": 5,
                        "isLiked": false,
                        "createTime": "2026-01-19T10:30:00",
                        "replies": [
                          {
                            "id": 1002,
                            "userId": 124,
                            "videoId": 456,
                            "parentId": 1001,
                            "content": "我也觉得很棒！",
                            "userName": "李四",
                            "userAvatar": "https://example.com/avatar2.jpg",
                            "likeCount": 2,
                            "isLiked": false,
                            "createTime": "2026-01-19T10:35:00",
                            "replies": []
                          }
                        ]
                      },
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "评论不存在",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                      "code": 404,
                      "message": "评论不存在",
                      "data": null,
                      "timestamp": 1737270600000
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/{commentId}")
    public R<CommentVO> getCommentDetail(
            @Parameter(
                description = "评论ID", 
                required = true,
                example = "1001"
            )
            @PathVariable Long commentId
    ) {
        CommentVO result = commentService.getCommentDetail(commentId);
        return R.ok(result);
    }
}
