package de.therealdev.groupsystem.database.redis;

import de.therealdev.groupsystem.database.redis.RedisManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisManagerTest {


    @Mock
    private JedisPool jedisPool;

    @InjectMocks
    private RedisManager redisManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testSetValue() {
        try (Jedis jedisMock = mock(Jedis.class)) {
            when(jedisPool.getResource()).thenReturn(jedisMock);

            redisManager.setValueAsync("testKey", "testValue");

            verify(jedisMock).select(1); // Überprüfen Sie, ob die richtige DB ausgewählt wurde
            verify(jedisMock).set(eq("testKey"), eq("\"testValue\"")); // Überprüfen Sie, ob set aufgerufen wurde
        }
    }

    @Test
    void testGetValue() {
        try (Jedis jedisMock = mock(Jedis.class)) {
            when(jedisPool.getResource()).thenReturn(jedisMock);
            when(jedisMock.select(anyInt())).thenReturn("OK");
            when(jedisMock.get(anyString())).thenReturn("testValue");

            String value = redisManager.getValueAsync("testKey");

            assertEquals("testValue", value);
            verify(jedisMock).select(1); // Überprüfen Sie, ob die richtige DB ausgewählt wurde
            verify(jedisMock).get(eq("testKey")); // Überprüfen Sie, ob get aufgerufen wurde
        }
    }

    @Test
    void testSetTimedValue() {
        try (Jedis jedisMock = mock(Jedis.class)) {
            when(jedisPool.getResource()).thenReturn(jedisMock);
            when(jedisMock.select(anyInt())).thenReturn("OK");
            when(jedisMock.setex(anyString(), anyInt(), anyString())).thenReturn("OK");

            redisManager.setTimedValue("testKey", "testValue", 60);

            verify(jedisMock).select(1); // Überprüfen Sie, ob die richtige DB ausgewählt wurde
            verify(jedisMock).setex(eq("testKey"), eq(60), eq("\"testValue\"")); // Überprüfen Sie, ob setex aufgerufen wurde
        }
    }

}
