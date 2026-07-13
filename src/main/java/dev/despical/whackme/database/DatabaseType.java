package dev.despical.whackme.database;

import java.util.Arrays;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 6.12.2025
 */
public enum DatabaseType {

    FLAT_FILE("Flat File", "flat", "flatfile", "default"),
    MYSQL("MySQL", "mysql", "sql");

    private final List<String> names;

    DatabaseType(String... names) {
        this.names = Arrays.asList(names);
    }

    public String getName() {
        return names.getFirst();
    }

    public static DatabaseType getByName(String name) {
        return Arrays.stream(values())
            .filter(type -> type.names.contains(name))
            .findFirst()
            .orElse(null);
    }
}
