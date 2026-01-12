package com.example.shortmovie.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName user_info
 */
@Data
@TableName("user_Info")
public class UserInfo {
    /**
     * 用户ID
     */
    @TableId
    private Integer uid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String userpassword;

    /**
     * 手机号
     */
    private String telephonenum;

    /**
     * 状态：默认0正常， 1冻结
     */
    private Integer status;

    /**
     * 头像地址
     */
    private String avator;

    /**
     * 
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createDate;
}