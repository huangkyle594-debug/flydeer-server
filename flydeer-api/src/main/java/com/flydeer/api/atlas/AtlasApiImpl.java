package com.flydeer.api.atlas;

import com.flydeer.api.atlas.mapper.AtlasVoMapper;
import com.flydeer.common.constants.AtlasConstants;
import com.flydeer.common.exception.auth.NeedLoginException;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.request.BadRequestException;
import com.flydeer.contract.atlas.AtlasApi;
import com.flydeer.contract.atlas.enums.AtlasPermissionScope;
import com.flydeer.contract.atlas.request.AtlasCreateRequest;
import com.flydeer.contract.atlas.request.AtlasIdRequest;
import com.flydeer.contract.atlas.request.AtlasQueryRequest;
import com.flydeer.contract.atlas.request.AtlasUpdateRequest;
import com.flydeer.contract.atlas.vo.AtlasListItemVO;
import com.flydeer.contract.atlas.vo.AtlasPageVO;
import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.repository.mysql.dto.AtlasQueryDTO;
import com.flydeer.service.atlas.AtlasService;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@AllArgsConstructor
public class AtlasApiImpl implements AtlasApi {

    private final AtlasService atlasService;

    @Override
    public AtlasPageVO listAtlases(@Valid AtlasQueryRequest request) throws NeedLoginException {
        AtlasPermissionScope scope = request.resolvedScope();
        if ((scope == AtlasPermissionScope.CREATED || scope == AtlasPermissionScope.MANAGED)
            && request.getUserId() == null) {
            throw new NeedLoginException();
        }
        int page = request.getPage() <= 0 ? AtlasConstants.DEFAULT_PAGE : request.getPage();
        int pageSize = request.getPageSize() <= 0 ? AtlasConstants.DEFAULT_PAGE_SIZE : request.getPageSize();
        if (pageSize > AtlasConstants.MAX_PAGE_SIZE) {
            pageSize = AtlasConstants.MAX_PAGE_SIZE;
        }

        AtlasQueryDTO query = new AtlasQueryDTO();
        query.setViewerId(request.getUserId());
        query.setScope(scope.name());
        if (StringUtils.hasText(request.getKeyword())) {
            query.setKeyword(request.getKeyword().trim());
        }
        query.setTags(request.getTags());

        PageInfo<AtlasDTO> pageInfo = atlasService.list(query, page, pageSize);
        List<AtlasListItemVO> items = pageInfo.getList().stream()
            .map(dto -> AtlasVoMapper.toListItem(dto, request.getUserId()))
            .toList();
        return new AtlasPageVO(items, pageInfo.isHasNextPage(), pageInfo.getTotal());
    }

    @Override
    public List<String> listTags() {
        return atlasService.listTags();
    }

    @Override
    public AtlasVO createAtlas(@Valid AtlasCreateRequest request) {
        AtlasDTO created = atlasService.create(
            request.getName(),
            request.getDescription(),
            request.getTags(),
            request.getUserId(),
            null);
        return AtlasVoMapper.toVO(created);
    }

    @Override
    public AtlasVO updateAtlas(@Valid AtlasUpdateRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, BadRequestException {
        AtlasDTO updated = atlasService.update(
            request.getAtlasId(),
            request.getUserId(),
            request.getName(),
            request.getDescription(),
            request.getTags());
        return AtlasVoMapper.toVO(updated);
    }

    @Override
    public void submitReview(@Valid AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, BadRequestException {
        atlasService.submitReview(request.getAtlasId(), request.getUserId());
    }

    @Override
    public void deleteAtlas(@Valid AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException {
        atlasService.delete(request.getAtlasId(), request.getUserId());
    }

    @Override
    public AtlasVO getAtlas(@Valid AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException {
        return AtlasVoMapper.toVO(atlasService.getVisible(request.getAtlasId(), request.getUserId()));
    }
}
