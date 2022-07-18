package me.despical.whackme.command.admin;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.arena.ArenaRegistry;
import me.despical.whackme.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		final Player player = (Player) sender;

		if (args.length == 0) {
			player.sendMessage(chatManager.prefixedRawMessage("&cPlease enter an name to create an arena!"));
			return;
		}

		final String id = args[0];

		if (ArenaRegistry.isArena(id)) {
			player.sendMessage(chatManager.prefixedRawMessage("&cArena with that ID already contains!"));
			player.sendMessage(chatManager.prefixedRawMessage("&cTo check existing arenas use: /wm list"));
			return;
		}

		player.sendMessage(chatManager.coloredRawMessage("&l--------------------------------------------"));
		MiscUtils.sendCenteredMessage(player, "&eInstance &a&l" + id + " &ecreated!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&aEdit this arena via /wm edit &6" + id + "&a!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&6Don't know where to start? Check out our wiki:");
		MiscUtils.sendCenteredMessage(player, "&7https://www.github.com/Despical/WhackMe/wiki");
		player.sendMessage(chatManager.coloredRawMessage("&l--------------------------------------------"));

		final String path = "instances." + id + ".";

		config.set(path + "ready", false);
		config.set(path + "endLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "centerLocation", LocationSerializer.SERIALIZED_LOCATION);

		ConfigUtils.saveConfig(plugin, config, "arenas");

		Arena arena = new Arena(id);
		arena.setReady(false);
		arena.setEndLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setStartLocation(LocationSerializer.DEFAULT_LOCATION);

		ArenaRegistry.registerArena(arena);
	}

	@Override
	public String getTutorial() {
		return "Creates a new arena with default configuration";
	}

	@Override
	public int getType() {
		return GENERIC;
	}

	@Override
	public int getSenderType() {
		return PLAYER;
	}
}