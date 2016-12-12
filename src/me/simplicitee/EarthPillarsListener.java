package me.simplicitee;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.projectkorra.projectkorra.BendingPlayer;

public class EarthPillarsListener implements Listener{

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onFallDamage(EntityDamageEvent event) {
		if (event.getCause() != DamageCause.FALL) return;
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.getBoundAbilityName().equalsIgnoreCase("Catapult")) {
			if (player.getFallDistance() >= EarthPillars.config.get().getDouble("Combos.Earth.EarthPillars.Fall.Distance")) {
				new EarthPillars(player, true);
			}
		}
	}
}
