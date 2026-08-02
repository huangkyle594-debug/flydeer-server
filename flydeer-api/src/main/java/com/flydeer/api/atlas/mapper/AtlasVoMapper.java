package com.flydeer.api.atlas.mapper;

import com.flydeer.contract.atlas.vo.AtlasListItemVO;
import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.repository.mysql.dto.AtlasDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public final class AtlasVoMapper {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private AtlasVoMapper() {
    }

    public static AtlasVO toVO(AtlasDTO dto) {
        if (dto == null) {
            return null;
        }
        return new AtlasVO(
            String.valueOf(dto.getId()),
            dto.getName(),
            dto.getDescription() == null ? "" : dto.getDescription(),
            String.valueOf(dto.getAuthorId()),
            dto.getAuthorName() == null ? "" : dto.getAuthorName(),
            dto.getStatus(),
            dto.getTags() == null ? List.of() : dto.getTags(),
            toEpochMilli(dto.getCreatedAt()),
            toEpochMilli(dto.getUpdatedAt())
        );
    }

    public static AtlasListItemVO toListItem(AtlasDTO dto, Long viewerId) {
        if (dto == null) {
            return null;
        }
        boolean editable = viewerId != null && Objects.equals(viewerId, dto.getAuthorId());
        return new AtlasListItemVO(
            String.valueOf(dto.getId()),
            dto.getName(),
            dto.getDescription() == null ? "" : dto.getDescription(),
            String.valueOf(dto.getAuthorId()),
            dto.getAuthorName() == null ? "" : dto.getAuthorName(),
            dto.getStatus(),
            dto.getTags() == null ? List.of() : dto.getTags(),
            toEpochMilli(dto.getCreatedAt()),
            toEpochMilli(dto.getUpdatedAt()),
            editable
        );
    }

    private static Long toEpochMilli(LocalDateTime value) {
        return value == null ? null : value.atZone(ZONE).toInstant().toEpochMilli();
    }
}
