package com.flydeer.structmind.controller.aop;

import com.flydeer.structmind.common.enums.AuthRequiredLevel;
import com.flydeer.structmind.common.enums.AuthResolveLevel;

import java.lang.annotation.*;

/**
 * Marks an {@link com.flydeer.structmind.contract.base.request.ApiRequest} parameter
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
