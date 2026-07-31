package com.flydeer.structmind.api.user.mapper;

import com.flydeer.structmind.contract.user.vo.JwtTokenVO;
import com.flydeer.structmind.contract.user.vo.OauthUrlVO;
import com.flydeer.structmind.service.user.model.IssuedTokensRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthorizationMapper {
    AuthorizationMapper INSTANCE = Mappers.getMapper(AuthorizationMapper.class);

    JwtTokenVO jwtToken(IssuedTokensRecord jwtToken);

    @Mapping(target = "authorizeUrl", source = "url")
    OauthUrlVO oauthUrl(String url);
}
