package com.flydeer.repository.mysql.option.atlas;

import com.flydeer.repository.mysql.option.Options;

public class AtlasOptions extends Options<AtlasOption> {
    public static AtlasOptions option() {
        return new AtlasOptions();
    }

    public AtlasOptions requireExist() {
        add(AtlasOption.REQUIRE_EXIST);
        return this;
    }

    public Boolean hasRequireExist() {
        return contains(AtlasOption.REQUIRE_EXIST);
    }

    public AtlasOptions requireEditable() {
        add(AtlasOption.REQUIRE_EDITABLE);
        return this;
    }

    public Boolean hasRequireEditable() {
        return contains(AtlasOption.REQUIRE_EDITABLE);
    }

    public AtlasOptions requireVisible() {
        add(AtlasOption.REQUIRE_VISIBLE);
        return this;
    }

    public Boolean hasRequireVisible() {
        return contains(AtlasOption.REQUIRE_VISIBLE);
    }

}
