package de.therealdev.groupsystem.manager;

import de.therealdev.groupsystem.database.mysql.AsyncMySQL;
import de.therealdev.groupsystem.database.mysql.GroupPlayerRepository;
import de.therealdev.groupsystem.database.mysql.GroupRepository;
import de.therealdev.groupsystem.database.redis.RedisManager;
import de.therealdev.groupsystem.model.player.GroupPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupPlayerManagerTest {
    private AsyncMySQL asyncMySQL;

    private GroupManager groupManager;

    private GroupRepository groupRepository;

    private GroupPlayerRepository groupPlayerRepository;

    private JedisPool jedisPool;

    private Jedis jedis;

    private RedisManager redisManager;

    private GroupPlayerManager groupPlayerManager;


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
        groupPlayerManager = new GroupPlayerManager(jedisPool, 1, groupPlayerRepository, groupManager);
    }

    @Test
    public void testCreatePlayer() {
        groupPlayerManager.createGroupPlayer("TestPlayer");
        GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer("TestPlayer");

        assertNotNull(groupPlayer);
        assertEquals("TestPlayer", groupPlayer.id());
    }

    @Test
    public void testAddGroup() {
        groupPlayerManager.createGroupPlayer("TestPlayer");
        groupManager.createGroup("TestGroup", "TestPrefix");
        groupPlayerManager.addGroup("TestPlayer", "TestGroup", 0L, false);
        GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer("TestPlayer");

        assertEquals(1, groupPlayer.groups().size());
        assertEquals("TestGroup", groupPlayer.groups().get(0).group().id());
    }

    @Test
    public void testRemoveGroup() {
        groupPlayerManager.createGroupPlayer("TestPlayer");
        groupManager.createGroup("TestGroup", "TestPrefix");
        groupPlayerManager.addGroup("TestPlayer", "TestGroup", 0L, false);
        groupPlayerManager.removeGroup("TestPlayer", "TestGroup", groupPlayerManager.findGroupPlayer("TestPlayer").groups().get(0));
        GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer("TestPlayer");

        assertEquals(0, groupPlayer.groups().size());
    }


    @AfterEach
    public void tearDown() {
        asyncMySQL.getMySQL().closeConnection();
        jedis.close();
        jedisPool.close();
    }
}
