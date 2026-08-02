package com.flydeer.repository.mysql.repository;

import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.repository.mysql.dto.AtlasQueryDTO;
import com.flydeer.repository.mysql.entity.AtlasEntity;
import com.flydeer.repository.mysql.mapper.AtlasExtMapper;
import com.flydeer.repository.mysql.mapper.AtlasMapper;
import com.flydeer.repository.mysql.mapping.AtlasMapping;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class AtlasRepository {

    private final AtlasMapper atlasMapper;
    private final AtlasExtMapper atlasExtMapper;

    public AtlasDTO findById(Long id) {
        AtlasEntity entity = atlasMapper.selectByPrimaryKey(id);
        return entity == null ? null : AtlasMapping.INSTANCE.toDto(entity);
    }

    public List<AtlasDTO> query(AtlasQueryDTO query) {
        return AtlasMapping.INSTANCE.toDtoList(atlasExtMapper.selectByQuery(query));
    }

    /**
     * PageHelper-aware query: keeps the MyBatis {@code Page} list wrapper so
     * {@link com.github.pagehelper.PageInfo} can read total/pages correctly.
     */
    public List<AtlasDTO> queryForPage(AtlasQueryDTO query) {
        List<AtlasEntity> entities = atlasExtMapper.selectByQuery(query);
        List<AtlasDTO> dtos = AtlasMapping.INSTANCE.toDtoList(entities);
        if (entities instanceof com.github.pagehelper.Page<AtlasEntity> page) {
            com.github.pagehelper.Page<AtlasDTO> dtoPage = new com.github.pagehelper.Page<>(page.getPageNum(), page.getPageSize());
            dtoPage.setTotal(page.getTotal());
            dtoPage.addAll(dtos);
            return dtoPage;
        }
        return dtos;
    }

    public List<String> listAllTagsJson() {
        return atlasExtMapper.selectAllTagsJson();
    }

    public AtlasDTO insert(AtlasDTO dto) {
        AtlasEntity entity = AtlasMapping.INSTANCE.dto2entity(dto);
        atlasMapper.insertSelective(entity);
        return AtlasMapping.INSTANCE.toDto(atlasMapper.selectByPrimaryKey(entity.getId()));
    }

    public void update(AtlasDTO dto) {
        AtlasEntity entity = AtlasMapping.INSTANCE.dto2entity(dto);
        atlasMapper.updateByPrimaryKeySelective(entity);
    }

    public void deleteById(Long id) {
        atlasMapper.deleteByPrimaryKey(id);
    }
}
