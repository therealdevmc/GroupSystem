package de.therealdev.groupsystem.database.mysql;

import de.therealdev.groupsystem.model.group.Group;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RequiredArgsConstructor
public class GroupRepository {

    private final AsyncMySQL asyncMySQL;

    @SneakyThrows
    public void createTable() {
        PreparedStatement statement = asyncMySQL.prepare("CREATE TABLE IF NOT EXISTS groupsystem (id VARCHAR(36) PRIMARY KEY, name VARCHAR(64), prefix VARCHAR(64))");
            asyncMySQL.update(statement);

    }

    public void dropTable() {
        try (PreparedStatement statement = asyncMySQL.prepare("DROP TABLE IF EXISTS groupsystem")) {
            asyncMySQL.update(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void createGroup(String id, String name, String prefix) {

        PreparedStatement statement = asyncMySQL.prepare("INSERT INTO groupsystem (id, name, prefix) VALUES (?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, name);
            statement.setString(3, prefix);
            asyncMySQL.update(statement);
    }

    @SneakyThrows
    public void deleteGroup(Group group) {
        PreparedStatement statement = asyncMySQL.prepare("DELETE FROM groupsystem WHERE id = ?");
            statement.setString(1, group.id());
            asyncMySQL.update(statement);
       deleteGroup(group.id());
    }

    @SneakyThrows
    public void deleteGroup(String id) {
        PreparedStatement statement = asyncMySQL.prepare("DELETE FROM group_players WHERE group_id = ?");
        statement.setString(1, id);
        asyncMySQL.update(statement);

    }

    public Group findGroupById(String id) {
        try (PreparedStatement statement = asyncMySQL.prepare("SELECT * FROM groupsystem WHERE id = ?")) {
            statement.setString(1, id);
            ResultSet resultSet = asyncMySQL.query(statement);
            if (resultSet.next()) {
                return new Group(resultSet.getString("id"), resultSet.getString("name"), resultSet.getString("prefix"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @SneakyThrows
    public void updateGroup(Group group) {
        PreparedStatement statement = asyncMySQL.prepare("UPDATE groupsystem SET name = ?, prefix = ? WHERE id = ?");
            statement.setString(1, group.name());
            statement.setString(2, group.prefix());
            statement.setString(3, group.id());
                asyncMySQL.update(statement);
    }

}
