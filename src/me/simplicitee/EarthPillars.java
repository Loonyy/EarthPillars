package me.simplicitee;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.Config;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;

public class EarthPillars extends EarthAbility implements AddonAbility, ComboAbility{
	
	public static Config config;

	public static ConcurrentHashMap<Block, Long> times = new ConcurrentHashMap<>();
	
	private double radius;
	private double launch;

	public EarthPillars(Player player, boolean fall) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (fall) {
			radius = config.get().getDouble("Combos.Earth.EarthPillars.Fall.Radius");
			launch = config.get().getDouble("Combos.Earth.EarthPillars.Fall.LaunchPower");
		} else {
			radius = config.get().getDouble("Combos.Earth.EarthPillars.Radius");
			launch = config.get().getDouble("Combos.Earth.EarthPillars.LaunchPower");
		}
		
		start();
	}
	
	@Override
	public long getCooldown() {
		return config.get().getLong("Combos.Earth.EarthPillars.Cooldown");
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "EarthPillars";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove(false);
			return;
		}
		progressAbility();
	}
	
	private void progressAbility() {
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), radius)) {
			if (entity.getEntityId() != player.getEntityId() && entity instanceof LivingEntity && isEarthbendable(player, entity.getLocation().subtract(0, 1, 0).getBlock())) {
				LivingEntity l = (LivingEntity) entity;
				entity.setVelocity(new Vector(0, launch, 0));
				
				new TempBlock(entity.getLocation().getBlock(), entity.getLocation().subtract(0, 1, 0).getBlock().getType(), (byte)0);
				times.put(entity.getLocation().getBlock(), System.currentTimeMillis() + 200);
				if (entity.getLocation().distance(l.getEyeLocation()) > 1) {
					new TempBlock(entity.getLocation().add(0, 1, 0).getBlock(), entity.getLocation().subtract(0, 1, 0).getBlock().getType(), (byte)0);
					times.put(entity.getLocation().add(0, 1, 0).getBlock(), System.currentTimeMillis() + 200);
				}
			}
		}
		remove(true);
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new EarthPillars(player, false);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combo = new ArrayList<>();
		combo.add(new AbilityInformation("Shockwave", ClickType.SHIFT_DOWN));
		combo.add(new AbilityInformation("Catapult", ClickType.SHIFT_UP));
		return combo;
	}

	@Override
	public String getInstructions() {
		return "Shockwave (Hold Sneak) > Catapult (Release Sneak)";
	}
	
	public void setLaunchPower(double power) {
		launch = power;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public static void revert(boolean ignoretime) {
		for (Block block : times.keySet()) {
			long revert = times.get(block);
			if (System.currentTimeMillis() >= revert || ignoretime) {
				times.remove(block);
				TempBlock.revertBlock(block, Material.AIR);
			}
		}
	}
	
	public void remove(boolean cooldown) {
		super.remove();
		if (cooldown) {
			bPlayer.addCooldown(this);
		}
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
	}

	@Override
	public String getAuthor() {
		return "Simpliciee";
	}

	@Override
	public String getVersion() {
		return "1.2";
	}

	@Override
	public void load() {
		config = new Config(new File("Simplicitee.yml"));
		
		FileConfiguration c = config.get();
		
		c.addDefault("Combos.Earth.EarthPillars.Enabled", true);
		c.addDefault("Combos.Earth.EarthPillars.Description", "This combo creates small pillars of earth under entities that launches them up into the air. It can also be used when falling by being on your Catapult slot");
		c.addDefault("Combos.Earth.EarthPillars.Cooldown", 5000);
		c.addDefault("Combos.Earth.EarthPillars.Radius", 15);
		c.addDefault("Combos.Earth.EarthPillars.LaunchPower", 1.5);
		c.addDefault("Combos.Earth.EarthPillars.Fall.Distance", 10);
		c.addDefault("Combos.Earth.EarthPillars.Fall.Radius", 7);
		c.addDefault("Combos.Earth.EarthPillars.Fall.LaunchPower", 1.7);
		
		config.save();
		
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new EarthPillarsListener(), ProjectKorra.plugin);
		Permission perm = new Permission("bending.ability.earthpillars");
		if (!ProjectKorra.plugin.getServer().getPluginManager().getPermissions().contains(perm)) {
			ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
			perm.setDefault(PermissionDefault.TRUE);
		}
	}

	@Override
	public void stop() {}
	
	@Override
	public String getDescription() {
		return config.get().getString("Combos.Earth.EarthPillars.Description");
	}
}