package com.flydeer.repository.mysql.repository;

import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.AtlasNotVisibleException;
import com.flydeer.contract.atlas.enums.AtlasVisibleEnum;
import com.flydeer.contract.atlas.request.AtlasQuery;
import com.flydeer.contract.common.request.PageRequest;
import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.repository.mysql.entity.AtlasEntity;
import com.flydeer.repository.mysql.entity.AtlasEntityExample;
import com.flydeer.repository.mysql.mapper.AtlasExtMapper;
import com.flydeer.repository.mysql.mapper.AtlasMapper;
import com.flydeer.repository.mysql.mapping.AtlasMapping;
import com.flydeer.repository.mysql.option.atlas.AtlasOptions;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class AtlasRepository {

    private final AtlasMapper atlasMapper;
    private final AtlasExtMapper atlasExtMapper;

    public AtlasDTO queryById(Long atlasId, List<Long> userIds, AtlasOptions options)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {
        AtlasEntity entity = atlasMapper.selectByPrimaryKey(atlasId);
        if (options.hasRequireExist()) {
            ensureExist(entity);
        }
        if (options.hasRequireEditable()) {
            ensureEditable(entity, userIds);
        }
        if (options.hasRequireVisible()) {
            ensureVisible(entity);
        }
        return AtlasMapping.INSTANCE.toDto(entity);
    }

    public void ensureExist(AtlasEntity entity) throws AtlasNotFoundException {
        if (entity == null) {
            throw new AtlasNotFoundException();
        }
    }

    public void ensureEditable(AtlasEntity entity, List<Long> userIds) throws AtlasForbiddenException {
        if (!userIds.contains(entity.getAuthorId())) {
            throw new AtlasForbiddenException();
        }
    }

    public void ensureVisible(AtlasEntity entity) throws AtlasNotVisibleException {
        if (!AtlasVisibleEnum.VISIBLE.getCode().equals(entity.getVisible())) {
            throw new AtlasNotVisibleException();
        }
    }

    public Page<AtlasDTO> pageQuery(PageRequest<AtlasQuery> request, AtlasOptions options) {
        if (options.hasRequireVisible()) {
            request.getQuery().setVisible(true);
        }

        PageHelper.startPage(request.getPage(), request.getPageSize());
        List<AtlasEntity> entities = atlasExtMapper.pageQuery(request);
        List<AtlasDTO> atlases = AtlasMapping.INSTANCE.toDtoList(entities);
        if (entities instanceof Page<AtlasEntity> page) {
            Page<AtlasDTO> dtoPage = new Page<>(page.getPageNum(), page.getPageSize());
            dtoPage.setTotal(page.getTotal());
            dtoPage.addAll(atlases);
            return dtoPage;
        }
        Page<AtlasDTO> dtoPage = new Page<>(request.getPage(), request.getPageSize());
        dtoPage.setTotal(atlases.size());
        dtoPage.addAll(atlases);
        return dtoPage;
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

    public int deleteByAuthorId(Long authorId) {
        AtlasEntityExample example = new AtlasEntityExample();
        example.createCriteria().andAuthorIdEqualTo(authorId);
        return atlasMapper.deleteByExample(example);
    }

    public int updateAuthorNameByAuthorId(Long authorId, String authorName) {
        AtlasEntityExample example = new AtlasEntityExample();
        example.createCriteria().andAuthorIdEqualTo(authorId);
        AtlasEntity row = new AtlasEntity();
        row.setAuthorName(authorName);
        return atlasMapper.updateByExampleSelective(row, example);
    }
}
