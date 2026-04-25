package com.cxin.chronicle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cxin.chronicle.infrastructure.model.entity.Entries;
import org.apache.ibatis.annotations.Mapper;

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

}
