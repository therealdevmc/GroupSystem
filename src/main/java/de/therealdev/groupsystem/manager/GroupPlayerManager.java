package de.therealdev.groupsystem.manager;

import de.therealdev.groupsystem.database.mysql.GroupPlayerRepository;
import de.therealdev.groupsystem.model.group.Group;
import de.therealdev.groupsystem.model.player.GroupPlayer;
import de.therealdev.groupsystem.model.player.PlayerGroup;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.UUID;

/**
 * GroupPlayerManager
 */
public class GroupPlayerManager extends AbstractManager<GroupPlayer> {

    private final GroupPlayerRepository groupPlayerRepository;
    private final GroupManager groupManager;

    private static final int CACHE_EXPIRATION_TIME_SECONDS = 3600;

    /**
     * GroupPlayerManager
     * @param jedisPool
     * @param redisDb
     * @param groupPlayerRepository
     * @param groupManager
     */
    public GroupPlayerManager(JedisPool jedisPool, int redisDb,GroupPlayerRepository groupPlayerRepository, GroupManager groupManager) {
        super(jedisPool, redisDb, GroupPlayer.class);
        this.groupPlayerRepository = groupPlayerRepository;
        this.groupManager = groupManager;
    }

    /**
     * createGroupPlayer
     * @param id
     */
    public void createGroupPlayer(String id) {
        GroupPlayer groupPlayer = new GroupPlayer(id, new ArrayList<>());
        groupPlayerRepository.createGroupPlayer(groupPlayer);
        GroupPlayer groupPlayerNew = findGroupPlayer(id);
        if(groupPlayerNew != null)
            updateObjectRedisTimed(groupPlayerNew, CACHE_EXPIRATION_TIME_SECONDS);
        else
            updateObjectRedisTimed(groupPlayer, CACHE_EXPIRATION_TIME_SECONDS);
    }


    /**
     * updateGroup
     * @param groupId
     */
    public void updateGroup(String groupId) {
        findAllObjects().stream().filter(groupPlayer -> groupPlayer.groups().stream().anyMatch(playerGroup -> playerGroup.group().id().equals(groupId))).forEach(groupPlayer -> {
            PlayerGroup playerGroup = groupPlayer.groups().stream().filter(playerGroup1 -> playerGroup1.group().id().equals(groupId)).findFirst().orElse(null);
            if(playerGroup != null) {
                playerGroup = new PlayerGroup(playerGroup.id(), groupManager.findGroup(groupId), playerGroup.valid(), playerGroup.timed());
                groupPlayer.groups().removeIf(playerGroup1 -> playerGroup1.group().id().equals(groupId));
                groupPlayer.groups().add(playerGroup);
                updateObjectRedisTimed(groupPlayer, CACHE_EXPIRATION_TIME_SECONDS);
            }
        });
    }

    /**
     * deletGroup
     * @param groupId
     */
    public void deletGroup(String groupId) {
        findAllObjects().stream().filter(groupPlayer -> groupPlayer.groups().stream().anyMatch(playerGroup -> playerGroup.group().id().equals(groupId))).forEach(groupPlayer -> {
            PlayerGroup playerGroup = groupPlayer.groups().stream().filter(playerGroup1 -> playerGroup1.group().id().equals(groupId)).findFirst().orElse(null);
            if(playerGroup != null) {
                groupPlayer.groups().removeIf(playerGroup1 -> playerGroup1.group().id().equals(groupId));
                updateObjectRedisTimed(groupPlayer, CACHE_EXPIRATION_TIME_SECONDS);
            }
        });
    }


    /**
     * addGroup
     * @param playerId
     * @param groupId
     * @param valid
     * @param timed
     */
    public void addGroup(String playerId, String groupId, Long valid, boolean timed) {
        String id = UUID.randomUUID().toString();
        GroupPlayer groupPlayer = findGroupPlayer(playerId);
        Group group = groupManager.findGroup(groupId);
        groupPlayerRepository.addGroup(id, group, groupPlayer, timed, valid);
        groupPlayer.groups().add(new PlayerGroup(id, group, valid, timed));
        updateObjectRedisTimed(groupPlayer, CACHE_EXPIRATION_TIME_SECONDS);
    }


    /**
     * removeGroup
     * @param playerId
     * @param groupId
     * @param playerGroup
     */
    public void removeGroup(String playerId, String groupId, PlayerGroup playerGroup) {
        GroupPlayer groupPlayer = findGroupPlayer(playerId);
        groupPlayer.groups().remove(playerGroup);
        groupPlayerRepository.removeGroup(groupId, playerId);
        updateObjectRedisTimed(groupPlayer, CACHE_EXPIRATION_TIME_SECONDS);
    }


    /**
     * findByGroupAndPlayer
     * @param playerId
     * @param groupId
     * @return
     */
    public PlayerGroup findByGroupAndPlayer(String playerId, String groupId) {
        GroupPlayer groupPlayer = findGroupPlayer(playerId);
        if(groupPlayer == null) return null;
        return groupPlayer.groups().stream().filter(playerGroup -> playerGroup.group().id().equals(groupId)).findFirst().orElse(null);
    }

    /**
     * findGroupPlayer
     * @param id
     * @return
     */

    public GroupPlayer findGroupPlayer(String id) {
        GroupPlayer groupPlayer = getLocalObject(id);

        if (groupPlayer != null) {
            removeGroupTimed(groupPlayer);
            return groupPlayer;
        }

        groupPlayer = groupPlayerRepository.findById(id);

        if (groupPlayer != null) {
            updateObjectRedisTimed(groupPlayer, CACHE_EXPIRATION_TIME_SECONDS);
        }

        return groupPlayer;
    }

    /**
     * removeGroupTimed
     * @param groupPlayer
     */
    private void removeGroupTimed(GroupPlayer groupPlayer) {
        groupPlayer.groups().removeIf(playerGroup -> playerGroup.timed() && playerGroup.valid() <= System.currentTimeMillis());
    }

}
