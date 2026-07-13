package dev.despical.whackme.arena.options;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

/**
 * @author Despical
 * <p>
 * Created at 2.06.2026
 */
public class LocationTypeAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        if (src == null || src.getWorld() == null) {
            return JsonNull.INSTANCE;
        }
        json.addProperty("world", src.getWorld().getName());
        json.addProperty("x", src.getX());
        json.addProperty("y", src.getY());
        json.addProperty("z", src.getZ());
        json.addProperty("yaw", src.getYaw());
        json.addProperty("pitch", src.getPitch());
        return json;
    }

    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || !json.isJsonObject()) {
            return null;
        }

        JsonObject obj = json.getAsJsonObject();

        World world = Bukkit.getWorld(obj.get("world").getAsString());
        if (world == null) return null;

        double x = obj.get("x").getAsDouble();
        double y = obj.get("y").getAsDouble();
        double z = obj.get("z").getAsDouble();

        float yaw = obj.has("yaw") ? obj.get("yaw").getAsFloat() : 0.0f;
        float pitch = obj.has("pitch") ? obj.get("pitch").getAsFloat() : 0.0f;

        return new Location(world, x, y, z, yaw, pitch);
    }
}
