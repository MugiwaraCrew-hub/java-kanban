package org.example.utill;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

public class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {
    @Override
    public JsonElement serialize(Duration src, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Duration deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        return Duration.parse(json.getAsString());
    }
}
