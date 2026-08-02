package com.flydeer.common.constants;

import java.util.List;

public final class AtlasConstants {

    public static final int MAX_NAME_LENGTH = 64;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    public static final int MAX_TAG_LENGTH = 20;
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 50;

    public static final List<String> PRESET_TAGS = List.of(
        "流程", "系统", "架构", "鉴权", "入门", "进阶", "计算机",
        "调试", "机器学习", "Web", "运维", "数据库", "网络", "安全"
    );

    private AtlasConstants() {
    }
}
