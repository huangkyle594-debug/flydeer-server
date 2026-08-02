package com.flydeer.repository.mysql.mapper;

import com.flydeer.repository.mysql.dto.AtlasQueryDTO;
import com.flydeer.repository.mysql.entity.AtlasEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AtlasExtMapper {

    List<AtlasEntity> selectByQuery(@Param("query") AtlasQueryDTO query);

    List<String> selectAllTagsJson();
}
