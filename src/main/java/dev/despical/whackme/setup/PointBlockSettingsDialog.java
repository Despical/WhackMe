package dev.despical.whackme.setup;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.whackme.WhackMe;
import dev.despical.whackme.arena.Arena;
import dev.despical.whackme.arena.options.ArenaKeys;
import dev.despical.whackme.chat.ChatManager;
import dev.despical.whackme.util.Var;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Builds and applies the per-arena point block settings dialogs.
 *
 * @author Despical
 * <p>
 * Created at 14.07.2026
 */
public final class PointBlockSettingsDialog {

    private static final String ROOT = "point-block-settings-dialog.";

    private final WhackMe plugin;
    private final SetupMenu menu;
    private final Arena arena;
    private final ChatManager chatManager;
    private final FileConfiguration config;

    public PointBlockSettingsDialog(SetupMenu menu) {
        this.plugin = WhackMe.getInstance();
        this.menu = menu;
        this.arena = menu.getArena();
        this.chatManager = plugin.getChatManager();
        this.config = ConfigUtils.getConfig(plugin, "menu/setup-menu");
    }

    public void open(Player player) {
        showDialog(player, createMenuDialog());
    }

    private Dialog createMenuDialog() {
        DialogBase base = DialogBase.builder(text("title"))
            .externalTitle(text("external-title"))
            .canCloseWithEscape(true)
            .pause(false)
            .afterAction(DialogBase.DialogAfterAction.CLOSE)
            .body(List.of(DialogBody.plainMessage(text("description"), 300)))
            .build();

        List<ActionButton> settings = List.of(
            createGroupButton(Group.ACTIVE_BLOCKS),
            createGroupButton(Group.VERTICAL_MOVEMENT),
            createSettingButton(Setting.WAIT_TICKS),
            createSettingButton(Setting.HANDLER_TICKS),
            createSettingButton(Setting.RUN_ASYNC),
            createAppearanceButton()
        );

        DialogType type = DialogType.multiAction(settings)
            .columns(1)
            .exitAction(createExitButton())
            .build();

        return createDialog(base, type);
    }

    private Dialog createSettingDialog(Setting setting) {
        DialogBase base = DialogBase.builder(optionText(setting, "title"))
            .externalTitle(optionText(setting, "title"))
            .canCloseWithEscape(true)
            .pause(false)
            .afterAction(DialogBase.DialogAfterAction.CLOSE)
            .body(List.of(DialogBody.plainMessage(optionText(setting, "description"), 260)))
            .inputs(List.of(createInput(setting)))
            .build();

        DialogType type = DialogType.confirmation(
            createSaveButton(setting),
            createBackButton()
        );

        return createDialog(base, type);
    }

    private Dialog createGroupDialog(Group group) {
        DialogBase base = DialogBase.builder(groupText(group, "title"))
            .externalTitle(groupText(group, "title"))
            .canCloseWithEscape(true)
            .pause(false)
            .afterAction(DialogBase.DialogAfterAction.CLOSE)
            .body(List.of(DialogBody.plainMessage(groupText(group, "description"), 280)))
            .inputs(group.settings.stream()
                .map(this::createInput)
                .toList())
            .build();

        DialogType type = DialogType.confirmation(
            createGroupSaveButton(group),
            createBackButton()
        );

        return createDialog(base, type);
    }

    private Dialog createDialog(DialogBase base, DialogType type) {
        return Dialog.create(factory -> factory.empty().base(base).type(type));
    }

    private ActionButton createSettingButton(Setting setting) {
        Component label = optionText(setting, "title")
            .append(Component.text("  •  " + currentValue(setting), NamedTextColor.WHITE));

        return ActionButton.builder(label)
            .tooltip(optionText(setting, "description"))
            .width(240)
            .action(DialogAction.customClick(
                (_, audience) -> showSetting(audience, setting),
                callbackOptions()
            ))
            .build();
    }

    private ActionButton createGroupButton(Group group) {
        Component label = groupText(group, "title")
            .append(Component.text("  •  " + currentValue(group), NamedTextColor.WHITE));

        return ActionButton.builder(label)
            .tooltip(groupText(group, "description"))
            .width(240)
            .action(DialogAction.customClick(
                (_, audience) -> showGroup(audience, group),
                callbackOptions()
            ))
            .build();
    }

