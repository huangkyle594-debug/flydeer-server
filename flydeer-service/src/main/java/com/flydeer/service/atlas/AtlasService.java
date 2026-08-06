package com.flydeer.service.atlas;

import com.flydeer.common.constants.AtlasConstants;
import com.flydeer.common.exception.auth.NeedLoginException;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.AtlasNotVisibleException;
import com.flydeer.common.exception.request.AtlasApproveException;
import com.flydeer.common.exception.request.AtlasNotPublishedException;
import com.flydeer.common.exception.request.AtlasPublishException;
import com.flydeer.contract.atlas.enums.AtlasPermissionScopeEnum;
import com.flydeer.contract.atlas.enums.AtlasStatus;
import com.flydeer.contract.atlas.request.AtlasCreateRequest;
import com.flydeer.contract.atlas.request.AtlasQuery;
import com.flydeer.contract.atlas.request.AtlasUpdateRequest;
import com.flydeer.contract.common.request.PageRequest;
import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.repository.mysql.option.atlas.AtlasOptions;
import com.flydeer.repository.mysql.repository.AtlasRepository;
import com.flydeer.service.atlas.config.AtlasConfig;
import com.flydeer.service.user.event.UserDeletedEvent;
import com.flydeer.service.user.event.UserNameUpdatedEvent;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AtlasService {

    private final AtlasRepository atlasRepository;
    private final AtlasConfig atlasConfig;

    public List<String> listTags() {
        return atlasConfig.getTags();
    }

    public PageInfo<AtlasDTO> pageQuery(PageRequest<AtlasQuery> request) throws NeedLoginException {
        AtlasQuery query = request.getQuery();
        boolean ownScope = AtlasPermissionScopeEnum.CREATED == query.getScope()
            || AtlasPermissionScopeEnum.MANAGED == query.getScope();
        if (ownScope) {
            if (request.getUserId() == null) {
                throw new NeedLoginException();
            }
            return pageQuery(request, AtlasOptions.option());
        }
        query.setStatus(AtlasStatus.PUBLISHED.name());
        return pageQuery(request, AtlasOptions.option().requireVisible());
    }

    public PageInfo<AtlasDTO> pageQuery(PageRequest<AtlasQuery> request, AtlasOptions options) {
        Page<AtlasDTO> rows = atlasRepository.pageQuery(request, options);
        return new PageInfo<>(rows);
    }

    /**
     * Admin review queue: PENDING atlases, not limited by visible.
     */
    public PageInfo<AtlasDTO> pagePending(PageRequest<AtlasQuery> request) {
        AtlasQuery query = request.getQuery();
        if (query == null) {
            query = new AtlasQuery();
            request.setQuery(query);
        }
        query.setStatus(AtlasStatus.PENDING.name());
        return pageQuery(request, AtlasOptions.option());
    }

    public AtlasDTO queryById(Long atlasId, List<Long> userIds, Boolean isAdmin)
        throws AtlasForbiddenException, AtlasNotFoundException, AtlasNotVisibleException, AtlasNotPublishedException {
        AtlasDTO atlas = atlasRepository.queryById(atlasId, userIds, AtlasOptions.option().requireExist().requireVisible());
        if (!isAdmin) {
            if (userIds == null || !userIds.contains(atlas.getAuthorId())) {
                ensurePublished(atlas);
            }
        }
        return atlas;
    }

    public AtlasDTO create(AtlasCreateRequest request) {
        AtlasDTO dto = new AtlasDTO();
        dto.setName(request.getName().trim());
        dto.setDescription(request.getDescription().trim());
        dto.setAuthorId(request.getUserId());
        dto.setAuthorName(request.getAuthorName());
        dto.setStatus(AtlasStatus.DRAFT.name());
        dto.setVisible(false);
        dto.setTags(normalizeTags(request.getTags()));
        return atlasRepository.insert(dto);
    }

    public AtlasDTO update(AtlasUpdateRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {
        AtlasDTO exist = atlasRepository.queryById(request.getAtlasId(), request.getAllUserIds(),
            AtlasOptions.option().requireExist().requireEditable());

        AtlasDTO update = new AtlasDTO();
        update.setId(exist.getId());
        boolean touched = false;
        if (StringUtils.hasText(request.getName())) {
            update.setName(request.getName());
            touched = true;
        }
        if (StringUtils.hasText(request.getDescription())) {
            update.setDescription(request.getDescription());
            touched = true;
        }
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            update.setTags(normalizeTags(request.getTags()));
            touched = true;
        }
        if (!touched) {
            return exist;
        }
        atlasRepository.update(update);
        return atlasRepository.queryById(request.getAtlasId(), request.getAllUserIds(), AtlasOptions.option());
    }

    public void submitReview(Long atlasId, List<Long> userIds)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasPublishException, AtlasNotVisibleException {
        AtlasDTO exist = atlasRepository.queryById(atlasId, userIds,
            AtlasOptions.option().requireExist().requireEditable());
        if (!AtlasStatus.DRAFT.name().equals(exist.getStatus())) {
            throw new AtlasPublishException();
        }
        AtlasDTO update = new AtlasDTO();
        update.setId(atlasId);
        update.setStatus(AtlasStatus.PENDING.name());
        atlasRepository.update(update);
    }

    /**
     * Admin approve: PENDING → PUBLISHED and mark visible.
     */
    public void approvePublish(Long atlasId)
        throws AtlasNotFoundException, AtlasApproveException, AtlasNotVisibleException, AtlasForbiddenException {
        AtlasDTO exist = atlasRepository.queryById(atlasId, null, AtlasOptions.option().requireExist());
        if (!AtlasStatus.PENDING.name().equals(exist.getStatus())) {
            throw new AtlasApproveException();
        }
        AtlasDTO update = new AtlasDTO();
        update.setId(atlasId);
        update.setStatus(AtlasStatus.PUBLISHED.name());
        update.setVisible(true);
        atlasRepository.update(update);
    }

    public void delete(Long atlasId, List<Long> userIds)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {
        atlasRepository.queryById(atlasId, userIds,
            AtlasOptions.option().requireExist().requireEditable());
        atlasRepository.deleteById(atlasId);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserDeleted(UserDeletedEvent event) {
        try {
            int deleted = atlasRepository.deleteByAuthorId(event.userId());
            log.info("deleted {} atlases for deleted userId={}", deleted, event.userId());
        } catch (Exception e) {
            log.error("failed to delete atlases for deleted userId={}", event.userId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserNameUpdated(UserNameUpdatedEvent event) {
        try {
            int updated = atlasRepository.updateAuthorNameByAuthorId(event.userId(), event.newName());
            log.info("updated author_name on {} atlases for userId={}", updated, event.userId());
        } catch (Exception e) {
            log.error("failed to update atlas author_name for userId={}", event.userId(), e);
        }
    }

    private void ensurePublished(AtlasDTO atlas) throws AtlasNotPublishedException {
        if (!AtlasStatus.PUBLISHED.name().equals(atlas.getStatus())) {
            throw new AtlasNotPublishedException();
        }
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
