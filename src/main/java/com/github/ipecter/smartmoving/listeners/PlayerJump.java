package com.github.ipecter.smartmoving.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.github.ipecter.smartmoving.SMPlayer;
import com.github.ipecter.smartmoving.SmartMovingManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerJump implements Listener {

    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onJump(PlayerJumpEvent e) {
        SMPlayer smPlayer = smartMovingManager.getPlayer(e.getPlayer());
        if (smPlayer == null) return;
        if (smPlayer.isCrawling()) {
            smPlayer.stopCrawling();
        }
    }
}
