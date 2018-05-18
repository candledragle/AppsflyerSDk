package com.appsflyer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


public class AFHelper {

    public static JSONObject convertToJsonObject(Map<String, ?> map) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            return new JSONObject(map);
        } else {
            return toJsonObject(map);
        }
    }


    /**
     * Return a copy of the contents of the given map as a {@link JSONObject}. Instead of failing on
     * {@code null} values like the {@link JSONObject} map constructor, it cleans them up and
     * correctly converts them to {@link JSONObject#NULL}.
     */
    public static JSONObject toJsonObject(Map<String, ?> map) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Object value = wrap(entry.getValue());
            try {
                jsonObject.put(entry.getKey(), value);
            } catch (JSONException ignored) {
                // Ignore values that JSONObject doesn't accept.
            }
        }
        return jsonObject;
    }

    /**
     * Wraps the given object if necessary. {@link JSONObject#wrap(Object)} is only available on API
     * 19+, so we've copied the implementation. Deviates from the original implementation in that it
     * always returns {@link JSONObject#NULL} instead of {@code null} in case of a failure, and
     * returns the {@link Object#toString} of any object that is of a custom (non-primitive or
     * non-collection/map) type.
     *
     * <p>If the object is null returns {@link JSONObject#NULL}. If the object is a {@link JSONArray}
     * or {@link JSONObject}, no wrapping is necessary. If the object is {@link JSONObject#NULL}, no
     * wrapping is necessary. If the object is an array or {@link Collection}, returns an equivalent
     * {@link JSONArray}. If the object is a {@link Map}, returns an equivalent {@link JSONObject}. If
     * the object is a primitive wrapper type or {@link String}, returns the object. Otherwise returns
     * the result of {@link Object#toString}. If wrapping fails, returns JSONObject.NULL.
     */
    /**
     * Wraps the given object if necessary.
     * <p>
     * <p>If the object is null or , returns {@link JSONObject#NULL}.
     * If the object is a {@code JSONArray} or {@code JSONObject}, no wrapping is necessary.
     * If the object is {@code NULL}, no wrapping is necessary.
     * If the object is an array or {@code Collection}, returns an equivalent {@code JSONArray}.
     * If the object is a {@code Map}, returns an equivalent {@code JSONObject}.
     * If the object is a primitive wrapper type or {@code String}, returns the object.
     * Otherwise if the object is from a {@code java} package, returns the result of {@code toString}.
     * If wrapping fails, returns null.
     */
    private static Object wrap(Object o) {
        if (o == null) {
            return JSONObject.NULL;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }
        if (o.equals(JSONObject.NULL)) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                JSONArray array = new JSONArray();
                for (Iterator it = ((Collection) o).iterator(); it.hasNext(); ) {
                    array.put(wrap(it.next()));
                }
                return array;
            } else if (o.getClass().isArray()) {
                final int length = Array.getLength(o);
                JSONArray array = new JSONArray();
                for (int i = 0; i < length; ++i) {
                    array.put(wrap(Array.get(o, i)));
                }
                return array;
            }
            if (o instanceof Map) {
                //noinspection unchecked
                return toJsonObject((Map) o);
            }
            if (o instanceof Boolean
                    || o instanceof Byte
                    || o instanceof Character
                    || o instanceof Double
                    || o instanceof Float
                    || o instanceof Integer
                    || o instanceof Long
                    || o instanceof Short
                    || o instanceof String) {
                return o;
            }
            // Deviate from original implementation and return the String representation of the object
            // regardless of package.
            return o.toString();
        } catch (Exception ignored) {
        }
        // Deviate from original and return JSONObject.NULL instead of null.
        return JSONObject.NULL;
    }
}