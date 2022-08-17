package com.github.ipecter.smartmoving.utils;

import com.github.ipecter.smartmoving.SmartMoving;
import com.github.ipecter.smartmoving.SmartMovingManager;
import com.github.ipecter.smartmoving.impl.WorldGuardImplementation;
import com.github.ipecter.smartmoving.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Utils {
    private static final Plugin plugin = SmartMoving.getPlugin(SmartMoving.class);
    private static final WorldGuardImplementation worldGuard = SmartMovingManager.getInstance().getWorldGuard();
    private static final ConfigManager configManager = ConfigManager.getInstance();
    public static BlockData BARRIER_BLOCK_DATA = Bukkit.createBlockData(Material.BARRIER);

    public static void revertBlockPacket(Player player, final Block block) {
        player.sendBlockChange(block.getLocation(), block.getBlockData());
        Bukkit.getScheduler().runTask(plugin, () -> block.getState().update());
    }

    public static boolean canCrawl(Player player) {
        if (!player.hasPermission("smartmoving.crawling.use")) return false;
        if (worldGuard != null)
            if (!worldGuard.canCrawl(player))
                return false;
        Block playerBelowBlock = player.getLocation().clone().subtract(0, 0.4, 0).getBlock();
        boolean isOnBlacklistedBlock = configManager.getCrawlingBlockBlackList().contains(playerBelowBlock.getType().name().toUpperCase());
        if (isOnBlacklistedBlock) return false;
        boolean isInBlacklistedWorld = configManager.getCrawlingWorldBlackList().contains(player.getWorld());
        if (isInBlacklistedWorld) return false;
        return isOnGround(player) && isFullBlock(player);
    }

    public static boolean isOnGround(Player player) {
        return !player.isFlying() && player.isOnGround() && !player.isInsideVehicle();
    }

    public static boolean isFullBlock(Player player) {
        return checkAboveLoc(player) && checkLegLoc(player);
    }

    public static boolean checkAboveLoc(Player player) {
        Block block = player.getLocation().add(0, 1.5, 0).getBlock();
        return checkAboveLoc(block);
    }

    public static boolean checkLegLoc(Player player) {
        Block block = player.getLocation().getBlock();
        return checkLegLoc(block);
    }

    public static boolean checkAboveLoc(Block block) {
        if (block.getType().isAir()) return true;
        if (block.isSolid()) return false;
        if (block.isPassable()) return false;
        return true;
    }

    public static boolean checkLegLoc(Block block) {
        if (block.isCollidable()) return false;
        return true;
    }

    public static boolean isInFrontOfATunnel(Player player) {
        WallFace facing = WallFace.fromBlockFace(player.getFacing());

        Location location = player.getLocation().add(facing.xOffset, 1, facing.zOffset);
        Block block = location.getBlock();
        double distanceLimit = facing.distance;

        if (block.getType().isSolid() && location.subtract(0, 1, 0).getBlock().isPassable()) {
            if (facing.equals(WallFace.EAST) || facing.equals(WallFace.WEST)) {
                return Math.abs(location.getX() - block.getX()) < distanceLimit;
            } else {
                return Math.abs(location.getZ() - block.getZ()) < distanceLimit;
            }
        }

        return false;
    }

    public enum WallFace {

        NORTH(0, -1, 0.31),
        SOUTH(0, 1, 0.70),
        WEST(-1, 0, 0.31),
        EAST(1, 0, 0.70);


        public final int xOffset;
        public final int zOffset;

        public final double distance;

        WallFace(int xOffset, int zOffset, double distance) {
            this.xOffset = xOffset;
            this.zOffset = zOffset;
            this.distance = distance;
        }

        public static WallFace fromBlockFace(BlockFace blockFace) {
            switch (blockFace) {
                case NORTH:
                    return NORTH;
                case SOUTH:
                    return SOUTH;
                case WEST:
                    return WEST;
                default: //EAST
                    return EAST;
            }
        }
    }
}