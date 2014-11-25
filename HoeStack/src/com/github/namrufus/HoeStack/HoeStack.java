package com.github.namrufus.HoeStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;

public class HoeStack extends JavaPlugin implements Listener {
	private class Config {
		public final Material BLOCK_MATERIAL;
		public final int BLOCK_COST;
		
		public Config(ConfigurationSection config) {
			BLOCK_MATERIAL = Material.getMaterial(config.getString("block_material"));
			BLOCK_COST = config.getInt("block_cost");
		}
	}
	// ================================================================================================================
	
	// set of all hoe types in Minecraft
	private final Set<Material> HOES = new HashSet<Material>(Arrays.asList(new Material[]{Material.WOOD_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLD_HOE, Material.DIAMOND_HOE}));
	
	// ----------------------------------------------------------------------------------------------------------------
	
	private Config config;
	
	// ================================================================================================================
	
	public void onEnable() {
		// create the configuration file if it does not exist
		this.saveDefaultConfig();
		
		// load configurations
		config = new Config(this.getConfig());

	    // register events
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {	
		Block block = event.getClickedBlock();
		
		// Hoe using right click
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		Material blockMaterial = block.getType();
		
		// Match the block material (DIRT and GRASS convert to farmland)
		if (!(blockMaterial == Material.DIRT || blockMaterial == Material.GRASS))
			return;
			
		Material toolMaterial = event.getMaterial();
		
		// make sure the player is using a hoe
		if (!(toolMaterial != null/*nothing in hand*/ && HOES.contains(toolMaterial)))
			return;
		
		//make sure that there are the required number of blocks below the farmland block
		int blockCount = getMatchingBlockCount(block);
		if (blockCount >= config.BLOCK_COST) {
			//consume the blocks and let the farmland be created
			transmuteBlockColumn(block, config.BLOCK_COST);
		} else {
			event.getPlayer().sendMessage("§7[" + this.getDescription().getName() + "] " + blockCount + " of " + config.BLOCK_COST + " " + config.BLOCK_MATERIAL + " required below surface block.");
			event.setCancelled(true);
		}
	}
	
	// get the number of blocks of the correct type the underlie the given block
	private int getMatchingBlockCount(Block block) {
		int blockX = block.getX(), blockZ = block.getZ();
		World world = block.getWorld();
		
		int startY = block.getY() - 1;
		
		// check if the required number of blocks are underneath the crop block
		for (int i = 0; i<config.BLOCK_COST; i++) {
			int y = startY - i;
			// if the y coordinate is below the world or any of the blocks do not match the
			// correct type, then the multiplier is not applied
			if (y < 0 || world.getBlockAt(blockX, y, blockZ).getType() != config.BLOCK_MATERIAL)
				return i;
		}
		
		// if we looped all the way through, there are enough blocks
		return config.BLOCK_COST;
	}
	
	// transmutes a column of blocks count deep below the given block into dirt
	private void transmuteBlockColumn(Block startBlock, int count) {
		int blockX = startBlock.getX(), blockZ = startBlock.getZ();
		World world = startBlock.getWorld();
		
		int startY = startBlock.getY() - 1;
		
		for (int i = 0; i < count; i++) {
			int y = startY - i;
			if (y < 0) break;
			
			Block block = world.getBlockAt(blockX, y, blockZ);
			block.setType(Material.DIRT);
			block.setData((byte)0);
		}
	}
}