    private ActionButton createAppearanceButton() {
        return ActionButton.builder(text("buttons.appearance.label"))
            .tooltip(text("buttons.appearance.tooltip"))
            .width(240)
            .action(DialogAction.customClick(
                (_, audience) -> openPointBlockAppearance(audience),
                callbackOptions()
            ))
            .build();
    }

    private ActionButton createSaveButton(Setting setting) {
        return ActionButton.builder(text("buttons.save.label"))
            .tooltip(text("buttons.save.tooltip"))
            .width(120)
            .action(DialogAction.customClick(
                (response, audience) -> save(setting, response, audience),
                callbackOptions()
            ))
            .build();
    }

    private ActionButton createGroupSaveButton(Group group) {
        return ActionButton.builder(text("buttons.save.label"))
            .tooltip(text("buttons.save.tooltip"))
            .width(120)
            .action(DialogAction.customClick(
                (response, audience) -> save(group, response, audience),
                callbackOptions()
            ))
            .build();
    }

    private ActionButton createBackButton() {
        return ActionButton.builder(text("buttons.back.label"))
            .tooltip(text("buttons.back.tooltip"))
            .width(120)
            .action(DialogAction.customClick(
                (_, audience) -> showMenu(audience),
                callbackOptions()
            ))
            .build();
    }

    private ActionButton createExitButton() {
        return ActionButton.builder(text("buttons.exit.label"))
            .tooltip(text("buttons.exit.tooltip"))
            .width(160)
            .action(DialogAction.customClick(
                (_, audience) -> reopenSetupMenu(audience),
                callbackOptions()
            ))
            .build();
    }

    private DialogInput createInput(Setting setting) {
        int portalCount = arena.getOption(ArenaKeys.PORTAL_LOCATIONS).size();
        int pointRangeEnd = Math.max(2, portalCount);

        return switch (setting) {
            case MINIMUM_POINTS -> range(setting, 1, pointRangeEnd, arena.getOption(ArenaKeys.MINIMUM_POINTS), 1);
            case MAXIMUM_POINTS -> range(setting, 1, pointRangeEnd, arena.getOption(ArenaKeys.MAXIMUM_POINTS), 1);
            case Y_MULTIPLIER -> range(setting, 0.01f, 0.2f, arena.getOption(ArenaKeys.POINT_BLOCK_Y_MULTIPLIER).floatValue(), 0.01f);
            case MAX_Y_MULTIPLIER -> range(setting, 0.01f, 1.0f, arena.getOption(ArenaKeys.POINT_BLOCK_MAX_Y_MULTIPLIER).floatValue(), 0.01f);
            case WAIT_TICKS -> range(setting, 0, 40, arena.getOption(ArenaKeys.POINT_BLOCK_WAIT_TICKS), 1);
            case HANDLER_TICKS -> range(setting, 1, 20, arena.getOption(ArenaKeys.POINT_BLOCK_TICKS), 1);
            case RUN_ASYNC -> DialogInput.bool(setting.inputKey, optionText(setting, "input-label"))
                .initial(arena.getOption(ArenaKeys.POINT_BLOCKS_RUN_ASYNC))
                .build();
        };
    }

    private DialogInput range(Setting setting, float start, float end, float initial, float step) {
        Component label = optionText(setting, "input-label");

        return DialogInput.numberRange(setting.inputKey, label, start, end)
            .width(220)
            .labelFormat("%s: %s")
            .initial(Math.clamp(initial, start, end))
            .step(step)
            .build();
    }

    private void save(Setting setting, DialogResponseView response, Audience audience) {
        runForPlayer(audience, player -> {
            apply(setting, response);
            showDialog(player, createMenuDialog());
        });
    }

    private void save(Group group, DialogResponseView response, Audience audience) {
        runForPlayer(audience, player -> {
            apply(group, response);
            showDialog(player, createMenuDialog());
        });
    }

