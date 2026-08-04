package com.flydeer.service.user.event;

/**
 * Published after a user account is deleted (self-cancel).
 * Side effects (e.g. delete delegate rows) should listen asynchronously.
 */
public record UserDeletedEvent(long userId) {
}
