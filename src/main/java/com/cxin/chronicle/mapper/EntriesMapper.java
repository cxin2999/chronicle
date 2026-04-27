package com.cxin.chronicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cxin.chronicle.infrastructure.model.entity.Entries;
import com.cxin.chronicle.infrastructure.model.vo.HeatmapDataVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 记录表 Mapper 接口
 * </p>
 *
 * @author Charles Chen
 * @since 2026-04-25
 */
@Mapper
public interface EntriesMapper extends BaseMapper<Entries> {

    /**
     * 查询热力图数据
     *
     * @param userId 用户ID
     * @param year   年份（yyyy格式）
     * @return 热力图数据列表
     */
    List<HeatmapDataVo> queryHeatmapData(@Param("userId") String userId,
                                         @Param("year") int year);

    /**
     * 游标分页查询历史记录
     *
     * @param userId         用户ID
     * @param lastCreateTime 上一页最后一条记录的创建时间（首次传 null）
     * @param pageSize       每页大小
     * @return 记录列表
     */
    List<Entries> queryHistoryWithCursor(@Param("userId") String userId,
                                         @Param("lastCreateTime") LocalDateTime lastCreateTime,
                                         @Param("pageSize") int pageSize);
}
