package com.flydeer.api.admin;

import com.flydeer.api.atlas.mapper.AtlasVoMapper;
import com.flydeer.common.exception.business.AtlasForbiddenException;
import com.flydeer.common.exception.business.AtlasNotFoundException;
import com.flydeer.common.exception.business.AtlasNotVisibleException;
import com.flydeer.common.exception.business.UserNotFoundException;
import com.flydeer.common.exception.request.AtlasApproveException;
import com.flydeer.contract.admin.AdminApi;
import com.flydeer.contract.admin.request.DisableUserRequest;
import com.flydeer.contract.atlas.request.AtlasIdRequest;
import com.flydeer.contract.atlas.request.AtlasQuery;
import com.flydeer.contract.atlas.vo.AtlasVO;
import com.flydeer.contract.common.request.PageRequest;
import com.flydeer.contract.common.vo.PageVO;
import com.flydeer.repository.mysql.dto.AtlasDTO;
import com.flydeer.service.atlas.AtlasService;
import com.flydeer.service.user.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AdminApiImpl implements AdminApi {

    private final UserService userService;
    private final AtlasService atlasService;

    @Override
    public void disableUser(@Valid DisableUserRequest request) throws UserNotFoundException {
        userService.disableUser(request.getOperatorId());
    }

    @Override
    public PageVO<AtlasVO> pagePendingAtlases(@Valid PageRequest<AtlasQuery> request) {
        PageInfo<AtlasDTO> pageInfo = atlasService.pagePending(request);
        List<AtlasVO> items = pageInfo.getList().stream()
            .map(dto -> AtlasVoMapper.toVO(dto, request.getAllUserIds()))
            .toList();
        return new PageVO<>(items, pageInfo.isHasNextPage(), pageInfo.getTotal());
    }

    @Override
    public void approveAtlas(@Valid AtlasIdRequest request)
        throws AtlasNotFoundException, AtlasApproveException, AtlasNotVisibleException, AtlasForbiddenException {
        atlasService.approvePublish(request.getAtlasId());
    }
}
