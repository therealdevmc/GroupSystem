package de.therealdev.groupsystem.manager;


import de.therealdev.groupsystem.database.redis.RedisManager;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Setter
public abstract class AbstractManager<T> {

    public JedisPool jedisPool;
    public RedisManager redisManager;

    public Class<T> c;

    /**
     * AbstractManager
     * @param jedisPool
     * @param redisDb
     * @param c
     */
    public AbstractManager(JedisPool jedisPool, int redisDb, Class<T> c) {
        this.jedisPool = jedisPool;
        redisManager = new RedisManager(jedisPool, redisDb);
        this.c = c;
    }


    /**
     * findObject
     * @param uuid
     * @return
     * @throws Exception
     */
    public void deleteObject(T object) {
        redisManager.setTimedValueAsync(getObjectID(object), object, 0);
    }

    /**
     * findObject
     * @param uuid
     * @return
     *
     */
    public T getLocalObject(String uuid) {
        return (T) redisManager.getAsyncObject(uuid, c);
    }


    /**
     * findObject
     * @param object
     * @return
     */
    @SneakyThrows
    public String getObjectID(T object) {
        return c.getMethod("id").invoke(object).toString();
    }


    /**
     * updateObjectRedisTimed
     * @param object
     * @param time
     */
    public void updateObjectRedisTimed(T object, int time) {
        if (object == null) return;
        redisManager.setTimedValueAsync(getObjectID(object), object, time);
    }

    /**
     * updateLocalObject
     * @param object
     */
    public void updateLocalObject(T object) {
        if (object == null) return;
        T localEntity = getLocalObject(getObjectID(object));
        if (localEntity != null) {
            redisManager.setValueAsync(getObjectID(object), localEntity);
        }
    }

    /**
     * pushObject
     * @param uuid
     */
    public void pushObject(String uuid) {
        T localEntity = getLocalObject(uuid);
        redisManager.setTimedValueAsync(uuid, localEntity, 60);
    }

    /**
     * findAllObjects
     * @return
     */
    public List<T> findAllObjects() {
        List<T> allObjects = new ArrayList<>();


        ScanParams scanParams = new ScanParams().count(100);
        String cursor = "0";
        do {

            ScanResult<String> scanResult = jedisPool.getResource().scan(cursor, scanParams);

            List<String> keys = scanResult.getResult();

            for (String key : keys) {
                T object = redisManager.getAsyncObject(key, c);
                if (object != null) {
                    allObjects.add(object);
                }
            }

            cursor = scanResult.getStringCursor();

        } while (!cursor.equals("0"));

        return allObjects;
    }

}

