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

/**
 * @author Despical
 * <p>
 * Created at 20.02.2026
 */
public final class ArenaKeys {

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Location.class, new LocationTypeAdapter())
        .create();
    public static final ArenaOption<Boolean> READY = new ArenaOption<>("ready", false, Boolean.class) {

        @Override
        protected Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    };

    public static final ArenaOption<Boolean> CUSTOM = new ArenaOption<>("custom", false, Boolean.class) {

        @Override
        protected Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    };

    public static final ArenaOption<Location> START_LOCATION = new ArenaOption<>("startLocation", null, Location.class) {

        @Override
        public Object serialize(Location value) {
            return LocationSerializer.toString(value);
        }

        @Override
        protected Location parse(String value) {
            return LocationSerializer.fromString(value);
        }
    };

    public static final ArenaOption<Location> END_LOCATION = new ArenaOption<>("endLocation", null, Location.class) {

        @Override
        public Object serialize(Location value) {
            return LocationSerializer.toString(value);
        }

        @Override
        protected Location parse(String value) {
            return LocationSerializer.fromString(value);
        }
    };

    public static final ArenaOption<Integer> MINIMUM_POINTS = new ArenaOption<>("minPoints", 4, Integer.class) {

        @Override
        protected Integer parse(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return 4;
            }
        }
    };

    public static final ArenaOption<Integer> MAXIMUM_POINTS = new ArenaOption<>("maxPoints", 8, Integer.class) {

        @Override
        protected Integer parse(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return 8;
            }
        }
    };

    public static final ArenaOption<String> RECORD_HOLDER = new ArenaOption<>("record-holder", "None", String.class) {

        @Override
        protected String parse(String value) {
            return value == null || value.isBlank() ? "None" : value;
        }
    };

    public static final ArenaOption<Integer> RECORD_SCORE = new ArenaOption<>("record-score", -1, Integer.class) {

        @Override
        protected Integer parse(String value) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
    };

    public static final ArenaOption<List<Location>> PORTAL_LOCATIONS = new ArenaOption<>("portalLocations", new ArrayList<>(),(Class<List<Location>>) (Class<?>) List.class) {

        @Override
        public Object serialize(List<Location> value) {
            return gson.toJson(value);
        }

        @Override
        protected List<Location> parse(String value) {
            return gson.fromJson(value, new TypeToken<List<Location>>() {}.getType());
        }
    };

    public static final ArenaOption<String> ARENA_SONG = new ArenaOption<>("arena-song", null, String.class) {

        @Override
        protected String parse(String value) {
            return value == null || value.isEmpty() || value.equalsIgnoreCase("null") ? null : value;
        }
    };

    public static final ArenaOption<ItemStack> GREEN_BLOCK_ITEM = createPointBlockOption("greenBlockItem", Material.LIME_TERRACOTTA);
    public static final ArenaOption<ItemStack> RED_BLOCK_ITEM = createPointBlockOption("redBlockItem", Material.RED_TERRACOTTA);
    public static final ArenaOption<ItemStack> GRAY_BLOCK_ITEM = createPointBlockOption("grayBlockItem", Material.CYAN_TERRACOTTA);

    @SuppressWarnings("unchecked")
    public static final ArenaOption<List<PointBlock>> POINT_BLOCKS = new ArenaOption<>(
        "pointBlocks",
        new ArrayList<>(),
        (Class<List<PointBlock>>) (Class<?>) List.class
    ) {
        @Override
        public boolean isPersistent() {
            return false;
        }
    };

    private static ArenaOption<ItemStack> createPointBlockOption(String key, Material material) {
        return new ArenaOption<>(key, new ItemStack(material), ItemStack.class) {

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

    public static List<ArenaOption<?>> getAllKeys() {
        return List.of(
            READY,
            CUSTOM,
            START_LOCATION,
            END_LOCATION,
            MINIMUM_POINTS,
            MAXIMUM_POINTS,
            RECORD_HOLDER,
            RECORD_SCORE,
            PORTAL_LOCATIONS,
            ARENA_SONG,
            GREEN_BLOCK_ITEM,
            RED_BLOCK_ITEM,
            GRAY_BLOCK_ITEM,
            POINT_BLOCKS
        );
    }

    public static List<ArenaOption<?>> getPersistentKeys() {
        return PersistentKeysHolder.KEYS;
    }

    /**
     * @author Despical
     * <p>
     * Created at 20.02.2026
     */
    private static final class PersistentKeysHolder {

        private static final List<ArenaOption<?>> KEYS = getAllKeys()
            .stream()
            .filter(ArenaOption::isPersistent)
            .toList();
    }
}
