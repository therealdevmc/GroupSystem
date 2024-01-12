package de.therealdev.groupsystem.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import de.therealdev.groupsystem.manager.GroupManager;
import de.therealdev.groupsystem.manager.GroupPlayerManager;
import de.therealdev.groupsystem.manager.message.MessageManager;
import de.therealdev.groupsystem.model.group.Group;
import de.therealdev.groupsystem.model.player.GroupPlayer;
import de.therealdev.groupsystem.model.player.PlayerGroup;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

@CommandAlias("group|g|gr|groups")
@RequiredArgsConstructor
public class GroupCommand extends BaseCommand {

    private final GroupPlayerManager groupPlayerManager;
    private final GroupManager groupManager;
    private final MessageManager messageManager;

    @Subcommand("help")
    @Default
    @CatchUnknown
    public void onHelp(CommandSender sender) {
        sender.sendMessage(messageManager.getMessage("messages.default"));
    }

    @Subcommand("create")
    @Syntax("/create <GroupName> <Prefix>")
    public void onCreate(CommandSender commandSender, String name, String prefix) {
        if(!commandSender.hasPermission("group.admin"))return;
        if(name == null){
            commandSender.sendMessage(messageManager.getMessage("messages.name"));
            return;
        }
        if(prefix == null){
            commandSender.sendMessage(messageManager.getMessage("messages.prefix"));
            return;
        }

        Group group = groupManager.findGroup(name);
        if(group != null) {
            commandSender.sendMessage(messageManager.getMessage("messages.exists"));
            return;
        }

        groupManager.createGroup(name, prefix);
        commandSender.sendMessage(messageManager.getMessage("messages.created"));
    }

    @Subcommand("edit")
    @Syntax("/edit <GroupName> <Prefix>")
    public void onEdit(CommandSender commandSender, String name, String prefix) {
        if(!commandSender.hasPermission("group.admin"))return;
        if(name == null){
            commandSender.sendMessage(messageManager.getMessage("messages.name"));
            return;
        }
        if(prefix == null){
            commandSender.sendMessage(messageManager.getMessage("messages.prefix"));
            return;
        }

        Group group = groupManager.findGroup(name);
        if(group == null) {
            commandSender.sendMessage(messageManager.getMessage("messages.notfound"));
            return;
        }

        groupManager.updateGroup(name, prefix);
        groupPlayerManager.updateGroup(group.id());
        commandSender.sendMessage(messageManager.getMessage("messages.edited"));
    }

    @Subcommand("delete")
    @Syntax("/delete <GroupName>")
    public void onRemove(CommandSender commandSender, String name) {
        if(!commandSender.hasPermission("group.admin"))return;
        if(name == null){
            commandSender.sendMessage(messageManager.getMessage("messages.name"));
            return;
        }

        Group group = groupManager.findGroup(name);
        if(group == null) {
            commandSender.sendMessage(messageManager.getMessage("messages.notfound"));
            return;
        }

        groupManager.removeGroup(name);
        groupPlayerManager.deletGroup(group.id());
        commandSender.sendMessage(messageManager.getMessage("messages.deleted"));
    }

