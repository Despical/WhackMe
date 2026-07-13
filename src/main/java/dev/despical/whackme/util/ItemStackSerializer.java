package dev.despical.whackme.util;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;

/**
 * @author Despical
 * <p>
 * Created at 03.06.2026
 */
@UtilityClass
public final class ItemStackSerializer {

    public static String serialize(ItemStack itemStack) {
        return Base64.getEncoder().encodeToString(itemStack.serializeAsBytes());
    }

    public static ItemStack deserialize(String value) {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(value));
    }
}
