package de.therealdev.groupsystem.database.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Setter
public class RedisManager {

    private JedisPool jedisPool;
    private Gson gson;
    private int db;
    private Logger logger;

    /**
     * Creates a new RedisManager
     * @param jedisPool
     * @param db
     */
    public RedisManager(JedisPool jedisPool, int db) {
        this.jedisPool = jedisPool;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        this.db = db;
        logger = Logger.getLogger("RedisManager");
    }

    public RedisManager() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        logger = Logger.getLogger("RedisManager");
        this.db = 1;
    }

    /**
     * Sets a value in the redis database
     * @param key
     * @param value
     */
    public void setValue(Object key, Object value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(db);
            jedis.set(String.valueOf(key), toJsonString(value));
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Sets a value in the redis database asynchronously
     * @param key
     * @param value
     */
    public void setValueAsync(Object key, Object value) {
        CompletableFuture.runAsync(() -> setValue(key, value))
                .exceptionally(ex -> {
                    logger.severe("Error in asynchronous operation: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }

    /**
     * Converts an object to a json string
     * @param value
     * @return
     */

    public String toJsonString(Object value) {
        return gson.toJson(value);
    }

    /**
     * Gets a value from the redis database asynchronously
     * @param key
     * @return
     */
    public String getValueAsync(Object key) {
        return CompletableFuture.supplyAsync(() -> getValue(key))
                .exceptionally(ex -> {
                    logger.severe("Error in asynchronous operation: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                }).join();
    }

    /**
     * Gets a value from the redis database
     * @param key
     * @return
     */

    public String getValue(Object key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(db);
            return jedis.get(String.valueOf(key));
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    /**
     * Gets an object from the redis database asynchronously
     * @param key
     * @param c
     * @return
     * @param <T>
     */

    public <T> T getAsyncObject(Object key, Class<T> c) {
        return CompletableFuture.supplyAsync(() -> getObject(key, c))
                .exceptionally(ex -> {
                    handleAsyncException(ex);
                    return null;
                }).join();
    }

    /**
     * Gets an object from the redis database
     * @param key
     * @param c
     * @return
     * @param <T>
     */

    public <T> T getObject(Object key, Class<T> c) {
        return gson.fromJson(getValue(key), c);
    }

    /**
     * Sets a timed value in the redis database asynchronously
     * @param key
     * @param value
     * @param seconds
     */

    public void setTimedValueAsync(Object key, Object value, int seconds) {
        CompletableFuture.runAsync(() -> setTimedValue(key, value, seconds))
                .exceptionally(ex -> {
                    handleAsyncException(ex);
                    return null;
                });
    }

    /**
     * Sets a timed value in the redis database
     * @param key
     * @param value
     * @param seconds
     */
    public void setTimedValue(Object key, Object value, int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(db);
            jedis.setex(String.valueOf(key), seconds, toJsonString(value));
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Handles an asynchronous exception
     * @param ex
     */
    private void handleAsyncException(Throwable ex) {
        logger.severe("Error in asynchronous operation: " + ex.getMessage());
        ex.printStackTrace();
    }

    /**
     * Handles an exception
     * @param e
     */
    private void handleException(Exception e) {
        logger.severe("Error while interacting with Jedis: " + e.getMessage());
        e.printStackTrace();
    }

}
