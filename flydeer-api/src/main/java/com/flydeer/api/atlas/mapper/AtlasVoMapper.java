package com.flydeer.api.atlas.mapper;

import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.repository.mysql.dto.AtlasDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public final class AtlasVoMapper {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private AtlasVoMapper() {
    }

    public static AtlasVO toVO(AtlasDTO dto, List<Long> userIds) {
        if (dto == null) {
            return null;
        }

        return new AtlasVO(
            dto.getId(),
            dto.getName(),
            dto.getDescription() == null ? "" : dto.getDescription(),
            dto.getAuthorId(),
            dto.getAuthorName() == null ? "" : dto.getAuthorName(),
            dto.getStatus(),
            dto.getTags() == null ? List.of() : dto.getTags(),
            toEpochMilli(dto.getCreatedAt()),
            toEpochMilli(dto.getUpdatedAt()),
            userIds != null && !userIds.isEmpty() && userIds.contains(dto.getAuthorId())
        );
    }

    private static Long toEpochMilli(LocalDateTime value) {
        return value == null ? null : value.atZone(ZONE).toInstant().toEpochMilli();
    }
}
