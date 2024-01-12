package de.therealdev.groupsystem.manager;

import de.therealdev.groupsystem.database.mysql.AsyncMySQL;
import de.therealdev.groupsystem.database.mysql.GroupPlayerRepository;
import de.therealdev.groupsystem.database.mysql.GroupRepository;
import de.therealdev.groupsystem.database.redis.RedisManager;
import de.therealdev.groupsystem.model.group.Group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupManagerTest {

    private AsyncMySQL asyncMySQL;

    private GroupManager groupManager;

    private GroupRepository groupRepository;

    private GroupPlayerRepository groupPlayerRepository;

    private JedisPool jedisPool;

    private Jedis jedis;

    private RedisManager redisManager;

    @BeforeEach
    public void setUp() {
        asyncMySQL = new AsyncMySQL();
        groupRepository = new GroupRepository(asyncMySQL);
        groupPlayerRepository = new GroupPlayerRepository(asyncMySQL, groupRepository);
        groupRepository.createTable();
        groupPlayerRepository.createGroupPlayerTable();
        groupPlayerRepository.createPlayerGroupTable();
        jedisPool = mock(JedisPool.class);
        jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        redisManager = mock(RedisManager.class);
        groupManager = new GroupManager(jedisPool, 1, groupRepository);
    }

    @Test
    public void testCreateGroup() {
        // Teste, ob eine Gruppe erstellt und in Redis gespeichert wird
        groupManager.createGroup("TestGroup", "TestPrefix");
        Group group = groupManager.findGroup("TestGroup");

        assertNotNull(group);
        assertEquals("TestGroup", group.id());
        assertEquals("TestGroup", group.name());
        assertEquals("TestPrefix", group.prefix());
    }

    @Test
    public void testUpdateGroup() {
        // Teste, ob eine Gruppe aktualisiert und in Redis aktualisiert wird
        groupManager.createGroup("TestGroup", "OldPrefix");
        groupManager.updateGroup("TestGroup", "NewPrefix");

        Group updatedGroup = groupManager.findGroup("TestGroup");

        assertNotNull(updatedGroup);
        assertEquals("TestGroup", updatedGroup.id());
        assertEquals("TestGroup", updatedGroup.name());
        assertEquals("NewPrefix", updatedGroup.prefix());
    }

    @Test
    public void testRemoveGroup() {
        // Teste, ob eine Gruppe erfolgreich entfernt wird
        groupManager.createGroup("TestGroupToRemove", "TestPrefix");
        groupManager.removeGroup("TestGroupToRemove");

        Group removedGroup = groupManager.findGroup("TestGroupToRemove");

        assertNull(removedGroup);
    }

    @Test
    public void testPushGroup() {
        // Teste, ob eine Gruppe erfolgreich gepusht und in Redis aktualisiert wird
        groupManager.createGroup("TestGroupToPush", "TestPrefix");
        groupManager.pushGroup("TestGroupToPush");

        Group pushedGroup = groupManager.findGroup("TestGroupToPush");

        assertNotNull(pushedGroup);
        assertTrue(pushedGroup.name().equals("TestGroupToPush")); // Hier könntest du weitere Überprüfungen durchführen
    }

    @Test
    public void testParseDuration() {
        // Teste die Parse-Duration-Methode
        Long duration = groupManager.parseDuration("1", "2", "30", "45");

        assertNotNull(duration);
        assertTrue(duration > System.currentTimeMillis());
    }

    // Hier könntest du weitere Tests hinzufügen, abhängig von der Logik in deinem Code

    // Mock-Methoden zum Erstellen von Mock-Objekten
   @AfterEach
    public void tearDown() {
        asyncMySQL.getMySQL().closeConnection();
        jedis.close();
        jedisPool.close();
    }

}
