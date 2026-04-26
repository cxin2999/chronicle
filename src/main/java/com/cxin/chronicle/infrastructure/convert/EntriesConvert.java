package com.cxin.chronicle.infrastructure.convert;

import com.cxin.chronicle.infrastructure.model.entity.Entries;
import com.cxin.chronicle.infrastructure.model.vo.EntriesVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class EntriesConvert {
    public abstract EntriesVo toEntriesVo(Entries entries);

    public abstract List<EntriesVo> toEntryVoList(List<Entries> entriesList);
}
