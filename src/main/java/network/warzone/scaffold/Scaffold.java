package network.warzone.scaffold;

import com.google.common.base.Preconditions;
import network.warzone.scaffold.commands.ScaffoldCommands;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Scaffold extends JavaPlugin implements TabCompleter {

    private static Scaffold instance;
    public static Scaffold get() {
        return instance;
    }

    private Map<ScaffoldWorld, Long> locked = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        setupConfig();

        ScaffoldCommands commandExecutor = new ScaffoldCommands();
        getCommand("lock").setExecutor(commandExecutor);
        getCommand("archive").setExecutor(commandExecutor);
        getCommand("create").setExecutor(commandExecutor);
        getCommand("open").setExecutor(commandExecutor);
        getCommand("close").setExecutor(commandExecutor);
        getCommand("export").setExecutor(commandExecutor);
        getCommand("import").setExecutor(commandExecutor);
        getCommand("worlds").setExecutor(commandExecutor);

        // Schedule repeating task to load locked world data
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (ScaffoldWorld wrapper : locked.keySet()) {
                if (wrapper.isOpen()) {
                    wrapper.getWorld().get().setFullTime(locked.get(wrapper));
                }
            }
        }, 1L, 20L);
    }

    public void sync(Runnable runnable) {
        getServer().getScheduler().runTask(this, runnable);
    }

    public void async(Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }

    public boolean toggleLock(ScaffoldWorld wrapper) {
        Preconditions.checkArgument(wrapper.isOpen(), "World not open.");
        Iterator<ScaffoldWorld> iterator = this.locked.keySet().iterator();
        while (iterator.hasNext()) {
            ScaffoldWorld next = iterator.next();
            if (next.getName().equals(wrapper.getName())) {
                iterator.remove();
                return false;
            }
        }
        locked.put(wrapper, wrapper.getWorld().get().getFullTime());
        return true;
    }

    public void setupConfig() {
        try {
            String path = "plugins/Scaffold/config.properties";
            File configFile = new File(path);

            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();

                // Write default properties to the file
                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write("fileio_username=defaultUsername\n");
                    writer.write("fileio_password=defaultPassword\n");
                } catch (IOException e) {
                    System.out.println("An error occurred while writing to the config file.");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Could not create the config.properties file.");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
