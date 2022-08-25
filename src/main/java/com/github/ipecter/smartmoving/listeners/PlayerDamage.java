package com.github.ipecter.smartmoving.listeners;

import com.github.ipecter.smartmoving.SmartMoving;
import com.github.ipecter.smartmoving.SmartMovingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDamage implements Listener {

    private final SmartMovingManager smartMovingManager = SmartMovingManager.getInstance();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (smartMovingManager.isCrawling(e.getEntity())) {
            SmartMoving.debug("Stop Crawling - Death");
            smartMovingManager.stopCrawling(e.getEntity());
        }

    }

}