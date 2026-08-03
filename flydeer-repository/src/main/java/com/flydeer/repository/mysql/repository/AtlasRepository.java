package com.flydeer.repository.mysql.repository;

import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.repository.mysql.dto.AtlasQueryDTO;
import com.flydeer.repository.mysql.entity.AtlasEntity;
import com.flydeer.repository.mysql.entity.AtlasEntityExample;
import com.flydeer.repository.mysql.mapper.AtlasMapper;
import com.flydeer.repository.mysql.mapping.AtlasMapping;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Repository
@AllArgsConstructor
public class AtlasRepository {

    private final AtlasMapper atlasMapper;

    public AtlasDTO findById(Long id) {
        AtlasEntity entity = atlasMapper.selectByPrimaryKey(id);
        return entity == null ? null : AtlasMapping.INSTANCE.toDto(entity);
    }

    /**
     * PageHelper-aware query: keeps the MyBatis {@code Page} list wrapper so
     * {@link com.github.pagehelper.PageInfo} can read total/pages correctly.
     */
    public List<AtlasDTO> queryForPage(AtlasQueryDTO query) {
        List<AtlasEntity> entities = atlasMapper.selectByExample(buildExample(query));
        List<AtlasDTO> dtos = AtlasMapping.INSTANCE.toDtoList(entities);
        if (entities instanceof com.github.pagehelper.Page<AtlasEntity> page) {
            com.github.pagehelper.Page<AtlasDTO> dtoPage =
                new com.github.pagehelper.Page<>(page.getPageNum(), page.getPageSize());
            dtoPage.setTotal(page.getTotal());
            dtoPage.addAll(dtos);
            return dtoPage;
        }
        return dtos;
    }

    public List<String> listAllTagsJson() {
        return atlasMapper.selectByExample(new AtlasEntityExample()).stream()
            .map(AtlasEntity::getTagsJson)
            .filter(Objects::nonNull)
            .toList();
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

    private AtlasEntityExample buildExample(AtlasQueryDTO query) {
        AtlasEntityExample example = new AtlasEntityExample();
        example.setOrderByClause("`updated_at` desc");

        List<Consumer<AtlasEntityExample.Criteria>> visibility = visibilityPredicates(query);
        List<Consumer<AtlasEntityExample.Criteria>> keywords = keywordPredicates(query.getKeyword());
        List<Consumer<AtlasEntityExample.Criteria>> tags = tagPredicates(query.getTags());

        boolean first = true;
        for (Consumer<AtlasEntityExample.Criteria> vis : visibility) {
            for (Consumer<AtlasEntityExample.Criteria> kw : keywords) {
                for (Consumer<AtlasEntityExample.Criteria> tag : tags) {
                    AtlasEntityExample.Criteria criteria =
                        first ? example.createCriteria() : example.or();
                    first = false;
                    vis.accept(criteria);
                    kw.accept(criteria);
                    tag.accept(criteria);
                }
            }
        }
        return example;
    }

    private List<Consumer<AtlasEntityExample.Criteria>> visibilityPredicates(AtlasQueryDTO query) {
        String scope = query.getScope();
        boolean authorOnly = "CREATED".equals(scope) || "MANAGED".equals(scope);

        List<Consumer<AtlasEntityExample.Criteria>> list = new ArrayList<>();
        if (authorOnly) {
            list.add(c -> c.andAuthorIdEqualTo(query.getViewerId()));
            return list;
        }
        if (query.getViewerId() != null) {
            list.add(c -> c.andStatusEqualTo("published"));
            list.add(c -> c.andAuthorIdEqualTo(query.getViewerId()));
            return list;
        }
        list.add(c -> c.andStatusEqualTo("published"));
        return list;
    }

    private List<Consumer<AtlasEntityExample.Criteria>> keywordPredicates(String keyword) {
        List<Consumer<AtlasEntityExample.Criteria>> list = new ArrayList<>();
        if (!StringUtils.hasText(keyword)) {
            list.add(c -> {
            });
            return list;
        }
        String like = "%" + keyword.trim() + "%";
        list.add(c -> c.andNameLike(like));
        list.add(c -> c.andDescriptionLike(like));
        list.add(c -> c.andAuthorNameLike(like));
        return list;
    }

    /**
     * Tag OR match via {@code tags_json LIKE}; collaboration/JSON_CONTAINS not used
     * so Example API stays sufficient.
     */
    private List<Consumer<AtlasEntityExample.Criteria>> tagPredicates(List<String> tags) {
        List<Consumer<AtlasEntityExample.Criteria>> list = new ArrayList<>();
        if (tags == null || tags.isEmpty()) {
            list.add(c -> {
            });
            return list;
        }
        for (String tag : tags) {
            if (!StringUtils.hasText(tag)) {
                continue;
            }
            String like = "%\"" + tag.trim() + "\"%";
            list.add(c -> c.andTagsJsonLike(like));
        }
        if (list.isEmpty()) {
            list.add(c -> {
            });
        }
        return list;
    }
}
