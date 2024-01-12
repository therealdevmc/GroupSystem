package de.therealdev.groupsystem.model.group;

import java.util.Objects;

/**
 * Represents a group
 * @param id
 * @param name
 * @param prefix
 */
public record Group(String id, String name, String prefix) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
