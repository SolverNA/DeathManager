import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.Location;
public class TestEvent {
    public void test(Player p, Location l) {
        new PlayerRespawnEvent(p, l, false, false, PlayerRespawnEvent.RespawnReason.PLUGIN);
    }
}
