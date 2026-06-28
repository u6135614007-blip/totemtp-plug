package com.totemtp.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TotemTPCommand implements CommandExecutor, TabCompleter {

    private final TotemTPPlugin plugin;

    public TotemTPCommand(TotemTPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "setdest" -> handleSetDest(sender, args);
            case "settarget" -> handleSetTarget(sender, args);
            case "info" -> handleInfo(sender, args);
            case "list" -> handleList(sender);
            case "reset" -> handleReset(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }

        return true;
    }

    /**
     * /totemtp setdest <joueurDeclencheur>
     * Definit la position actuelle du sender comme destination de TP pour la station de <joueurDeclencheur>.
     * Doit etre execute par un joueur (on prend sa Location).
     */
    private void handleSetDest(CommandSender sender, String[] args) {
        if (!sender.hasPermission("totemtp.admin")) {
            sender.sendMessage(ChatColor.RED + "Tu n'as pas la permission.");
            return;
        }
        if (!(sender instanceof Player adminPlayer)) {
            sender.sendMessage(ChatColor.RED + "Cette commande doit etre executee par un joueur (pour recuperer la position).");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /totemtp setdest <joueurDeclencheur>");
            return;
        }

        OfflinePlayer triggerOff = Bukkit.getOfflinePlayer(args[1]);
        if (!hasPlayedOrOnline(triggerOff)) {
            sender.sendMessage(ChatColor.RED + "Joueur inconnu: " + args[1]);
            return;
        }

        TotemStation station = plugin.getDataManager().getOrCreate(triggerOff.getUniqueId(), triggerOff.getName());
        Location loc = adminPlayer.getLocation();
        station.setLocation(loc);
        plugin.getDataManager().save();

        sender.sendMessage(ChatColor.GREEN + "[TotemTP] Destination definie pour la station de " + ChatColor.AQUA
                + triggerOff.getName() + ChatColor.GREEN + " : "
                + formatLoc(loc));

        if (station.getTargetUUID() == null) {
            sender.sendMessage(ChatColor.YELLOW + "Pense a definir le joueur a teleporter avec /totemtp settarget "
                    + triggerOff.getName() + " <joueurCible>");
        }
    }

    /**
     * /totemtp settarget <joueurDeclencheur> <joueurCible>
     * Choisit quel joueur sera teleporte quand <joueurDeclencheur> pop son 3e totem.
     */
    private void handleSetTarget(CommandSender sender, String[] args) {
        if (!sender.hasPermission("totemtp.admin")) {
            sender.sendMessage(ChatColor.RED + "Tu n'as pas la permission.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /totemtp settarget <joueurDeclencheur> <joueurCible>");
            return;
        }

        OfflinePlayer triggerOff = Bukkit.getOfflinePlayer(args[1]);
        if (!hasPlayedOrOnline(triggerOff)) {
            sender.sendMessage(ChatColor.RED + "Joueur declencheur inconnu: " + args[1]);
            return;
        }

        OfflinePlayer targetOff = Bukkit.getOfflinePlayer(args[2]);
        if (!hasPlayedOrOnline(targetOff)) {
            sender.sendMessage(ChatColor.RED + "Joueur cible inconnu: " + args[2]);
            return;
        }

        TotemStation station = plugin.getDataManager().getOrCreate(triggerOff.getUniqueId(), triggerOff.getName());
        station.setTarget(targetOff.getUniqueId(), targetOff.getName());
        plugin.getDataManager().save();

        sender.sendMessage(ChatColor.GREEN + "[TotemTP] Quand " + ChatColor.AQUA + triggerOff.getName()
                + ChatColor.GREEN + " pop son 3e totem, " + ChatColor.AQUA + targetOff.getName()
                + ChatColor.GREEN + " sera teleporte.");

        if (!station.hasLocation()) {
            sender.sendMessage(ChatColor.YELLOW + "Pense a definir la destination avec /totemtp setdest "
                    + triggerOff.getName());
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /totemtp info <joueurDeclencheur>");
            return;
        }
        OfflinePlayer triggerOff = Bukkit.getOfflinePlayer(args[1]);
        TotemStation station = plugin.getDataManager().get(triggerOff.getUniqueId());
        if (station == null) {
            sender.sendMessage(ChatColor.RED + "Aucune station configuree pour " + args[1] + ".");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Station de " + station.getTriggerName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Compteur: " + ChatColor.AQUA + station.getCounter() + "/3");
        sender.sendMessage(ChatColor.YELLOW + "Cible TP: " + ChatColor.AQUA
                + (station.getTargetName() != null ? station.getTargetName() : "non definie"));
        if (station.hasLocation()) {
            sender.sendMessage(ChatColor.YELLOW + "Destination: " + ChatColor.AQUA + formatLoc(station.getLocation()));
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Destination: " + ChatColor.RED + "non definie");
        }
    }

    private void handleList(CommandSender sender) {
        Map<UUID, TotemStation> all = plugin.getDataManager().getAllConfigs();
        if (all.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Aucune station configuree.");
            return;
        }
        sender.sendMessage(ChatColor.GOLD + "=== Stations TotemTP (" + all.size() + ") ===");
        for (TotemStation s : all.values()) {
            String dest = s.hasLocation() ? "OK" : "manquante";
            String target = s.getTargetName() != null ? s.getTargetName() : "manquant";
            sender.sendMessage(ChatColor.AQUA + s.getTriggerName() + ChatColor.GRAY + " -> cible: "
                    + ChatColor.WHITE + target + ChatColor.GRAY + " | dest: " + ChatColor.WHITE + dest
                    + ChatColor.GRAY + " | compteur: " + ChatColor.WHITE + s.getCounter() + "/3");
        }
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("totemtp.admin")) {
            sender.sendMessage(ChatColor.RED + "Tu n'as pas la permission.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /totemtp reset <joueurDeclencheur>");
            return;
        }
        OfflinePlayer triggerOff = Bukkit.getOfflinePlayer(args[1]);
        TotemStation station = plugin.getDataManager().get(triggerOff.getUniqueId());
        if (station == null) {
            sender.sendMessage(ChatColor.RED + "Aucune station configuree pour " + args[1] + ".");
            return;
        }
        station.resetCounter();
        plugin.getDataManager().save();
        sender.sendMessage(ChatColor.GREEN + "Compteur de " + args[1] + " remis a 0.");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("totemtp.admin")) {
            sender.sendMessage(ChatColor.RED + "Tu n'as pas la permission.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /totemtp remove <joueurDeclencheur>");
            return;
        }
        OfflinePlayer triggerOff = Bukkit.getOfflinePlayer(args[1]);
        if (plugin.getDataManager().get(triggerOff.getUniqueId()) == null) {
            sender.sendMessage(ChatColor.RED + "Aucune station configuree pour " + args[1] + ".");
            return;
        }
        plugin.getDataManager().remove(triggerOff.getUniqueId());
        plugin.getDataManager().save();
        sender.sendMessage(ChatColor.GREEN + "Station de " + args[1] + " supprimee.");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("totemtp.admin")) {
            sender.sendMessage(ChatColor.RED + "Tu n'as pas la permission.");
            return;
        }
        plugin.getDataManager().load();
        sender.sendMessage(ChatColor.GREEN + "[TotemTP] Donnees rechargees depuis data.yml.");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== TotemTP - Aide ===");
        sender.sendMessage(ChatColor.AQUA + "/totemtp setdest <joueurDeclencheur>" + ChatColor.GRAY
                + " - definit ta position actuelle comme destination");
        sender.sendMessage(ChatColor.AQUA + "/totemtp settarget <joueurDeclencheur> <joueurCible>" + ChatColor.GRAY
                + " - definit qui sera teleporte");
        sender.sendMessage(ChatColor.AQUA + "/totemtp info <joueurDeclencheur>" + ChatColor.GRAY + " - affiche la config");
        sender.sendMessage(ChatColor.AQUA + "/totemtp list" + ChatColor.GRAY + " - liste toutes les stations");
        sender.sendMessage(ChatColor.AQUA + "/totemtp reset <joueurDeclencheur>" + ChatColor.GRAY + " - remet le compteur a 0");
        sender.sendMessage(ChatColor.AQUA + "/totemtp remove <joueurDeclencheur>" + ChatColor.GRAY + " - supprime la station");
        sender.sendMessage(ChatColor.AQUA + "/totemtp reload" + ChatColor.GRAY + " - recharge data.yml");
    }

    private boolean hasPlayedOrOnline(OfflinePlayer p) {
        return p != null && (p.hasPlayedBefore() || p.isOnline());
    }

    private String formatLoc(Location loc) {
        if (loc == null) return "inconnue";
        return String.format("%s [x=%.1f, y=%.1f, z=%.1f]", loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = List.of("setdest", "settarget", "info", "list", "reset", "remove", "reload");
            return filter(subs, args[0]);
        }

        if (args.length == 2 && List.of("setdest", "settarget", "info", "reset", "remove").contains(args[0].toLowerCase())) {
            List<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return filter(names, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("settarget")) {
            List<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            return filter(names, args[2]);
        }

        return result;
    }

    private List<String> filter(List<String> options, String typed) {
        String lower = typed.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}
