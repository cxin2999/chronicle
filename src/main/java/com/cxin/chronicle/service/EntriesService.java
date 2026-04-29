package com.cxin.chronicle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cxin.chronicle.infrastructure.model.dto.entries.*;
import com.cxin.chronicle.infrastructure.model.entity.Entries;
import com.cxin.chronicle.infrastructure.model.entity.User;
import com.cxin.chronicle.infrastructure.model.vo.EntriesVo;
import com.cxin.chronicle.infrastructure.model.vo.HeatmapDataVo;

import java.util.List;

/**
 * <p>
 * 记录表 服务类
 * </p>
 *
 * @author Charles Chen
 * @since 2026-04-25
 */
public interface EntriesService extends IService<Entries> {

    boolean addEntries(User loginUser, EntriesAddReq request);

    List<EntriesVo> queryDailyEntries(User loginUser, EntriesQueryReq request);

    /**
     * 查询热力图数据
     *
     * @param loginUser 登录用户
     * @param year      年份（yyyy格式）
     * @return 热力图数据列表
     */
    List<HeatmapDataVo> queryHeatmapData(User loginUser, int year);

    /**
     * 更新记录勾选状态
     *
     * @param loginUser 登录用户
     * @param request   更新请求
     * @return 是否更新成功
     */
    boolean updateChecked(User loginUser, EntriesUpdateCheckedReq request);

    /**
     * 更新记录内容和类型
     *
     * @param loginUser 登录用户
     * @param request   更新请求
     * @return 是否更新成功
     */
    boolean updateEntriesContent(User loginUser, EntriesContentUpdateReq request);

    /**
     * 删除记录
     *
     * @param loginUser 登录用户
     * @param request   删除请求
     * @return 是否删除成功
     */
    boolean deleteEntry(User loginUser, EntriesDeleteReq request);

    /**
     * 游标分页查询历史记录
     *
     * @param loginUser 登录用户
     * @param request   分页请求
     * @return 记录列表
     */
    List<EntriesVo> queryHistoryWithCursor(User loginUser, EntriesHistoryReq request);
}
