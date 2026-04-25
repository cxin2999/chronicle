package com.cxin.chronicle.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cxin.chronicle.infrastructure.model.dto.entries.EntriesAddReq;
import com.cxin.chronicle.infrastructure.model.entity.Entries;
import com.cxin.chronicle.infrastructure.model.entity.User;

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
}
