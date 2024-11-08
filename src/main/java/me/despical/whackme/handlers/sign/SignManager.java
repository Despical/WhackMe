package me.despical.whackme.handlers.sign;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.whackme.api.Reloadable;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.event.AbstractEventHandler;
import me.despical.whackme.user.User;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 31.01.2024
 */
public class SignManager extends AbstractEventHandler implements Reloadable {

	private final Set<ArenaSign> arenaSigns;
	private final List<String> signLines;

	public SignManager() {
		this.arenaSigns = new HashSet<>();
		this.signLines = plugin.getChatManager().getStringList("Signs.Lines");

		this.loadSigns();
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		final Player player = event.getPlayer();

		if (!player.hasPermission("wm.admin.sign.create") || !event.getLine(0).equalsIgnoreCase("[wm]")) {
			return;
		}

		final User user = plugin.getUserManager().getUser(event.getPlayer());
		final String line = event.getLine(1);

		if (line.isEmpty()) {
			player.sendMessage(chatManager.prefixedMessage("Commands.Type-Arena-Name"));
			return;
		}

		final Arena arena = plugin.getArenaRegistry().getArena(line);

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("Commands.No-Arena-Like-That"));
			return;
		}

		final Block block = event.getBlock();

		arenaSigns.add(new ArenaSign((Sign) block.getState(), arena));

		for (int i = 0; i < signLines.size(); i++) {
			event.setLine(i, formatSign(signLines.get(i), arena));
		}

		user.sendRawMessage("&aArena sign has been created successfully!");

		final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
		final String path = String.format("instances.%s.signs", arena);
		final List<String> locs = config.getStringList(path);
		locs.add(LocationSerializer.toString(event.getBlock().getLocation()));

		config.set(path, locs);
		ConfigUtils.saveConfig(plugin, config, "arenas");
	}

	@EventHandler
	public void onSignDestroy(BlockBreakEvent event) {
		final Block block = event.getBlock();
		final ArenaSign arenaSign = getArenaSignByBlock(block);

		if (arenaSign == null) return;

		final Player player = event.getPlayer();
		final User user = plugin.getUserManager().getUser(player);

		if (!player.hasPermission("wm.admin.sign.break")) {
			event.setCancelled(true);

			user.sendRawMessage("&cYou don't have enough permission to break this sign!");
			return;
		}

		arenaSigns.remove(arenaSign);

		final String location = LocationSerializer.toString(block.getLocation());
		final String path = String.format("instances.%s.signs", arenaSign.getArena());
		final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
		final List<String> signs = config.getStringList(path);

		for (final String loc : signs) {
			if (loc.equals(location)) {
				signs.remove(location);

				config.set(path, signs);
				ConfigUtils.saveConfig(plugin, config, "arenas");

				user.sendRawMessage("&aSign removed successfully!");
				return;
			}
		}

		user.sendRawMessage("&cCouldn't remove arena sign! Please do manually!");
	}

	@EventHandler
	public void onJoinAttempt(PlayerInteractEvent event) {
		final ArenaSign arenaSign = getArenaSignByBlock(event.getClickedBlock());

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && arenaSign != null) {
			final Arena arena = arenaSign.getArena();

			if (arena == null) return;

			final Player player = event.getPlayer();

			if (plugin.getArenaRegistry().isInArena(player)) {
				player.sendMessage(chatManager.prefixedMessage("In-Game.Already-Playing"));
				return;
			}

			plugin.getArenaManager().joinAttempt(player, arena);
		}
	}

	public void loadSigns() {
		arenaSigns.clear();

		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
		boolean removed = false;

		for (final String arenaId : config.getConfigurationSection("instances").getKeys(false)) {
			List<String> signs = config.getStringList("instances." + arenaId + ".signs");
			int size = signs.size();

			for (final String location : signs) {
				final BlockState blockState = LocationSerializer.fromString(location).getBlock().getState();

				if (blockState instanceof Sign) {
					arenaSigns.add(new ArenaSign((Sign) blockState, plugin.getArenaRegistry().getArena(arenaId)));
				} else {
					signs.remove(location);
				}
			}

			if (removed |= size != signs.size()) {
				config.set("instances." + arenaId + ".signs", signs);
			}
		}

		if (removed) {
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}

		updateSigns();
	}

	public void updateSign(final Arena arena) {
		this.arenaSigns.stream().filter(arenaSign -> arenaSign.getArena().equals(arena)).forEach(this::updateSign);
	}

	private void updateSign(final ArenaSign arenaSign) {
		final Sign sign = arenaSign.getSign();

		for (int i = 0; i < signLines.size(); i++) {
			sign.setLine(i, formatSign(signLines.get(i), arenaSign.getArena()));
		}

		sign.update();
	}

	public void updateSigns() {
		for (final ArenaSign arenaSign : arenaSigns) {
			final Sign sign = arenaSign.getSign();

			for (int i = 0; i < signLines.size(); i++) {
				sign.setLine(i, formatSign(signLines.get(i), arenaSign.getArena()));
			}

			sign.update();
		}
	}

	public boolean isGameSign(Block block) {
		return this.arenaSigns.stream().anyMatch(sign -> sign.getSign().getLocation().equals(block.getLocation()));
	}

	public void addArenaSign(Block block, Arena arena) {
		arenaSigns.add(new ArenaSign((Sign) block.getState(), arena));
	}

	public void removeSigns(Arena arena) {
		this.arenaSigns.removeIf(sign -> sign.getArena().equals(arena));
	}

	private String formatSign(String msg, Arena arena) {
		String formatted = msg;

		formatted = formatted.replace("%arena%", arena.getId());
		formatted = formatted.replace("%player%", arena.getPlayerName());

		if (arena.isReady()) {
			if (arena.getPlayer() == null) {
				formatted = formatted.replace("%state%", plugin.getChatManager().message("Signs.States.Waiting"));
			} else {
				formatted = formatted.replace("%state%", plugin.getChatManager().message("Signs.States.Playing"));
			}
		} else {
			formatted = formatted.replace("%state%", plugin.getChatManager().message("Signs.States.Inactive"));
		}

		return plugin.getChatManager().coloredRawMessage(formatted);
	}

	private ArenaSign getArenaSignByBlock(Block block) {
		return block == null || !(block.getState() instanceof Sign) ? null : arenaSigns.stream().filter(sign -> sign.getSign().getLocation().equals(block.getLocation())).findFirst().orElse(null);
	}

	@Override
	public void reload() {
		this.loadSigns();
	}
}