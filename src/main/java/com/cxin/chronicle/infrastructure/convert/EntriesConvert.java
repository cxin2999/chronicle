package com.cxin.chronicle.infrastructure.convert;

import com.cxin.chronicle.infrastructure.model.document.EntriesDocument;
import com.cxin.chronicle.infrastructure.model.entity.Entries;
import com.cxin.chronicle.infrastructure.model.vo.EntriesVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class EntriesConvert {
    public abstract EntriesVo toEntriesVo(Entries entries);

    public abstract List<EntriesVo> toEntryVoList(List<Entries> entriesList);

    public abstract EntriesVo documentToVo(EntriesDocument document);

    public abstract List<EntriesVo> documentsToVoList(List<EntriesDocument> documents);

    /**
     * Entries实体转换为EntriesDocument文档
     */
    public abstract EntriesDocument entryToDocument(Entries entries);

    /**
     * Boolean转Integer：true->1, false->0, null->0
     */
    protected Integer booleanToInteger(Boolean value) {
        return value != null && value ? 1 : 0;
    }

    /**
     * Integer转Boolean：1->true, 0->false, null->false
     */
    protected Boolean integerToBoolean(Integer value) {
        return value != null && value == 1;
    }

    /**
     * Byte转Boolean：1->true, 0->false, null->false
     */
    protected Boolean byteToBoolean(Byte value) {
        return value != null && value == 1;
    }
}
