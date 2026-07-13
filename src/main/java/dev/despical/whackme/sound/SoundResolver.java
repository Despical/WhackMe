package dev.despical.whackme.sound;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2026
 */
public final class SoundResolver {

    private SoundResolver() {
    }

    public static Sound resolve(String rawSound) {
        if (rawSound == null || rawSound.isBlank()) {
            return null;
        }

        String normalized = rawSound.trim().toLowerCase(Locale.ENGLISH);
        if (normalized.startsWith("minecraft:")) {
            normalized = normalized.substring("minecraft:".length());
        }

        NamespacedKey key = NamespacedKey.fromString(normalized.contains(":") ? normalized : "minecraft:" + normalized);
        Sound sound = key == null ? null : Registry.SOUNDS.get(key);
        if (sound != null) {
            return sound;
        }

        String fieldName = rawSound.trim().toUpperCase(Locale.ENGLISH).replace('.', '_').replace(':', '_');
        try {
            Field field = Sound.class.getField(fieldName);
            Object value = field.get(null);
            return value instanceof Sound resolved ? resolved : null;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
    }
}