    @Subcommand("add")
    @Syntax("/add <GroupName> <Player> [days] [hours] [minutes] [seconds]")
    public void addPlayer(CommandSender commandSender, String name, String playerName, @Optional String days, @Optional String hours, @Optional String minutes, @Optional String seconds) {
        if(!commandSender.hasPermission("group.admin"))return;
        if(name == null){
            commandSender.sendMessage(messageManager.getMessage("messages.name"));
            return;
        }
        if(playerName == null){
            commandSender.sendMessage(messageManager.getMessage("messages.player"));
            return;
        }


        Group group = groupManager.findGroup(name);
        if(group == null) {
            commandSender.sendMessage(messageManager.getMessage("messages.notfound"));
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if(!offlinePlayer.hasPlayedBefore()) {
            commandSender.sendMessage(messageManager.getMessage("messages.playernotfound"));
            return;
        }
        boolean timed = false;
        Long valid = 0L;
        if(days != null || hours != null || minutes != null || seconds != null) {
            valid = groupManager.parseDuration(days, hours, minutes, seconds);
            timed = true;
            if(valid == null) {
                commandSender.sendMessage(messageManager.getMessage("messages.input"));
                return;
            }
        }
        GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer(offlinePlayer.getUniqueId().toString());
        if(groupPlayer == null) {
            commandSender.sendMessage(messageManager.getMessage("messages.playernotfound"));
            return;
        }
        if(groupPlayer.groups().size() > 0) {
            commandSender.sendMessage(messageManager.getMessage("messages.alreadyingroup"));
            return;
        }

        groupPlayerManager.addGroup(offlinePlayer.getUniqueId().toString(), group.id(), valid, timed);
        commandSender.sendMessage(messageManager.getMessage("messages.added"));
    }

    @Subcommand("remove")
    @Syntax("/remove <GroupName> <Player>")
    public void removePlayer(CommandSender commandSender, String name, String playerName) {
        if(!commandSender.hasPermission("group.admin"))return;
        if(name == null){
            commandSender.sendMessage(messageManager.getMessage("messages.name"));
            return;
        }
        if(playerName == null){
            commandSender.sendMessage(messageManager.getMessage("messages.player"));
            return;
        }


        Group group = groupManager.findGroup(name);
        if(group == null) {
            commandSender.sendMessage(messageManager.getMessage("messages.notfound"));
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if(!offlinePlayer.hasPlayedBefore()) {
            commandSender.sendMessage(messageManager.getMessage("messages.playernotfound"));
            return;
        }
        GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer(offlinePlayer.getUniqueId().toString());
        if(groupPlayer == null) {
            commandSender.sendMessage(messageManager.getMessage("messages.notingroup"));
            return;
        }
        if(groupPlayer.groups().size() == 0) {
            commandSender.sendMessage(messageManager.getMessage("messages.notingroup"));
            return;
        }

        PlayerGroup playerGroup = groupPlayerManager.findByGroupAndPlayer(offlinePlayer.getUniqueId().toString(), group.id());
        if(playerGroup == null) {
            commandSender.sendMessage(messageManager.getMessage("messages.notingroup"));
            return;
        }
        groupPlayerManager.removeGroup(offlinePlayer.getUniqueId().toString(), group.id(), playerGroup);
        commandSender.sendMessage(messageManager.getMessage("messages.removed"));
    }

    @Subcommand("info")
    @Syntax("/info [player]")
    public void info(CommandSender commandSender, @Optional String playerName) {
        if(playerName != null){
            if(!commandSender.hasPermission("group.admin"))return;
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            if(!offlinePlayer.hasPlayedBefore()) {
                commandSender.sendMessage(messageManager.getMessage("messages.playernotfound"));
                return;
            }
            GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer(offlinePlayer.getUniqueId().toString());
            if(groupPlayer == null) {
                commandSender.sendMessage(messageManager.getMessage("messages.playernotfound"));
                return;
            }
            if(groupPlayer.groups().size() == 0) {
                commandSender.sendMessage(messageManager.getMessage("messages.notingroup"));
                return;
            }
            commandSender.sendMessage(messageManager.getMessage("messages.groupplayer"));
            for(PlayerGroup playerGroup : groupPlayer.groups()) {
                if(playerGroup.group() == null) continue;
                commandSender.sendMessage(messageManager.getMessage("messages.infos").replace("%name", playerGroup.group().name()).replace("%prefix", playerGroup.group().prefix()));
                if(playerGroup.timed()) {
                    sendInfo(playerGroup, commandSender);
                }
            }
            return;
        }
        if(!(commandSender instanceof Player)){
            return;
        }
        Player player = (Player) commandSender;

        GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer(player.getUniqueId().toString());
        if(groupPlayer == null) {
            commandSender.sendMessage(messageManager.getMessage("messages.error"));
            return;
        }
        if(groupPlayer.groups().size() == 0) {
            commandSender.sendMessage(messageManager.getMessage("messages.notingroupplayer"));
            return;
        }
        commandSender.sendMessage(messageManager.getMessage("messages.group"));
        for(PlayerGroup playerGroup : groupPlayer.groups()) {
            commandSender.sendMessage(messageManager.getMessage("messages.infos").replace("%name", playerGroup.group().name()).replace("%prefix", playerGroup.group().prefix()));
            if(playerGroup.timed()) {
                sendInfo(playerGroup, commandSender);
            }
        }
    }

    private void sendInfo(PlayerGroup playerGroup, CommandSender commandSender) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(playerGroup.valid());
        commandSender.sendMessage(messageManager.getMessage("messages.end").replace("%date", sdf.format(resultdate)));

    }

}
