package de.therealdev.groupsystem.listeners;

import de.therealdev.groupsystem.manager.GroupPlayerManager;
import de.therealdev.groupsystem.manager.message.MessageManager;
import de.therealdev.groupsystem.model.player.GroupPlayer;
import de.therealdev.groupsystem.model.player.PlayerGroup;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

@RequiredArgsConstructor
public class ChatListener implements Listener {

    private final GroupPlayerManager groupPlayerManager;
    private final MessageManager messageManager;

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer(player.getUniqueId().toString());
        Optional<PlayerGroup> playerGroup = groupPlayer.groups().stream().findFirst();
        if(playerGroup.isPresent()) {
            PlayerGroup playerGroup1 = playerGroup.get();
            event.setFormat(messageManager.getMessage("messages.chat").replace("%player", player.getName()).replace("%prefix", playerGroup1.group().prefix()) + event.getMessage());
        }
    }

}
