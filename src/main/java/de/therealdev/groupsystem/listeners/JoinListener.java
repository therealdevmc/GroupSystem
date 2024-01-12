package de.therealdev.groupsystem.listeners;

import de.therealdev.groupsystem.manager.GroupPlayerManager;
import de.therealdev.groupsystem.manager.message.MessageManager;
import de.therealdev.groupsystem.model.player.GroupPlayer;
import de.therealdev.groupsystem.model.player.PlayerGroup;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

@RequiredArgsConstructor
public class JoinListener implements Listener {

    private final GroupPlayerManager groupPlayerManager;
    private final MessageManager messageManager;

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer(player.getUniqueId().toString());
        Optional<PlayerGroup> playerGroup = groupPlayer.groups().stream().findFirst();
        if(playerGroup.isPresent()) {
            PlayerGroup playerGroup1 = playerGroup.get();
            e.setJoinMessage(messageManager.getMessage("messages.join").replace("%player", player.getName()).replace("%prefix", playerGroup1.group().prefix()));
        }
    }

}
