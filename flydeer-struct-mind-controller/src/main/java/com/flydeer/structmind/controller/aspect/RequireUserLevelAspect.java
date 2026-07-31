package com.flydeer.structmind.controller.aspect;

import com.flydeer.structmind.common.exception.ErrorCodes;
import com.flydeer.structmind.common.exception.business.BusinessException;
import com.flydeer.structmind.contract.base.request.ApiRequest;
import com.flydeer.structmind.contract.user.enums.UserLevel;
import com.flydeer.structmind.controller.auth.RequireUserLevel;
import com.flydeer.structmind.controller.support.AuthCookieSupport;
import com.flydeer.structmind.repository.mysql.entity.UserEntity;
import com.flydeer.structmind.service.user.utils.JwtTokenUtils;
import com.flydeer.structmind.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RequireUserLevelAspect {

    private final JwtTokenUtils jwtTokenUtils;
    private final UserService userService;
    private final AuthCookieSupport authCookieSupport;

    public RequireUserLevelAspect(
        JwtTokenUtils jwtTokenUtils, UserService userService, AuthCookieSupport authCookieSupport) {
        this.jwtTokenUtils = jwtTokenUtils;
        this.userService = userService;
        this.authCookieSupport = authCookieSupport;
    }

    @Around("@annotation(com.flydeer.structmind.controller.auth.RequireUserLevel) || "
        + "@within(com.flydeer.structmind.controller.auth.RequireUserLevel)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        RequireUserLevel methodAnno =
            AnnotationUtils.findAnnotation(signature.getMethod(), RequireUserLevel.class);
        RequireUserLevel typeAnno =
            AnnotationUtils.findAnnotation(signature.getDeclaringType(), RequireUserLevel.class);
        RequireUserLevel anno = methodAnno != null ? methodAnno : typeAnno;
        UserLevel required = anno != null ? anno.value() : UserLevel.AUTHENTICATED;

        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException(ErrorCodes.UNAUTHORIZED, "no request context");
        }
        HttpServletRequest request = attrs.getRequest();
        String bearer = authCookieSupport.extractBearer(request);

        UserEntity user = null;
        if (bearer != null && !bearer.isBlank()) {
            long userId = jwtTokenUtils.parseAccessToken(bearer);
            user = userService.requireActive(userId);
        }

        if (required == UserLevel.AUTHENTICATED || required == UserLevel.VERIFIED) {
            if (user == null) {
                throw new BusinessException(ErrorCodes.UNAUTHORIZED, "login required");
            }
        }
        if (required == UserLevel.VERIFIED) {
            if (user.getVerified() == null || user.getVerified() != 1) {
                throw new BusinessException(ErrorCodes.FORBIDDEN, "verified user required");
            }
        }

        if (user != null) {
            List<Long> delegated = userService.listDelegatedUserIds(user.getId());
            injectBaseRequest(pjp.getArgs(), user, delegated);
            request.setAttribute("auth.userId", user.getId());
        } else {
            request.setAttribute("auth.userId", null);
        }

        return pjp.proceed();
    }

    private void injectBaseRequest(Object[] args, UserEntity user, List<Long> delegated) {
        if (args == null) {
            return;
        }
        for (Object arg : args) {
            if (arg instanceof ApiRequest base) {
                base.setUserId(user.getId());
                base.setChannel(user.getChannel());
                base.setVerified(user.getVerified() != null && user.getVerified() == 1);
                base.setDelegatedUserIds(delegated);
            }
        }
    }
}
