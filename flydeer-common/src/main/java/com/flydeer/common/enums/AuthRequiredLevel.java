package com.flydeer.common.enums;

/**
 * Required user auth level for an endpoint.
 */
public enum AuthRequiredLevel {
    ANONYMOUS,
    AUTHENTICATED,
    VERIFIED,
    /** Logged-in user whose id is in {@code app.user.admin-ids}. */
    ADMIN
}
