package com.flydeer.repository.mysql.mapper;

import com.flydeer.repository.mysql.entity.AtlasEntity;
import com.flydeer.repository.mysql.entity.AtlasEntityExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AtlasMapper {
    long countByExample(AtlasEntityExample example);

    int deleteByExample(AtlasEntityExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AtlasEntity row);

    int insertSelective(AtlasEntity row);

    List<AtlasEntity> selectByExample(AtlasEntityExample example);

    AtlasEntity selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") AtlasEntity row, @Param("example") AtlasEntityExample example);

    int updateByExample(@Param("row") AtlasEntity row, @Param("example") AtlasEntityExample example);

    int updateByPrimaryKeySelective(AtlasEntity row);

    int updateByPrimaryKey(AtlasEntity row);
}