package com.totemtp.plugin;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/**
 * Represente la configuration de teleportation pour UN joueur cible.
 * targetUUID : le joueur qui sera teleporte au bout de 3 totems.
 * triggerUUID : le joueur dont on compte les totems (peut etre le meme que target, ou un autre).
 * Le compteur repart a 0 apres chaque teleportation.
 */
public class TotemStation {

    private final UUID triggerUUID;
    private String triggerName;

    private UUID targetUUID;
    private String targetName;

    private int counter;

    private String worldName;
    private double x, y, z;
    private float yaw, pitch;

    public TotemStation(UUID triggerUUID, String triggerName) {
        this.triggerUUID = triggerUUID;
        this.triggerName = triggerName;
        this.counter = 0;
    }

    public UUID getTriggerUUID() {
        return triggerUUID;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTarget(UUID targetUUID, String targetName) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
    }

    public int getCounter() {
        return counter;
    }

    public int incrementAndGet() {
        return ++counter;
    }

    public void resetCounter() {
        this.counter = 0;
    }

    public boolean hasLocation() {
        return worldName != null;
    }

    public void setLocation(Location loc) {
        this.worldName = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    public Location getLocation() {
        if (worldName == null) return null;
        World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public String getWorldName() {
        return worldName;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    public void setRaw(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void setCounterRaw(int counter) {
        this.counter = counter;
    }
}
