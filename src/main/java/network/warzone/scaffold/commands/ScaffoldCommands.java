package network.warzone.scaffold.commands;

import com.google.common.base.Joiner;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import network.warzone.scaffold.Scaffold;
import network.warzone.scaffold.ScaffoldWorld;
import network.warzone.scaffold.Zip;
import network.warzone.scaffold.utils.config.FtpManager;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ScaffoldCommands implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        return switch (cmd.getName().toLowerCase()) {
            case "lock" -> lock(sender, args);
            case "archive" -> archive(sender, args);
            case "create" -> create(sender, args);
            case "open" -> open(sender, args);
            case "world" -> open(sender, args);
            case "close" -> close(sender, args);
            case "export" -> export(sender, args);
            case "import" -> download(sender, args);
            case "worlds" -> worlds(sender, args);

            default -> false;
        };
    }

    private boolean lock(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scaffold.command.lock")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /lock <world>");
            return true;
        }

        String worldName = args[0];
        ScaffoldWorld wrapper = ScaffoldWorld.ofSearch(worldName);

        if (!wrapper.isCreated()) {
            sender.sendMessage(ChatColor.RED + "World not found.");
            return true;
        }

        if (!wrapper.isOpen()) {
            sender.sendMessage(ChatColor.RED + "World not open.");
            return true;
        }

        boolean locked = Scaffold.get().toggleLock(wrapper);
        sender.sendMessage(ChatColor.GOLD + (locked ? "Locked " + wrapper.getName() + " to current time." : "Unlocked " + wrapper.getName() + "."));
        return true;
    }

    private boolean archive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scaffold.command.archive")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        Set<String> flags = getFlags(args);
        args = removeFlags(args);

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /archive <world>");
            return true;
        }

        ScaffoldWorld wrapper = ScaffoldWorld.ofSearch(args[0]);
        boolean delete = flags.contains("-k");

        if (!wrapper.isCreated()) {
            sender.sendMessage(ChatColor.RED + "World has not been created.");
            return true;
        }

        if (wrapper.isOpen() && delete) {
            sender.sendMessage(ChatColor.RED + "World must be closed to archive and delete.");
            return true;
        }

        File folder = wrapper.getFolder();
        File archives = new File("scaffold-archives");
        String unique = UUID.randomUUID().toString().substring(0, 6);
        File archive = new File(archives, folder.getName() + "-" + unique);

        if (!archives.exists()) archives.mkdir();

        try {
            FileUtils.copyDirectory(folder, archive);
            if (delete) FileUtils.deleteDirectory(folder);
            sender.sendMessage(ChatColor.GOLD + (delete ? "Deleted and archived \"" + wrapper.getName() + "\"." : "Archived \"" + wrapper.getName() + "\"."));
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "An error has occurred. See the server logs.");
        }
        return true;
    }

    private boolean create(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scaffold.command.create")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /create <world>");
            return true;
        }


        ScaffoldWorld wrapper = ScaffoldWorld.ofSearch(args[0]);
        if (wrapper.isCreated()) {
            sender.sendMessage(ChatColor.RED + "World already created.");
            return true;
        }
        if (wrapper.isOpen()) {
            sender.sendMessage(ChatColor.RED + "World already open.");
            return true;
        }

        WorldType type = WorldType.FLAT;
        Environment env = Environment.NORMAL;
        long seed = ThreadLocalRandom.current().nextInt(500000000);

        wrapper.create(type, env, seed);
        sender.sendMessage(ChatColor.GOLD + "Created world \"" + wrapper.getName() + "\".");
        return true;
    }

    private boolean open(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scaffold.command.open")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /open <world>");
            return true;
        }

        ScaffoldWorld wrapper = ScaffoldWorld.ofSearch(args[0]);
        if (!wrapper.isCreated()) {
            sender.sendMessage(ChatColor.RED + "World has not been created.");
            return true;
        }

        if (!wrapper.isOpen()) {
            wrapper.load();
            sender.sendMessage(ChatColor.GOLD + "Opened world \"" + wrapper.getName() + "\".");
        }

        if (sender instanceof Player player) {
            wrapper.getWorld().ifPresentOrElse(
                    world -> {
                        player.teleport(world.getSpawnLocation());
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                        player.setGameMode(GameMode.CREATIVE);
                        player.setAllowFlight(true);
                        player.setFlying(true);
                    },
                    () -> {
                        new RuntimeException("An unexpected error has occurred.").printStackTrace();
                    }
            );
        }
        return true;
    }

    private boolean close(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scaffold.command.close")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /close <world>");
            return true;
        }

        ScaffoldWorld wrapper = ScaffoldWorld.ofSearch(args[0]);
        if (!wrapper.isCreated()) {
            sender.sendMessage(ChatColor.RED + "World has not been created.");
            return true;
        }

        if (!wrapper.isOpen()) {
            sender.sendMessage(ChatColor.RED + "World is not open.");
            return true;
        }

        World main = Bukkit.getWorlds().getFirst();
        for (Entity entity : wrapper.getWorld().get().getEntities()) {
            if (entity instanceof Player player) {
                player.sendMessage(ChatColor.RED + sender.getName() + " is unloading this world... Teleporting elsewhere!");
                player.teleport(main.getSpawnLocation());
            }
        }

        boolean unloaded = wrapper.unload();
        sender.sendMessage(ChatColor.GOLD + (unloaded ? "Closed world \"" + wrapper.getName() + "\"." : "Failed to unload world \"" + wrapper.getName() + "\"."));
        return true;
    }

    private boolean export(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scaffold.command.export")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        Set<String> flags = getFlags(args);
        args = removeFlags(args);

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /export <world>");
            return true;
        }

        String worldName = args[0];
        ScaffoldWorld wrapper = ScaffoldWorld.ofSearch(worldName);
        //TODO: Implement chunk pruning (this flag is currently unusued)
        boolean prune = flags.contains("-p");

        if (!wrapper.isCreated()) {
            sender.sendMessage(ChatColor.RED + "World not found.");
            return true;
        }

        FtpManager ftpManager = new FtpManager();
        String username = ftpManager.getProperty("fileio_username");
        String password = ftpManager.getProperty("fileio_password");

        if (username == null || password == null) {
            sender.sendMessage(ChatColor.RED + "API credentials are not set correctly.");
            return true;
        }

        Scaffold.get().async(() -> {
            sender.sendMessage(ChatColor.YELLOW + "Compressing world...");
            String randy = UUID.randomUUID().toString().substring(0, 3);
            File zip = new File(wrapper.getName() + "-" + randy + ".zip");

            try {
                Zip.create(wrapper.getFolder(), zip, prune);

                sender.sendMessage(ChatColor.YELLOW + "Uploading world, this may take a while depending on the map size...");
                HttpResponse<String> response = Unirest.post("https://file.io")
                        .basicAuth(username, password)
                        .field("file", zip)
                        .asString();

                if (response.getStatus() == 200) {
                    JSONObject responseJson = new JSONObject(response.getBody());
                    String link = responseJson.getString("link");
                    sender.sendMessage(ChatColor.GREEN + "Upload complete: " + link);
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to upload world: " + response.getStatusText());
                }
                try {
                    if (!zip.delete()) {
                        System.err.println("Failed to delete the zip file: " + zip.getAbsolutePath());
                    }
                } catch (SecurityException e) {
                    System.err.println("SecurityException: Unable to delete the zip file (insufficient permissions?)");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "Failed to compress or upload the specified world.");
            }
        });
        return true;
    }

    private boolean download(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scaffold.command.import")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /import <.zip file link> <world name>");
            return true;
        }

        String link = args[0];
        ScaffoldWorld wrapper = ScaffoldWorld.ofSearch(args[1]);

        if (wrapper.isCreated()) {
            sender.sendMessage(ChatColor.RED + "World already created.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Importing world...");
        try {
            HttpResponse<InputStream> response = Unirest.get(link)
                    .header("content-type", "*/*")
                    .asBinary();

            File tempZip = new File(UUID.randomUUID() + ".zip");
            Files.copy(response.getBody(), tempZip.toPath());
            Zip.extract(tempZip, wrapper.getFolder());
            FileUtils.forceDelete(tempZip);

            if (!wrapper.isCreated()) {
                sender.sendMessage(ChatColor.RED + "Invalid zipped world, no level.dat in root?");
                FileUtils.deleteDirectory(wrapper.getFolder());
                return true;
            }

            wrapper.load();
            sender.sendMessage(ChatColor.GOLD + "World imported and opened!");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Failed to import, see server logs.");
        }
        return true;
    }

    private boolean worlds(CommandSender sender, String[] args) {
        if (!sender.hasPermission("scaffold.command.worlds")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        Set<String> flags = getFlags(args);
        args = removeFlags(args);

        List<ScaffoldWorld> allWorlds = new ArrayList<>();
        File scaffoldFolder = new File("scaffold");

        if (scaffoldFolder.exists()) {
            File[] contents = scaffoldFolder.listFiles();
            if (contents != null) {
                for (File folder : contents) {
                    ScaffoldWorld world = new ScaffoldWorld(folder.getName());
                    if (world.isCreated()) {
                        allWorlds.add(world);
                    }
                }
            }
        }

        allWorlds.sort(Comparator.comparing(ScaffoldWorld::getName));
        String prefix = "Worlds";

        if (flags.contains("-o")) {
            allWorlds.removeIf(world -> !world.isOpen());
            prefix = "Opened worlds";
        }
        else if (flags.contains("-c")) {
            allWorlds.removeIf(ScaffoldWorld::isOpen);
            prefix = "Closed worlds";
        }

        if (args.length > 0) {
            //TODO: I am like 95% sure we don't want world names to have spaces in them, so this is redundant.
            // Instead, perhaps change it to search for like worlds with "string1" or "string2" or "string3" etc.
            String query = String.join(" ", args).toLowerCase();

            allWorlds.removeIf(world -> !world.getName().toLowerCase().contains(query));
            prefix += " (matching \"" + query + "\")";
        }

        List<String> names = new ArrayList<>();
        for (ScaffoldWorld wrapper : allWorlds) {
            names.add((wrapper.isOpen() ? ChatColor.GREEN : ChatColor.RED) + wrapper.getName());
        }

        String list = Joiner.on(ChatColor.WHITE + ", ").join(names);
        sender.sendMessage(ChatColor.GRAY + prefix + ": " + list);
        return true;
    }

    private Set<String> getFlags(String[] args) {
        Set<String> flags = new HashSet<>();

        for (String arg : args) {
            if (arg.startsWith("-")) {
                // Handle combined flags (ex: "-ktf" becomes "-k", "-t", "-f")
                // Also limit max combined flag to 10 (software security!)
                for (int i = 1; i < arg.length() || i == 10; i++) {
                    flags.add("-" + arg.charAt(i));
                }
            }
        }
        return flags;
    }

    private String[] removeFlags(String[] args) {
        List<String> filteredArgs = new ArrayList<>();

        for (String arg : args) {
            if (!arg.startsWith("-")) {
                filteredArgs.add(arg);
            }
        }
        return filteredArgs.toArray(new String[0]);
    }
}
