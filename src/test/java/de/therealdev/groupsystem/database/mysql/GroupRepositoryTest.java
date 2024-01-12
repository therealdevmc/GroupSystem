package de.therealdev.groupsystem.database.mysql;

import de.therealdev.groupsystem.model.group.Group;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class GroupRepositoryTest {

    private AsyncMySQL asyncMySQL;

    private GroupRepository groupRepository;

    private GroupPlayerRepository groupPlayerRepository;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        asyncMySQL = new AsyncMySQL();
        groupRepository = new GroupRepository(asyncMySQL);
        groupPlayerRepository = new GroupPlayerRepository(asyncMySQL, groupRepository);
        groupPlayerRepository.dropTables();
        groupRepository.dropTable();
        groupRepository.createTable();
        groupPlayerRepository.createGroupPlayerTable();
        groupPlayerRepository.createPlayerGroupTable();
        }

    @Test
    public void createGroup() {
        groupRepository.createGroup("test", "test", "test");
        assertEquals(groupRepository.findGroupById("test").id(), "test");
    }

    @Test
    public void updateGroup() {
        groupRepository.createGroup("test", "test", "test");
        Group group = new Group("test", "test", "test2");
        groupRepository.updateGroup(group);
        assertEquals(groupRepository.findGroupById("test").prefix(), "test2");
    }

    @Test
    public void deleteGroup() {
        groupRepository.createGroup("test", "test", "test");

        groupRepository.deleteGroup(groupRepository.findGroupById("test"));
        assertNull(groupRepository.findGroupById("test"));

    }


    @AfterEach
    void tearDown() {
        asyncMySQL.getMySQL().closeConnection();
    }

}

