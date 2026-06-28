package com.totemtp.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

public class TotemListener implements Listener {

    private final TotemTPPlugin plugin;

    public TotemListener(TotemTPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // L'event peut etre appele en etat "cancelled=true" si pas de totem -> on l'ignore.
        if (event.isCancelled()) {
            return;
        }

        TotemStation station = plugin.getDataManager().get(player.getUniqueId());
        if (station == null) {
            // Le joueur n'a pas de station configuree, on ne fait rien de special.
            return;
        }

        int count = station.incrementAndGet();
        plugin.getDataManager().save();

        player.sendMessage(ChatColor.GOLD + "[TotemTP] " + ChatColor.YELLOW
                + "Totem utilise ! Compteur: " + ChatColor.AQUA + count + ChatColor.YELLOW + "/3");

        if (count >= 3) {
            handleThirdTotem(station, player);
        }
    }

    private void handleThirdTotem(TotemStation station, Player triggerPlayer) {
        Location dest = station.getLocation();

        if (dest == null) {
            triggerPlayer.sendMessage(ChatColor.RED + "[TotemTP] Aucune destination n'est configuree pour cette station !");
            // On ne reset pas le compteur tant que la TP n'a pas pu avoir lieu, pour ne pas perdre l'info.
            station.setCounterRaw(2);
            plugin.getDataManager().save();
            return;
        }

        if (station.getTargetUUID() == null) {
            triggerPlayer.sendMessage(ChatColor.RED + "[TotemTP] Aucun joueur cible n'est configure pour cette station !");
            station.setCounterRaw(2);
            plugin.getDataManager().save();
            return;
        }

        Player target = plugin.getServer().getPlayer(station.getTargetUUID());
        if (target == null || !target.isOnline()) {
            triggerPlayer.sendMessage(ChatColor.RED + "[TotemTP] Le joueur cible (" + station.getTargetName() + ") n'est pas en ligne.");
            station.setCounterRaw(2);
            plugin.getDataManager().save();
            return;
        }

        target.teleport(dest);
        target.sendMessage(ChatColor.GOLD + "[TotemTP] " + ChatColor.GREEN + "Tu as ete teleporte !");

        if (!target.getUniqueId().equals(triggerPlayer.getUniqueId())) {
            triggerPlayer.sendMessage(ChatColor.GOLD + "[TotemTP] " + ChatColor.GREEN
                    + target.getName() + " a ete teleporte avec succes.");
        }

        station.resetCounter();
        plugin.getDataManager().save();
    }
}
