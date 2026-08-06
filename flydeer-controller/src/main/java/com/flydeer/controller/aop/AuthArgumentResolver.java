package com.flydeer.controller.aop;

import com.flydeer.common.enums.AuthRequiredLevel;
import com.flydeer.common.enums.AuthResolveLevel;
import com.flydeer.common.exception.auth.AccessTokenParseException;
import com.flydeer.common.exception.auth.NeedAdminException;
import com.flydeer.common.exception.auth.NeedLoginException;
import com.flydeer.common.exception.auth.NeedVerifyException;
import com.flydeer.common.exception.business.UserInvalidException;
import com.flydeer.contract.common.request.ApiRequest;
import com.flydeer.contract.user.enums.DelegateRelationEnum;
import com.flydeer.contract.user.enums.DelegateStatusEnum;
import com.flydeer.controller.utils.AuthCookieUtils;
import com.flydeer.repository.mysql.dto.UserDelegateDTO;
import com.flydeer.service.user.UserDelegateService;
import com.flydeer.service.user.config.UserConfig;
import com.flydeer.service.user.model.AccessTokenClaims;
import com.flydeer.service.user.utils.JwtTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves {@link ApiRequest} parameters annotated with {@link AuthCheck}
 * from Bearer access token only (does not bind request body / query).
 */
@Component
public class AuthArgumentResolver implements HandlerMethodArgumentResolver, WebMvcConfigurer {

    private final JwtTokenUtils jwtTokenUtils;
    private final UserDelegateService userDelegateService;
    private final AuthCookieUtils authCookieUtils;
    private final UserConfig userConfig;

    public AuthArgumentResolver(
        JwtTokenUtils jwtTokenUtils,
        UserDelegateService userDelegateService,
        AuthCookieUtils authCookieUtils,
        UserConfig userConfig) {
        this.jwtTokenUtils = jwtTokenUtils;
        this.userDelegateService = userDelegateService;
        this.authCookieUtils = authCookieUtils;
        this.userConfig = userConfig;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthCheck.class)
            && ApiRequest.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
        throws AccessTokenParseException, NeedLoginException, NeedVerifyException, NeedAdminException,
        UserInvalidException {

        AuthCheck authCheck = parameter.getParameterAnnotation(AuthCheck.class);
        if (authCheck == null) {
            throw new IllegalStateException("@AuthCheck required");
        }

        ApiRequest apiRequest = new ApiRequest();
        HttpServletRequest httpRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        AccessTokenClaims claims = resolveClaims(authCheck.resolve(), httpRequest);
        enforceRequired(authCheck.required(), claims);
        if (claims != null) {
            fillRequest(apiRequest, claims, authCheck.resolve());
        }
        return apiRequest;
    }

    private AccessTokenClaims resolveClaims(AuthResolveLevel resolveLevel, HttpServletRequest httpRequest)
        throws AccessTokenParseException {
        if (resolveLevel == AuthResolveLevel.NONE || httpRequest == null) {
            return null;
        }
        String token = authCookieUtils.extractAccessToken(httpRequest);
        if (!StringUtils.hasText(token)) {
            return null;
        }
        return jwtTokenUtils.parseAccessToken(token);
    }

    private void enforceRequired(AuthRequiredLevel required, AccessTokenClaims claims)
        throws NeedLoginException, NeedVerifyException, NeedAdminException, UserInvalidException {

        if (required == AuthRequiredLevel.ANONYMOUS) {
            return;
        }
        if (claims == null) {
            throw new NeedLoginException();
        }
        // status comes from JWT; disable takes effect after access token expires / refresh.
        if (!claims.active()) {
            throw new UserInvalidException();
        }
        if (required == AuthRequiredLevel.VERIFIED && !claims.verified()) {
            throw new NeedVerifyException();
        }
        if (required == AuthRequiredLevel.ADMIN && !userConfig.isAdmin(claims.userId())) {
            throw new NeedAdminException();
        }
    }

    private void fillRequest(ApiRequest apiRequest, AccessTokenClaims claims, AuthResolveLevel resolveLevel) {
        apiRequest.setUserId(claims.userId());
        apiRequest.setVerified(claims.verified());
        apiRequest.setName(claims.name());

        if (resolveLevel == AuthResolveLevel.SELF) {
            apiRequest.setAllUserIds(List.of(claims.userId()));
            return;
        }
        if (resolveLevel == AuthResolveLevel.DELEGATE) {
            List<Long> delegatedIds = userDelegateService.queryDelegations(
                    claims.userId(),
                    List.of(DelegateStatusEnum.ACCEPTED.name()),
                    DelegateRelationEnum.DELEGATOR)
                .stream()
                .map(UserDelegateDTO::getDelegatedId)
                .toList();
            List<Long> allUserIds = new ArrayList<>(delegatedIds.size() + 1);
            allUserIds.add(claims.userId());
            allUserIds.addAll(delegatedIds);
            apiRequest.setAllUserIds(allUserIds);
        }
    }
}
