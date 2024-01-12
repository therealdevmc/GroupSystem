package de.therealdev.groupsystem.manager;

import de.therealdev.groupsystem.database.mysql.GroupRepository;
import de.therealdev.groupsystem.model.group.Group;
import redis.clients.jedis.JedisPool;


public class GroupManager extends AbstractManager<Group> {

    private final GroupRepository groupRepository;


    private static final int CACHE_EXPIRATION_TIME_SECONDS = 3600;

    /**
     * GroupManager
     * @param jedisPool
     * @param redisDb
     * @param groupRepository
     */
    public GroupManager(JedisPool jedisPool, int redisDb, GroupRepository groupRepository) {
        super(jedisPool, redisDb, Group.class);
        this.groupRepository = groupRepository;
    }


    /**
     * createGroup
     * @param name
     * @param prefix
     */

    public void createGroup(String name, String prefix) {
        String id = name;
        groupRepository.createGroup(id, name, prefix);
        Group group = new Group(id, name, prefix);
        updateObjectRedisTimed(group, CACHE_EXPIRATION_TIME_SECONDS);
    }


    /**
     * updateGroup
     * @param name
     * @param prefix
     */
    public void updateGroup(String name, String prefix) {
        Group group = findGroup(name);
        if(group == null) return;
        group = new Group(name, name, prefix);
        groupRepository.updateGroup(group);
        updateObjectRedisTimed(group, CACHE_EXPIRATION_TIME_SECONDS);
    }


    /**
     * removeGroup
     * @param id
     */

    public void removeGroup(String id) {
        Group group = findGroup(id);
        if(group == null) return;
        deleteObject(group);
        groupRepository.deleteGroup(group);
    }

    /**
     * pushGroup
     * @param id
     */

    public void pushGroup(String id) {
        Group group = findGroup(id);
        if(group == null) return;
        groupRepository.updateGroup(group);
        updateObjectRedisTimed(group, 300);
    }


    /**
     * findGroup
     * @param id
     * @return
     */
    public Group findGroup(String id) {
        Group group = getLocalObject(id);
        if(group != null) return group;
        return groupRepository.findGroupById(id);
    }


    /**
     * parseDuration
     * @param days
     * @param hours
     * @param minutes
     * @param seconds
     * @return
     */
    public Long parseDuration(String days, String hours, String minutes, String seconds) {
        try {
            long time = 0;

            if (days != null && !days.isEmpty()) {
                time += Long.parseLong(days) * 24 * 60 * 60*1000;
            }
            if (hours != null && !hours.isEmpty()) {
                time += Long.parseLong(hours) * 60 * 60*1000;
            }
            if (minutes != null && !minutes.isEmpty()) {
                time += Long.parseLong(minutes) * 60*1000;
            }
            if (seconds != null && !seconds.isEmpty()) {
                time += Long.parseLong(seconds)*1000;
            }

            if (time > 0) {
                time += System.currentTimeMillis();
                return time;
            } else {
                return null;
            }
        } catch (NumberFormatException ex) {
            return null;
        }
    }






}
