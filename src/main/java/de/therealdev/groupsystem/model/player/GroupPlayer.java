package de.therealdev.groupsystem.model.player;

import java.util.List;
import java.util.Objects;

/**
 * GroupPlayer
 * @param id
 * @param groups
 */
public record GroupPlayer(String id, List<PlayerGroup> groups) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupPlayer that = (GroupPlayer) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
