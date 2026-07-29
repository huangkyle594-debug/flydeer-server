package com.flydeer.structmind.controller.auth;

import com.flydeer.structmind.contract.enums.UserLevel;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireUserLevel {
    UserLevel value() default UserLevel.AUTHENTICATED;
}
