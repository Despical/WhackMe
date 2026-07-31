package dev.despical.whackme.arena;

import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 31.07.2026
 */
@UtilityClass
public final class ArenaIdValidator {

    private static final Pattern VALID_ID = Pattern.compile("[^\\s.]{1,64}");

    public static boolean isValid(String id) {
        return id != null && VALID_ID.matcher(id).matches();
    }

    public static String normalize(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
