package me.despical.whackme.handler.setup;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.whackme.ConfigPreferences;
import me.despical.whackme.Main;
import me.despical.whackme.arena.Arena;
import me.despical.whackme.handler.ChatManager;
import me.despical.whackme.handler.setup.components.ArenaRegisterComponent;
import me.despical.whackme.handler.setup.components.SpawnComponents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2022
 */
public class SetupInventory {

	private Gui gui;
	private final Main plugin;
	private final Arena arena;
	private final Player player;
	private final SetupUtilities setupUtilities;

	public SetupInventory(Arena arena, Player player) {
		this.arena = arena;
		this.player = player;
		this.plugin = JavaPlugin.getPlugin(Main.class);
		this.setupUtilities = new SetupUtilities(plugin);

		prepareGui();
	}

	private void prepareGui() {
		this.gui = new Gui(plugin, 3, "Arena Setup Menu");
		this.gui.setOnGlobalClick(e -> e.setCancelled(true));

		final StaticPane pane = new StaticPane(9, 3);
		final ItemBuilder registeredItem = new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).name("&aArena Validation Successful"),
			notRegisteredItem = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&cArena Validation Not Finished Yet");
		pane.fillWith(arena.isReady() ? registeredItem.build() : notRegisteredItem.build());
		pane.fillProgressBorder(GuiItem.of(registeredItem.build()), GuiItem.of(notRegisteredItem.build()), arena.isReady() ? 100 : 0);

		this.gui.addPane(pane);

		prepareComponents(pane);
	}

	private void prepareComponents(StaticPane pane) {
		final SpawnComponents spawnComponents = new SpawnComponents();
		spawnComponents.injectComponents(this, pane);

		final ArenaRegisterComponent arenaRegistryComponents = new ArenaRegisterComponent();
		arenaRegistryComponents.injectComponents(this, pane);
	}

	private void sendProTip(Player player) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.SEND_SETUP_TIPS)) return;

		final ChatManager chatManager = plugin.getChatManager();
		String tip = "";

		switch (ThreadLocalRandom.current().nextInt(12)) {
			case 0:
				tip = "Need help? You can join our Discord community. Check out https://discord.gg/rVkaGmyszE";
				break;
			case 1:
				tip = "Need help? Check our wiki: https://github.com/Despical/WhackMe/wiki";
				break;
			case 2:
				tip = "Help us translating our plugin to your language here: https://github.com/Despical/LocaleStorage/";
				break;
			case 3:
				tip = "You have suggestions to improve the plugin? Use our issue tracker or join our Discord server.";
				break;
			default:
				break;
		}

		if (!tip.isEmpty()) {
			player.sendMessage(chatManager.coloredRawMessage("&e&lTIP: &7" + tip));
		}
	}

	public void openInventory() {
		sendProTip(player);
		gui.show(player);
	}

	public Main getPlugin() {
		return plugin;
	}

	public Arena getArena() {
		return arena;
	}

	public Player getPlayer() {
		return player;
	}

	public SetupUtilities getSetupUtilities() {
		return setupUtilities;
	}
}