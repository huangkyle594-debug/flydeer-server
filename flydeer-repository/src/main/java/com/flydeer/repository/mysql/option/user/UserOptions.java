package com.flydeer.repository.mysql.option.user;

import com.flydeer.repository.mysql.option.Options;

public class UserOptions extends Options<UserOption> {
    public static UserOptions option() {
        return new UserOptions();
    }

    public UserOptions requireActive() {
        add(UserOption.REQUIRE_ACTIVE);
        return this;
    }

    public Boolean hasRequireActive() {
        return contains(UserOption.REQUIRE_ACTIVE);
    }

    public UserOptions requireVerify() {
        add(UserOption.REQUIRE_VERIFY);
        return this;
    }

    public Boolean hasRequireVerify() {
        return contains(UserOption.REQUIRE_VERIFY);
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

    public UserOptions delegated() {
        add(UserOption.DELEGATED);
        return this;
    }

    public Boolean hasDelegated() {
        return contains(UserOption.DELEGATED);
    }
}
