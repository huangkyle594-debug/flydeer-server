package com.flydeer.controller.aop;

import com.flydeer.common.enums.AuthRequiredLevel;
import com.flydeer.common.enums.AuthResolveLevel;
import com.flydeer.contract.common.request.ApiRequest;

import java.lang.annotation.*;

/**
 * Marks an {@link ApiRequest} parameter
 * to be resolved from HTTP auth context.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthCheck {

    /**
     * How much auth context to resolve into the request.
     */
    AuthResolveLevel resolve() default AuthResolveLevel.NONE;

    /**
     * Minimum auth level required to enter the endpoint.
     */
    AuthRequiredLevel required() default AuthRequiredLevel.ANONYMOUS;
}
