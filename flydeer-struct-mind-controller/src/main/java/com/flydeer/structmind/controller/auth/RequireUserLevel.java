package com.flydeer.structmind.controller.auth;

import com.flydeer.structmind.contract.user.enums.UserLevelEnum;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireUserLevel {
    UserLevelEnum value() default UserLevelEnum.AUTHENTICATED;
}
