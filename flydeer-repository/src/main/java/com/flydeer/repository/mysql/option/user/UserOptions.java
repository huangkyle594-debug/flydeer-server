package com.flydeer.repository.mysql.option.user;

import com.flydeer.repository.mysql.option.Options;

public class UserOptions extends Options<UserOption> {
    public static UserOptions option() {
        return new UserOptions();
    }

    public UserOptions withGrantedIds() {
        add(UserOption.WITH_GRANTED_IDS);
        return this;
    }

    public Boolean hasWithGrantedUserIds() {
        return contains(UserOption.WITH_GRANTED_IDS);
    }

    public UserOptions loginUsePhone() {
        add(UserOption.LOGIN_USE_PHONE);
        return this;
    }

    public Boolean hasLoginUsePhone() {
        return contains(UserOption.LOGIN_USE_PHONE);
    }

    public UserOptions updateToNull() {
        add(UserOption.UPDATE_TO_NULL);
        return this;
    }

    public Boolean hasUpdateToNull() {
        return contains(UserOption.UPDATE_TO_NULL);
    }

    public UserOptions grantedUser() {
        add(UserOption.GRANTED_USER);
        return this;
    }

    public Boolean hasGrantedUser() {
        return contains(UserOption.GRANTED_USER);
    }
}
