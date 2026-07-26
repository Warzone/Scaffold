package network.warzone.scaffold;

import com.google.common.base.Preconditions;
import network.warzone.scaffold.utils.config.Config;
import network.warzone.scaffold.utils.config.ConfigFile;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public class ScaffoldWorld {

    private final String name;
    private final String worldName;
    private final File folder;
    private final File configFile;
    private int taskID = -1;

    public ScaffoldWorld(String name) {
        this.name = name;
        this.worldName = "scaffold/" + this.name;
        this.folder = new File(this.worldName);
        this.configFile = new File(this.folder, "scaffold.yml");
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public File getFolder() {
        return folder;
    }

    public File getConfigFile() {
        return configFile;
    }

    public Optional<World> getWorld() {
        return Optional.ofNullable(Bukkit.getWorld(this.worldName));
    }

    public Optional<Config> getConfig() {
        try {
            if (this.configFile.exists())
                return Optional.of(new ConfigFile(this.configFile));
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean isOpen() {
        return getWorld().isPresent();
    }

    public boolean isCreated() {
        return this.folder.exists() && new File(this.folder, "level.dat").exists();
    }

    public World create(WorldType type, Environment env, long seed) {
        Preconditions.checkArgument(!isOpen(), "World already loaded.");
        Preconditions.checkArgument(!isCreated(), "World already created.");

        Config config = new Config();
        config.set("type", type.name());
        config.set("environment", env.name());
        config.set("seed", seed);

        WorldCreator creator = worldCreator(Optional.of(config));
        World world = creator.createWorld();
        world.setSpawnLocation(0, 3, 0);
        world.setAutoSave(true);
        world.save();

        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.MOB_GRIEFING, false);


        Vector min = new Vector(-1, 0, -1);
        Vector max = new Vector(1, 0, 1);

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++)
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
                    world.getBlockAt(x, y, z).setType(Material.GLASS);

        config.save(this.configFile);

        return world;
    }

    public World load() {
        Preconditions.checkArgument(!isOpen(), "World already loaded.");
        Preconditions.checkArgument(isCreated(), "World is not created.");
        deleteUid();
        Optional<Config> config = getConfig();
        WorldCreator creator = worldCreator(config);
        World world = creator.createWorld();
        world.setAutoSave(true);
        startAutoSaveTask();
        return world;
    }

    private void deleteUid() {
        try {
            FileUtils.forceDelete(new File(this.folder, "uid.dat"));
        } catch (Exception e) {
            // meh...
            // ??? tf u mean "meh" luuke???
        }
    }

    public boolean unload() {
        Preconditions.checkArgument(isOpen(), "World is not loaded.");
        Preconditions.checkArgument(isCreated(), "World is not created.");

        World world = getWorld().get();
        world.save();
        cancelAutoSaveTask();
        return Bukkit.unloadWorld(world, true);
    }

    private WorldCreator worldCreator(Optional<Config> config) {
        WorldCreator creator = new WorldCreator(this.worldName);
        creator.generator(new NullChunkGenerator());
        if (config.isPresent()) {
            WorldType type = WorldType.valueOf(config.get().getAsString("type").toUpperCase());
            Environment environment =  Environment.valueOf(config.get().getAsString("environment").toUpperCase());
            long seed = config.get().getLong("seed");

            creator.type(type);
            creator.environment(environment);
            creator.seed(seed);
        }
        return creator;
    }

    public static Optional<ScaffoldWorld> ofWorld(World world) {
        if (!world.getName().startsWith("scaffold/"))
            return Optional.empty();

        return Optional.of(new ScaffoldWorld(world.getName().replace("scaffold/", "")));
    }

    public static ScaffoldWorld ofSearch(String query) {
        File[] files = new File("scaffold").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equalsIgnoreCase(query)) {
                    return new ScaffoldWorld(file.getName());
                }
            }
        }
        return new ScaffoldWorld(query);
    }

    @Override
    public String toString() {
        return "ScaffoldWorld{" +
                "name='" + name + '\'' +
                ", worldName='" + worldName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScaffoldWorld)) return false;
        ScaffoldWorld that = (ScaffoldWorld) o;
        return name.equals(that.name) && worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, worldName);
    }

    // Schedule an async repeating task every 5 minutes (5 * 60 * 20 ticks)
    private void startAutoSaveTask() {
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                Scaffold.get(),
                () -> Scaffold.get().async(() -> {
                    Optional<World> optionalWorld = getWorld();
                    optionalWorld.ifPresent(world -> Scaffold.get().sync(() -> {
                        world.save();
                        Bukkit.getLogger().info("Auto-saved world: " + world.getName());
                    }));
                }),
                0L,
                5 * 60 * 20L
        );
    }

    private void cancelAutoSaveTask() {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
            taskID = -1;
        }
    }
}
