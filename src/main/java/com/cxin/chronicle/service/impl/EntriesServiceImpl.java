package com.cxin.chronicle.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxin.chronicle.infrastructure.model.dto.entries.EntriesAddReq;
import com.cxin.chronicle.infrastructure.model.entity.Entries;
import com.cxin.chronicle.infrastructure.model.entity.User;
import com.cxin.chronicle.mapper.EntriesMapper;
import com.cxin.chronicle.service.EntriesService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 记录表 服务实现类
 * </p>
 *
 * @author Charles Chen
 * @since 2026-04-25
 */
@Service
public class EntriesServiceImpl extends ServiceImpl<EntriesMapper, Entries> implements EntriesService {

    @Override
    public boolean addEntries(User loginUser, EntriesAddReq request) {
        Entries entries = new Entries();
        entries.setEntryType(request.getEntryType());
        entries.setContent(request.getContent());
        entries.setUserId(loginUser.getId());
        return this.save(entries);
    }
}
