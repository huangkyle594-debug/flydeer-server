package com.flydeer.api.atlas;

import com.flydeer.api.atlas.mapper.AtlasVoMapper;
import com.flydeer.common.exception.auth.NeedLoginException;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.AtlasNotVisibleException;
import com.flydeer.common.exception.request.AtlasNotPublishedException;
import com.flydeer.common.exception.request.AtlasPublishException;
import com.flydeer.contract.atlas.AtlasApi;
import com.flydeer.contract.atlas.request.AtlasCreateRequest;
import com.flydeer.contract.atlas.request.AtlasIdRequest;
import com.flydeer.contract.atlas.request.AtlasQuery;
import com.flydeer.contract.atlas.request.AtlasUpdateRequest;
import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.contract.common.request.PageRequest;
import com.flydeer.contract.common.vo.PageVO;
import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.service.atlas.AtlasService;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AtlasApiImpl implements AtlasApi {

    private final AtlasService atlasService;

    @Override
    public List<String> listTags() {
        return atlasService.listTags();
    }

    @Override
    public PageVO<AtlasVO> pageQuery(@Valid PageRequest<AtlasQuery> request) throws NeedLoginException {
        PageInfo<AtlasDTO> pageInfo = atlasService.pageQuery(request);
        List<AtlasVO> items = pageInfo.getList().stream()
            .map(dto -> AtlasVoMapper.toVO(dto, request.getAllUserIds()))
            .toList();
        return new PageVO<>(items, pageInfo.isHasNextPage(), pageInfo.getTotal());
    }

    @Override
    public AtlasVO getAtlas(@Valid AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasNotPublishedException {
        AtlasDTO atlas = atlasService.requireReadable(request.getAtlasId(), request.getAllUserIds(), false);
        return AtlasVoMapper.toVO(atlas, request.getAllUserIds());
    }

    @Override
    public AtlasVO createAtlas(@Valid AtlasCreateRequest request) {
        AtlasDTO created = atlasService.create(request);
        return AtlasVoMapper.toVO(created, request.getAllUserIds());
    }

    @Override
    public AtlasVO updateAtlas(@Valid AtlasUpdateRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {
        AtlasDTO updated = atlasService.update(request);
        return AtlasVoMapper.toVO(updated, request.getAllUserIds());
    }

    @Override
    public void submitReview(@Valid AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException, AtlasPublishException {
        atlasService.submitReview(request.getAtlasId(), request.getAllUserIds());
    }

    @Override
    public void deleteAtlas(@Valid AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasForbiddenException, AtlasNotVisibleException {
        atlasService.delete(request.getAtlasId(), request.getAllUserIds());
    }
}
