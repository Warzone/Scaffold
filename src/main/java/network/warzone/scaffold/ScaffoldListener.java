package network.warzone.scaffold;

import com.google.common.base.Splitter;
import com.sk89q.minecraft.util.commands.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.text.NumberFormat;
import java.util.*;

/**
 * Created by MatrixTunnel on 10/30/2018.
 */
public class ScaffoldListener implements Listener {

    private static final int NUMBER_WARNING = 950;

    private final Map<Date, String> todo = new HashMap<>();

//    @EventHandler
//    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
//        List<Date> outdated = new ArrayList<>();
//        Date oneMinAgo = new Date(System.currentTimeMillis() - 1000 * 60);
//        for (Date date : this.todo.keySet()) {
//            if (date.before(oneMinAgo))
//                outdated.add(date);
//        }
//
//        for (Date date : outdated)
//            this.todo.remove(date);
//
//        // Already entered, remove
//        if (this.todo.containsValue(event.getMessage())) {
//            Date remove = null;
//            for (Date date : this.todo.keySet())
//                if (this.todo.get(date).equals(event.getMessage()))
//                    remove = date;
//            this.todo.remove(remove);
//            return;
//        }
//
//        List<String> parts = Splitter.on(' ').splitToList(event.getMessage());
//        for (String part : parts) {
//            try {
//                Number number = NumberFormat.getInstance().parse(part);
//                if (number.doubleValue() > NUMBER_WARNING) {
//                    this.todo.put(new Date(), event.getMessage());
//                    event.getPlayer().sendMessage(ChatColor.RED + "Large number detected! Please try smaller operations or type the command again to ignore this warning.");
//                    event.getPlayer().sendMessage(ChatColor.RED + event.getMessage());
//                    event.setCancelled(true);
//                    break;
//                }
//            } catch (Exception e) {
//                // Ignore, not a number
//            }
//        }
//    }

    // Detect if a player is below 0 to stop players from falling into the void.
    @EventHandler
    public void onVoidDepth(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerlocation = event.getPlayer().getLocation();

        if (Scaffold.get().getConfig().getBoolean("voidbounce") == true && playerlocation.getY() < -64) {
            player.setVelocity(new Vector(0,2,0));
        }
    }

}
