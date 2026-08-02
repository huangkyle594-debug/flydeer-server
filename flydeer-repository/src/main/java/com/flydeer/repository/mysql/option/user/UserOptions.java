package com.flydeer.repository.mysql.option.user;

import com.flydeer.repository.mysql.option.Options;

public class UserOptions extends Options<UserOption> {
    public static UserOptions option() {
        return new UserOptions();
    }

    public UserOptions withDelegatorIds() {
        add(UserOption.WITH_DELEGATOR_IDS);
        return this;
    }

    public Boolean hasWithDelegatorIds() {
        return contains(UserOption.WITH_DELEGATOR_IDS);
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

    public UserOptions delegated() {
        add(UserOption.DELEGATED);
        return this;
    }

    public Boolean hasDelegated() {
        return contains(UserOption.DELEGATED);
    }
}
