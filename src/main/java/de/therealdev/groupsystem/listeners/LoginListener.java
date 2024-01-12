package de.therealdev.groupsystem.listeners;

import de.therealdev.groupsystem.manager.GroupPlayerManager;
import de.therealdev.groupsystem.model.player.GroupPlayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

@RequiredArgsConstructor
public class LoginListener implements Listener {

    private final GroupPlayerManager groupPlayerManager;

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        GroupPlayer groupPlayer = groupPlayerManager.findGroupPlayer(e.getUniqueId().toString());
       if(groupPlayer == null)
            groupPlayerManager.createGroupPlayer(e.getUniqueId().toString());
    }

}