    private void apply(Group group, DialogResponseView response) {
        switch (group) {
            case ACTIVE_BLOCKS -> {
                int portalCount = Math.max(1, arena.getOption(ArenaKeys.PORTAL_LOCATIONS).size());
                int minimum = Math.clamp(integerValue(response, Setting.MINIMUM_POINTS, arena.getOption(ArenaKeys.MINIMUM_POINTS)), 1, portalCount);
                int maximum = Math.clamp(integerValue(response, Setting.MAXIMUM_POINTS, arena.getOption(ArenaKeys.MAXIMUM_POINTS)), 1, portalCount);

                if (maximum < minimum) {
                    maximum = minimum;
                }

                arena.setOption(ArenaKeys.MINIMUM_POINTS, minimum);
                arena.setOption(ArenaKeys.MAXIMUM_POINTS, maximum);
            }
            case VERTICAL_MOVEMENT -> {
                arena.setOption(
                    ArenaKeys.POINT_BLOCK_Y_MULTIPLIER,
                    decimalValue(response, Setting.Y_MULTIPLIER, arena.getOption(ArenaKeys.POINT_BLOCK_Y_MULTIPLIER))
                );
                arena.setOption(
                    ArenaKeys.POINT_BLOCK_MAX_Y_MULTIPLIER,
                    decimalValue(response, Setting.MAX_Y_MULTIPLIER, arena.getOption(ArenaKeys.POINT_BLOCK_MAX_Y_MULTIPLIER))
                );
            }
        }
    }

    private void apply(Setting setting, DialogResponseView response) {
        switch (setting) {
            case MINIMUM_POINTS -> {
                int portalCount = Math.max(1, arena.getOption(ArenaKeys.PORTAL_LOCATIONS).size());
                int minimum = Math.clamp(integerValue(response, setting, arena.getOption(ArenaKeys.MINIMUM_POINTS)), 1, portalCount);
                arena.setOption(ArenaKeys.MINIMUM_POINTS, minimum);

                if (arena.getOption(ArenaKeys.MAXIMUM_POINTS) < minimum) {
                    arena.setOption(ArenaKeys.MAXIMUM_POINTS, minimum);
                }
            }
            case MAXIMUM_POINTS -> {
                int portalCount = Math.max(1, arena.getOption(ArenaKeys.PORTAL_LOCATIONS).size());
                int maximum = Math.clamp(integerValue(response, setting, arena.getOption(ArenaKeys.MAXIMUM_POINTS)), 1, portalCount);
                arena.setOption(ArenaKeys.MAXIMUM_POINTS, maximum);

                if (arena.getOption(ArenaKeys.MINIMUM_POINTS) > maximum) {
                    arena.setOption(ArenaKeys.MINIMUM_POINTS, maximum);
                }
            }
            case Y_MULTIPLIER -> arena.setOption(
                ArenaKeys.POINT_BLOCK_Y_MULTIPLIER,
                decimalValue(response, setting, arena.getOption(ArenaKeys.POINT_BLOCK_Y_MULTIPLIER))
            );
            case MAX_Y_MULTIPLIER -> arena.setOption(
                ArenaKeys.POINT_BLOCK_MAX_Y_MULTIPLIER,
                decimalValue(response, setting, arena.getOption(ArenaKeys.POINT_BLOCK_MAX_Y_MULTIPLIER))
            );
            case WAIT_TICKS -> arena.setOption(
                ArenaKeys.POINT_BLOCK_WAIT_TICKS,
                integerValue(response, setting, arena.getOption(ArenaKeys.POINT_BLOCK_WAIT_TICKS))
            );
            case HANDLER_TICKS -> arena.setOption(
                ArenaKeys.POINT_BLOCK_TICKS,
                integerValue(response, setting, arena.getOption(ArenaKeys.POINT_BLOCK_TICKS))
            );
            case RUN_ASYNC -> arena.setOption(
                ArenaKeys.POINT_BLOCKS_RUN_ASYNC,
                booleanValue(response, setting, arena.getOption(ArenaKeys.POINT_BLOCKS_RUN_ASYNC))
            );
        }
    }

    private void showSetting(Audience audience, Setting setting) {
        runForPlayer(audience, player -> showDialog(player, createSettingDialog(setting)));
    }

    private void showGroup(Audience audience, Group group) {
        runForPlayer(audience, player -> showDialog(player, createGroupDialog(group)));
    }

    private void showMenu(Audience audience) {
        runForPlayer(audience, player -> showDialog(player, createMenuDialog()));
    }

    private void reopenSetupMenu(Audience audience) {
        runForPlayer(audience, player -> {
            plugin.getSetupDialogTracker().release(player);
            menu.open();
        });
    }

    private void openPointBlockAppearance(Audience audience) {
        runForPlayer(audience, player -> {
            plugin.getSetupDialogTracker().release(player);
            menu.setPage(3);
            menu.open();
        });
    }

