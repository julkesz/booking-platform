package be.kuleuven.distributedsystems.cloud.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class LocalDateTimeTypeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return LocalDateTime.parse(json.getAsString(), Utils.DATE_TIME_FORMATTER1);
        } catch (DateTimeParseException dateTimeParseException1) {
            try {
                return LocalDateTime.parse(json.getAsString(), Utils.DATE_TIME_FORMATTER2);
            } catch (DateTimeParseException dateTimeParseException2) {
                try {
                    return LocalDateTime.parse(json.getAsString(), Utils.DATE_TIME_FORMATTER3);
                } catch (DateTimeParseException dateTimeParseException3) {
                    return LocalDateTime.parse(json.getAsString(), Utils.DATE_TIME_FORMATTER4);
                }
            }
        }
    }

    @Override
    public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
        try {
            return new JsonPrimitive(Utils.DATE_TIME_FORMATTER1.format(localDateTime));
        } catch (DateTimeParseException dateTimeParseException1) {
            try {
                return new JsonPrimitive(Utils.DATE_TIME_FORMATTER2.format(localDateTime));
            } catch (DateTimeParseException dateTimeParseException2) {
                try {
                    return new JsonPrimitive(Utils.DATE_TIME_FORMATTER3.format(localDateTime));
                } catch (DateTimeParseException dateTimeParseException3) {
                    return new JsonPrimitive(Utils.DATE_TIME_FORMATTER4.format(localDateTime));
                }
            }
        }
    }
}
