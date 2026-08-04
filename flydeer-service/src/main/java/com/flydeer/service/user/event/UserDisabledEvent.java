package com.flydeer.service.user.event;

/**
 * Published after a user account is disabled.
 * Side effects (delegate revoke, etc.) should listen asynchronously.
 */
public record UserDisabledEvent(long userId) {
}
