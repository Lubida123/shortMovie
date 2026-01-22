package com.example.shortmovie.utils;

import com.example.shortmovie.entity.Video;
import com.example.shortmovie.vo.AdminVideoVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 视频转换工具类
 */
public class VideoConverter {

    /**
     * 将Video实体转换为AdminVideoVO
     */
    public static AdminVideoVO convertToAdminVideoVO(Video video) {
        if (video == null) {
            return null;
        }
        
        AdminVideoVO vo = new AdminVideoVO();
        vo.setId(video.getId());
        vo.setTitle(video.getTitle());
        vo.setCoverUrl(video.getCoverUrl());
        vo.setDuration(video.getDuration());
        vo.setAuthorName(video.getAuthorName());
        vo.setCategory(video.getCategory());
        vo.setViews(video.getPlayCount());
        vo.setLikes(video.getLikeCount());
        vo.setComments(video.getCommentCount());
        vo.setCollects(video.getCollectCount());
        vo.setStatus(convertAuditStatusToString(video.getAuditStatus()));
        vo.setCreateTime(video.getCreateTime());
        
        // 作者头像暂时设置为空，后续可以关联用户表获取
        vo.setAuthorAvatar(null);
        
        return vo;
    }
    
    /**
     * 批量转换Video实体为AdminVideoVO
     */
    public static List<AdminVideoVO> convertToAdminVideoVOList(List<Video> videos) {
        if (videos == null) {
            return null;
        }
        return videos.stream()
                .map(VideoConverter::convertToAdminVideoVO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将审核状态数字转换为字符串
     * 0-待审核 -> pending
     * 1-已通过 -> approved
     * 2-已拒绝 -> rejected
     */
    private static String convertAuditStatusToString(Integer auditStatus) {
        if (auditStatus == null) {
            return "pending";
        }
        switch (auditStatus) {
            case 0:
                return "pending";
            case 1:
                return "approved";
            case 2:
                return "rejected";
            default:
                return "pending";
        }
    }
    
    /**
     * 将审核状态字符串转换为数字
     * pending -> 0
     * approved -> 1
     * rejected -> 2
     */
    public static Integer convertAuditStatusToInt(String status) {
        if (status == null) {
            return 0;
        }
        switch (status.toLowerCase()) {
            case "pending":
                return 0;
            case "approved":
                return 1;
            case "rejected":
                return 2;
            default:
                return 0;
        }
    }
}
