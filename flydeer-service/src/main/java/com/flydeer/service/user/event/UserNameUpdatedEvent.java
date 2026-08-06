package com.flydeer.service.user.event;

/**
 * Published after a user display name is updated.
 * Side effects (denormalized name sync, etc.) should listen asynchronously.
 */
public record UserNameUpdatedEvent(long userId, String oldName, String newName) {
}
