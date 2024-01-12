package de.therealdev.groupsystem.listeners;

import de.therealdev.groupsystem.manager.GroupPlayerManager;
import de.therealdev.groupsystem.manager.message.MessageManager;
import de.therealdev.groupsystem.model.player.GroupPlayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

@RequiredArgsConstructor
public class SignListener implements Listener {

    private final GroupPlayerManager groupPlayerManager;
    private final MessageManager messageManager;

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();
        if (lines[1].contains("[GroupSystem]")) {
            String playerName = lines[2];
            Player target = Bukkit.getPlayer(playerName);
            if(target == null) {
                event.setCancelled(true);
                player.sendMessage(messageManager.getMessage("messages.playernotfound"));
                return;
            }
            GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer(target.getUniqueId().toString());
            if(groupPlayer == null) {
                event.setCancelled(true);
                player.sendMessage(messageManager.getMessage("messages.playernotfound"));
                return;
            }
            if(groupPlayer.groups().isEmpty()) {
                event.setCancelled(true);
                player.sendMessage(messageManager.getMessage("messages.notingroup"));
                return;
            }
            event.setLine(0, "");
            event.setLine(1, messageManager.getMessage("messages.signplayer").replace("%player", playerName));
            event.setLine(2, messageManager.getMessage("messages.signprefix").replace("%prefix", groupPlayer.groups().stream().findFirst().get().group().prefix()));
            event.setLine(3, "");
        }
    }

}
