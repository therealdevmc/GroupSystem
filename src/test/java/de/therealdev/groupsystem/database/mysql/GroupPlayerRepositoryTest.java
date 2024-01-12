package de.therealdev.groupsystem.database.mysql;

import de.therealdev.groupsystem.model.group.Group;
import de.therealdev.groupsystem.model.player.GroupPlayer;
import de.therealdev.groupsystem.model.player.PlayerGroup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class GroupPlayerRepositoryTest {

    private AsyncMySQL asyncMySQL;

    private GroupRepository groupRepository;

    private GroupPlayerRepository groupPlayerRepository;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        asyncMySQL = new AsyncMySQL();
        // Mocking behaviors for AsyncMySQL
        groupRepository = new GroupRepository(asyncMySQL);
        groupPlayerRepository = new GroupPlayerRepository(asyncMySQL, groupRepository);
        groupPlayerRepository.dropTables();
        groupRepository.dropTable();
        groupRepository.createTable();
        groupPlayerRepository.createGroupPlayerTable();
        groupPlayerRepository.createPlayerGroupTable();
        Group group = new Group("test", "test", "test");
        groupRepository.createGroup("test", "test", "test");
        groupPlayerRepository.createGroupPlayer(new GroupPlayer("test", null));
        groupPlayerRepository.addGroup("test1", group, groupPlayerRepository.findById("test"), false, 0L);
    }

    @Test
    public void createGroup() {
        Group group = groupRepository.findGroupById("test");
        PlayerGroup playerGroup = new PlayerGroup("test123", group, 0L, false);
        groupPlayerRepository.addGroup("test123", group, groupPlayerRepository.findById("test"), false, 0L);
        assertEquals(groupPlayerRepository.findGroupsByPlayer("test").get(1), playerGroup);

    }

    @Test
    public void createPlayer() {
        groupPlayerRepository.createGroupPlayer(new GroupPlayer("test123", null));
        assertEquals(groupPlayerRepository.findById("test123").id(), "test123");
    }

    @Test
    public void removeGroup() {
        groupPlayerRepository.removeGroup("test", "test");
        assertTrue(groupPlayerRepository.findGroupsByPlayer("test").isEmpty());

    }

    @AfterEach
    void tearDown() {
        asyncMySQL.getMySQL().closeConnection();
    }

}
