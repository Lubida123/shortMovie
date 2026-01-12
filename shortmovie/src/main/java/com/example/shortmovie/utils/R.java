package com.example.shortmovie.utils;

import lombok.Data;

@Data
public class R {

    static final String SUCCESS = "操作成功！";
    private Object data; //数据属性
    private String msg; //信息属性
    private Integer code; //代码属性

    public R(Object data)
    {
        this(SUCCESS, data, 200);
    }

    public R(String msg, Integer code)
    {
        this(msg, null, code);
    }

    public R(String msg, Object data, Integer code)
    {
        this.msg = msg;
        this.data = data;
        this.code = code;
    }

    /**
     * 返回成功JSON信息
     * @param data
     * @return
     */
    public static R success(Object data)
    {
        return new R(data);
    }

    public  static R failed(String msg)
    {
        return new R(msg, 500);
    }
}
