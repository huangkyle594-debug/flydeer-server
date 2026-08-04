package com.flydeer.api.user.mapping;

import com.flydeer.contract.user.vo.JwtTokenVO;
import com.flydeer.contract.user.vo.OauthUrlVO;
import com.flydeer.service.user.model.IssuedTokensRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthorizationMapping {
    AuthorizationMapping INSTANCE = Mappers.getMapper(AuthorizationMapping.class);

    JwtTokenVO jwtToken(IssuedTokensRecord jwtToken);

    @Mapping(target = "authorizeUrl", source = "url")
    OauthUrlVO oauthUrl(String url);
}
