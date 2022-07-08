package me.despical.whackme.command.admin;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.SubCommand;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class CreateCommand extends SubCommand {

	public CreateCommand() {
		super ("create");

		setPermission("wm.admin.create");
	}

	@Override
	public String getPossibleArguments() {
		return "<id>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		Player player = (Player) sender;

		if (args.length == 0) {
			player.sendMessage(chatManager.prefixedRawMessage("&cPlease enter an name to create an arena!"));
			return;
		}

		String arg = args[0];

		if (ArenaRegistry.isArena(arg)) {
			player.sendMessage(chatManager.prefixedRawMessage("&cArena with that ID already contains!"));
			player.sendMessage(chatManager.prefixedRawMessage("&cTo check existing arenas use: /wm list"));
			return;
		}

		setupDefaultConfiguration(arg);

		player.sendMessage(chatManager.coloredRawMessage("&l--------------------------------------------"));
		MiscUtils.sendCenteredMessage(player, "&eInstance &a&l" + arg + " &ecreated!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&aEdit this arena via /wm edit &6" + arg + "&a!");
		player.sendMessage(chatManager.coloredRawMessage("&l--------------------------------------------"));
	}

	@Override
	public String getTutorial() {
		return "Creates a new arena with default configuration";
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}

	private void setupDefaultConfiguration(String id) {
		String path = "instances." + id + ".", def = LocationSerializer.SERIALIZED_LOCATION;

		config.set(path + "ready", false);
		config.set(path + "endLocation", def);
		config.set(path + "centerLocation", def);
		config.set(path + "signs", new ArrayList<>());

		ConfigUtils.saveConfig(plugin, config, "arenas");

		Location defLoc = LocationSerializer.DEFAULT_LOCATION;
		Arena arena = new Arena(id);
		arena.setReady(false);
		arena.setEndLocation(defLoc);
		arena.setStartLocation(defLoc);

		ArenaRegistry.registerArena(arena);
	}
}