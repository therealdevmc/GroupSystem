package de.therealdev.groupsystem.model.player;

import de.therealdev.groupsystem.model.group.Group;

import java.util.Objects;

/**
 * PlayerGroup
 * @param id
 * @param group
 * @param valid
 * @param timed
 */
public record PlayerGroup(String id, Group group, Long valid, boolean timed) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerGroup that = (PlayerGroup) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
