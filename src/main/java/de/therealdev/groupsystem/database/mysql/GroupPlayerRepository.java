package de.therealdev.groupsystem.database.mysql;

import de.therealdev.groupsystem.model.group.Group;
import de.therealdev.groupsystem.model.player.GroupPlayer;
import de.therealdev.groupsystem.model.player.PlayerGroup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class GroupPlayerRepository {

    private final AsyncMySQL asyncMySQL;

    private final GroupRepository groupRepository;

    public void dropTables() {
        try (PreparedStatement statement = asyncMySQL.prepare("DROP TABLE IF EXISTS group_players")) {
            asyncMySQL.update(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try (PreparedStatement statement = asyncMySQL.prepare("DROP TABLE IF EXISTS group_player")) {
            asyncMySQL.update(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createGroupPlayerTable() {
        try (PreparedStatement statement = asyncMySQL.prepare("CREATE TABLE IF NOT EXISTS group_player (id VARCHAR(36) PRIMARY KEY)")) {
            asyncMySQL.update(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void createGroupPlayer(GroupPlayer groupPlayer) {
        PreparedStatement statement = asyncMySQL.prepare("INSERT INTO group_player (id) VALUES (?)");
        statement.setString(1, groupPlayer.id());
        asyncMySQL.update(statement);
    }


    @SneakyThrows
    public void createPlayerGroupTable() {
        PreparedStatement statement = asyncMySQL.prepare("CREATE TABLE IF NOT EXISTS group_players (\n" +
                "    id VARCHAR(36) PRIMARY KEY,\n" +
                "    group_id VARCHAR(36) REFERENCES groupsystem(id),\n" +
                "    player_id VARCHAR(36) REFERENCES group_player(id),\n" +
                "    timed BOOLEAN,\n" +
                "    valid BIGINT\n" +
                ");\n");
        asyncMySQL.update(statement);
    }

    public GroupPlayer findById(String id) {
        try (PreparedStatement statement = asyncMySQL.prepare("SELECT * FROM group_player WHERE id = ?")) {
            statement.setString(1, id);
            ResultSet resultSet = asyncMySQL.query(statement);
            return resultSet.next() ? new GroupPlayer(id, findGroupsByPlayer(id)) : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PlayerGroup> findGroupsByPlayer(String playerId) {
        List<PlayerGroup> groupList = new ArrayList<>();
        try (PreparedStatement statement = asyncMySQL.prepare("SELECT id, group_id, timed, valid  FROM group_players WHERE player_id = ?")) {
            statement.setString(1, playerId);
            ResultSet resultSet = asyncMySQL.query(statement);
            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String groupId = resultSet.getString("group_id");
                boolean timed = resultSet.getBoolean("timed");
                long valid = resultSet.getLong("valid");
                Group group = groupRepository.findGroupById(groupId);
                PlayerGroup playerGroup = new PlayerGroup(id, group, valid, timed);
                groupList.add(playerGroup);
                if(timed) {
                    if(valid <= System.currentTimeMillis()) {
                        removeGroup(groupId, playerId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groupList;
    }

    @SneakyThrows
    public void addGroup(String id, Group group, GroupPlayer groupPlayer, boolean timed, long valid) {
        PreparedStatement statement = asyncMySQL.prepare("INSERT INTO group_players (id, group_id, player_id, timed, valid) VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, group.id());
            statement.setString(3, groupPlayer.id());
            statement.setBoolean(4, timed);
            statement.setLong(5, valid);
            asyncMySQL.update(statement);
    }

    @SneakyThrows
    public void removeGroup(String groupId, String playerId) {
       PreparedStatement statement = asyncMySQL.prepare("DELETE FROM group_players WHERE group_id = ? AND player_id = ?");
            statement.setString(1, groupId);
            statement.setString(2, playerId);
            asyncMySQL.update(statement);

    }


}
