package com.training.common.dto;

import lombok.Data;

/**
 * 分页请求基类
 */
@Data
public class PageQuery {
    /** 当前页（从 1 开始）*/
    private Integer pageNum = 1;
    /** 每页大小 */
    private Integer pageSize = 10;
}