    private void runForPlayer(Audience audience, java.util.function.Consumer<Player> action) {
        if (!(audience instanceof Player player) || !plugin.isEnabled()) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (plugin.isEnabled() && player.isOnline()) {
                action.accept(player);
            }
        });
    }

    private void showDialog(Player player, Dialog dialog) {
        plugin.getSetupDialogTracker().show(player, dialog);
    }

    private int integerValue(DialogResponseView response, Setting setting, int defaultValue) {
        Float value = response.getFloat(setting.inputKey);
        return value == null ? defaultValue : Math.round(value);
    }

    private double decimalValue(DialogResponseView response, Setting setting, double defaultValue) {
        Float value = response.getFloat(setting.inputKey);
        return value == null ? defaultValue : Math.round(value * 100.0) / 100.0;
    }

    private boolean booleanValue(DialogResponseView response, Setting setting, boolean defaultValue) {
        Boolean value = response.getBoolean(setting.inputKey);
        return value == null ? defaultValue : value;
    }

    private String currentValue(Setting setting) {
        return switch (setting) {
            case MINIMUM_POINTS -> Integer.toString(arena.getOption(ArenaKeys.MINIMUM_POINTS));
            case MAXIMUM_POINTS -> Integer.toString(arena.getOption(ArenaKeys.MAXIMUM_POINTS));
            case Y_MULTIPLIER -> decimal(arena.getOption(ArenaKeys.POINT_BLOCK_Y_MULTIPLIER));
            case MAX_Y_MULTIPLIER -> decimal(arena.getOption(ArenaKeys.POINT_BLOCK_MAX_Y_MULTIPLIER));
            case WAIT_TICKS -> arena.getOption(ArenaKeys.POINT_BLOCK_WAIT_TICKS) + " ticks";
            case HANDLER_TICKS -> arena.getOption(ArenaKeys.POINT_BLOCK_TICKS) + " ticks";
            case RUN_ASYNC -> textValue(arena.getOption(ArenaKeys.POINT_BLOCKS_RUN_ASYNC) ? "enabled-value" : "disabled-value");
        };
    }

    private String currentValue(Group group) {
        return switch (group) {
            case ACTIVE_BLOCKS -> arena.getOption(ArenaKeys.MINIMUM_POINTS)
                + " – " + arena.getOption(ArenaKeys.MAXIMUM_POINTS);
            case VERTICAL_MOVEMENT -> decimal(arena.getOption(ArenaKeys.POINT_BLOCK_Y_MULTIPLIER))
                + " / " + decimal(arena.getOption(ArenaKeys.POINT_BLOCK_MAX_Y_MULTIPLIER));
        };
    }

    private String decimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private String textValue(String path) {
        return config.getString(ROOT + path, "");
    }

    private ClickCallback.Options callbackOptions() {
        return ClickCallback.Options.builder()
            .uses(1)
            .lifetime(Duration.ofMinutes(10))
            .build();
    }

    private Component optionText(Setting setting, String path) {
        return text("options." + setting.configKey + "." + path);
    }

    private Component groupText(Group group, String path) {
        return text("groups." + group.configKey + "." + path);
    }

    private Component text(String path) {
        return chatManager.parseMessage(
            config.getString(ROOT + path, ""),
            Var.of("%arena_id%", arena.getId())
        );
    }

    private enum Setting {
        MINIMUM_POINTS("minimum-points", "minimum_points"),
        MAXIMUM_POINTS("maximum-points", "maximum_points"),
        Y_MULTIPLIER("y-multiplier", "y_multiplier"),
        MAX_Y_MULTIPLIER("max-y-multiplier", "max_y_multiplier"),
        WAIT_TICKS("wait-ticks", "wait_ticks"),
        HANDLER_TICKS("handler-ticks", "handler_ticks"),
        RUN_ASYNC("run-async", "run_async");

        private final String configKey;
        private final String inputKey;

        Setting(String configKey, String inputKey) {
            this.configKey = configKey;
            this.inputKey = inputKey;
        }
    }

    private enum Group {
        ACTIVE_BLOCKS("active-blocks", List.of(Setting.MINIMUM_POINTS, Setting.MAXIMUM_POINTS)),
        VERTICAL_MOVEMENT("vertical-movement", List.of(Setting.Y_MULTIPLIER, Setting.MAX_Y_MULTIPLIER));

        private final String configKey;
        private final List<Setting> settings;

        Group(String configKey, List<Setting> settings) {
            this.configKey = configKey;
            this.settings = settings;
        }
    }
}
