package com.training.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页响应包装（与小程序端约定格式）
 */
@Data
public class PageResult<T> {
    /** 数据列表 */
    private List<T> records;
    /** 总记录数 */
    private Long total;
    /** 当前页 */
    private Integer pageNum;
    /** 每页大小 */
    private Integer pageSize;

    /**
     * 从 MyBatis-Plus IPage 转换为 PageResult
     * 通过 page 参数透传，避免 training-common 依赖 mybatis-plus
     */
    public static <T> PageResult<T> of(List<T> records, long total, int pageNum, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }
}
