package com.flydeer.service.atlas;

import com.flydeer.common.constants.AtlasConstants;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.request.BadRequestException;
import com.flydeer.contract.atlas.enums.AtlasStatus;
import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.repository.mysql.dto.AtlasQueryDTO;
import com.flydeer.repository.mysql.mapping.AtlasMapping;
import com.flydeer.repository.mysql.repository.AtlasRepository;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class AtlasService {

    private final AtlasRepository atlasRepository;

    public PageInfo<AtlasDTO> list(AtlasQueryDTO query, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<AtlasDTO> rows = atlasRepository.queryForPage(query);
        return new PageInfo<>(rows);
    }

    public List<String> listTags() {
        Set<String> tags = new LinkedHashSet<>(AtlasConstants.PRESET_TAGS);
        List<String> used = new ArrayList<>();
        for (String tagsJson : atlasRepository.listAllTagsJson()) {
            for (String tag : AtlasMapping.INSTANCE.jsonToTags(tagsJson)) {
                if (StringUtils.hasText(tag) && !tags.contains(tag)) {
                    used.add(tag);
                }
            }
        }
        used.sort(String::compareTo);
        tags.addAll(used);
        return List.copyOf(tags);
    }

    public AtlasDTO create(String name, String description, List<String> tags, Long authorId, String authorName) {
        AtlasDTO dto = new AtlasDTO();
        dto.setName(name.trim());
        dto.setDescription(description == null ? "" : description.trim());
        dto.setAuthorId(authorId);
        dto.setAuthorName(authorName == null ? "" : authorName);
        dto.setStatus(AtlasStatus.draft.name());
        dto.setTags(normalizeTags(tags));
        return atlasRepository.insert(dto);
    }

    public AtlasDTO update(Long atlasId, Long userId, String name, String description, List<String> tags)
        throws AtlasNotFoundException, AtlasForbiddenException, BadRequestException {
        AtlasDTO exist = requireAuthor(atlasId, userId);
        boolean touched = false;
        if (name != null) {
            if (!StringUtils.hasText(name)) {
                throw new BadRequestException("图集名称不能为空");
            }
            exist.setName(name.trim());
            touched = true;
        }
        if (description != null) {
            exist.setDescription(description.trim());
            touched = true;
        }
        if (tags != null) {
            exist.setTags(normalizeTags(tags));
            touched = true;
        }
        if (!touched) {
            return exist;
        }
        if (AtlasStatus.pending.name().equals(exist.getStatus())) {
            exist.setStatus(AtlasStatus.draft.name());
        }
        atlasRepository.update(exist);
        return atlasRepository.findById(atlasId);
    }

    public void submitReview(Long atlasId, Long userId)
        throws AtlasNotFoundException, AtlasForbiddenException, BadRequestException {
        AtlasDTO exist = requireAuthor(atlasId, userId);
        if (!AtlasStatus.draft.name().equals(exist.getStatus())) {
            throw new BadRequestException("仅草稿状态可提交审核");
        }
        AtlasDTO update = new AtlasDTO();
        update.setId(atlasId);
        update.setStatus(AtlasStatus.pending.name());
        atlasRepository.update(update);
    }

    public void delete(Long atlasId, Long userId) throws AtlasNotFoundException, AtlasForbiddenException {
        requireAuthor(atlasId, userId);
        atlasRepository.deleteById(atlasId);
    }

    public AtlasDTO getVisible(Long atlasId, Long viewerId)
        throws AtlasNotFoundException, AtlasForbiddenException {
        AtlasDTO exist = atlasRepository.findById(atlasId);
        if (exist == null) {
            throw new AtlasNotFoundException();
        }
        boolean published = AtlasStatus.published.name().equals(exist.getStatus());
        boolean owner = viewerId != null && Objects.equals(viewerId, exist.getAuthorId());
        if (!published && !owner) {
            throw new AtlasForbiddenException();
        }
        return exist;
    }

    public AtlasDTO requireAuthor(Long atlasId, Long userId)
        throws AtlasNotFoundException, AtlasForbiddenException {
        AtlasDTO exist = atlasRepository.findById(atlasId);
        if (exist == null) {
            throw new AtlasNotFoundException();
        }
        if (!Objects.equals(userId, exist.getAuthorId())) {
            throw new AtlasForbiddenException();
        }
        return exist;
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String tag : tags) {
            if (!StringUtils.hasText(tag)) {
                continue;
            }
            String trimmed = tag.trim();
            if (trimmed.length() > AtlasConstants.MAX_TAG_LENGTH) {
                trimmed = trimmed.substring(0, AtlasConstants.MAX_TAG_LENGTH);
            }
            normalized.add(trimmed);
        }
        return List.copyOf(normalized);
    }
}
