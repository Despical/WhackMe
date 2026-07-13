package dev.despical.whackme.bossbar;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.chat.ChatManager;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Despical
 * <p>
 * Created at 28.01.2026
 */
@Getter
public class BossBarConfig {

    private boolean enabled;
    private BossBarData data;

    @Getter(AccessLevel.NONE)
    private final WhackMe plugin;

    public BossBarConfig(WhackMe plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "bossbar");
        ChatManager chatManager = plugin.getChatManager();

        this.enabled = config.getBoolean("enabled", true);

        String text = config.getString("text", "");
        this.data = new BossBarData(
                chatManager.parseMessage(text),
                BossBar.Color.valueOf(config.getString("color", "BLUE")),
                BossBar.Overlay.valueOf(config.getString("overlay", "PROGRESS")),
                !text.isEmpty()
        );
    }

    /**
     * @author Despical
     * <p>
     * Created at 28.01.2026
     */
    public record BossBarData(Component title, BossBar.Color color, BossBar.Overlay overlay, boolean visible) {
    }
}
