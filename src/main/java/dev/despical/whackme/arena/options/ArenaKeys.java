package dev.despical.whackme.arena.options;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.whackme.arena.blocks.PointBlock;
import dev.despical.whackme.util.ItemStackSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Despical
 * <p>
 * Created at 20.02.2026
 */
public final class ArenaKeys {

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Location.class, new LocationTypeAdapter())
        .create();

    public static final ArenaOption<Boolean>   READY = booleanOption("ready", false);
    public static final ArenaOption<Boolean>   CUSTOM = booleanOption("custom", false);
    public static final ArenaOption<Boolean>   POINT_BLOCKS_RUN_ASYNC = booleanOption("pointBlocksRunAsync", false);
    public static final ArenaOption<Boolean>   ARENA_SCOREBOARD_ENABLED = booleanOption("arenaScoreboardEnabled", true);
    public static final ArenaOption<Boolean>   ARENA_BOSS_BAR_ENABLED = booleanOption("arenaBossBarEnabled", true);

    public static final ArenaOption<Location>  START_LOCATION = locationOption("startLocation");
    public static final ArenaOption<Location>  END_LOCATION = locationOption("endLocation");

    public static final ArenaOption<Integer>   MINIMUM_POINTS = integerOption("minPoints", 4);
    public static final ArenaOption<Integer>   MAXIMUM_POINTS = integerOption("maxPoints", 8);
    public static final ArenaOption<Integer>   POINT_BLOCK_TICKS = integerOption("pointBlockTicks", 8);
    public static final ArenaOption<Integer>   POINT_BLOCK_WAIT_TICKS = integerOption("pointBlockWaitTicks", 12);
    public static final ArenaOption<Integer>   RECORD_SCORE = integerOption("record-score", 0);

    public static final ArenaOption<Double>    POINT_BLOCK_Y_MULTIPLIER = doubleOption("pointBlockYMultiplier", 0.05);
    public static final ArenaOption<Double>    POINT_BLOCK_MAX_Y_MULTIPLIER = doubleOption("pointBlockMaxYMultiplier", 0.64);

    public static final ArenaOption<String>    RECORD_HOLDER = stringOption("record-holder", "None");
    public static final ArenaOption<String>    ARENA_SONG = stringOption("arena-song", null);

    public static final ArenaOption<ItemStack> GREEN_BLOCK_ITEM = createPointBlockOption("greenBlockItem", Material.LIME_TERRACOTTA);
    public static final ArenaOption<ItemStack> RED_BLOCK_ITEM = createPointBlockOption("redBlockItem", Material.RED_TERRACOTTA);
    public static final ArenaOption<ItemStack> GRAY_BLOCK_ITEM = createPointBlockOption("grayBlockItem", Material.CYAN_TERRACOTTA);

    public static final ArenaOption<List<Location>> PORTAL_LOCATIONS = new ArenaOption<>("portalLocations", (Class<List<Location>>) (Class<?>) List.class, (Supplier<List<Location>>) ArrayList::new) {

        @Override
        public Object serialize(List<Location> value) {
            return gson.toJson(value);
        }

        @Override
        protected List<Location> parse(String value) {
            return gson.fromJson(value, new TypeToken<List<Location>>() {
            }.getType());
        }
    };

    @SuppressWarnings("unchecked")
    public static final ArenaOption<List<PointBlock>> POINT_BLOCKS = new ArenaOption<>(
        "pointBlocks",
        (Class<List<PointBlock>>) (Class<?>) List.class,
        (Supplier<List<PointBlock>>) ArrayList::new
    ) {
        @Override
        public boolean isPersistent() {
            return false;
        }
    };

    private static ArenaOption<ItemStack> createPointBlockOption(String key, Material material) {
        return new ArenaOption<>(key, ItemStack.class, (Supplier<ItemStack>) () -> new ItemStack(material)) {

            @Override
            public Object serialize(ItemStack value) {
                return ItemStackSerializer.serialize(value);
            }

            @Override
            protected ItemStack parse(String value) {
                return ItemStackSerializer.deserialize(value);
            }
        };
    }

    private static ArenaOption<Boolean> booleanOption(String key, boolean defaultValue) {
        return new ArenaOption<>(key, Boolean.class, defaultValue) {

            @Override
            protected Boolean parse(String value) {
                return Boolean.parseBoolean(value);
            }
        };
    }

    private static ArenaOption<Integer> integerOption(String key, int defaultValue) {
        return new ArenaOption<>(key, Integer.class, defaultValue) {

            @Override
            protected Integer parse(String value) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException ignored) {
                    return defaultValue;
                }
            }
        };
    }

    private static ArenaOption<String> stringOption(String key, String defaultValue) {
        return new ArenaOption<>(key, String.class, defaultValue) {

            @Override
            protected String parse(String value) {
                return value == null || value.isBlank() ? defaultValue : value;
            }
        };
    }

    private static ArenaOption<Double> doubleOption(String key, double defaultValue) {
        return new ArenaOption<>(key, Double.class, defaultValue) {

            @Override
            protected Double parse(String value) {
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException ignored) {
                    return defaultValue;
                }
            }
        };
    }

    private static ArenaOption<Location> locationOption(String key) {
        return new ArenaOption<>(key, Location.class, (Location) null) {

            @Override
            public Object serialize(Location value) {
                return LocationSerializer.toString(value);
            }

            @Override
            protected Location parse(String value) {
                return LocationSerializer.fromString(value);
            }
        };
    }

    public static List<ArenaOption<?>> getAllKeys() {
        return ArenaKeysHolder.ALL_KEYS;
    }

    public static List<ArenaOption<?>> getPersistentKeys() {
        return ArenaKeysHolder.KEYS;
    }

    /**
     * @author Despical
     * <p>
     * Created at 20.02.2026
     */
    private static final class ArenaKeysHolder {

        private static final List<ArenaOption<?>> ALL_KEYS = List.of(
            READY,
            CUSTOM,
            START_LOCATION,
            END_LOCATION,
            MINIMUM_POINTS,
            MAXIMUM_POINTS,
            POINT_BLOCKS_RUN_ASYNC,
            POINT_BLOCK_TICKS,
            POINT_BLOCK_Y_MULTIPLIER,
            POINT_BLOCK_MAX_Y_MULTIPLIER,
            POINT_BLOCK_WAIT_TICKS,
            ARENA_SCOREBOARD_ENABLED,
            ARENA_BOSS_BAR_ENABLED,
            RECORD_HOLDER,
            RECORD_SCORE,
            PORTAL_LOCATIONS,
            ARENA_SONG,
            GREEN_BLOCK_ITEM,
            RED_BLOCK_ITEM,
            GRAY_BLOCK_ITEM,
            POINT_BLOCKS
        );

        private static final List<ArenaOption<?>> KEYS = ALL_KEYS.stream()
            .filter(ArenaOption::isPersistent)
            .toList();
    }
}
