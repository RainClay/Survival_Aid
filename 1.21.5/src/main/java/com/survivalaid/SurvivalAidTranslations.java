package com.survivalaid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SurvivalAidTranslations {

    private static final Map<String, String> ZH_CN = load("zh_cn.json");
    private static final Map<String, String> EN_US = load("en_us.json");

    private SurvivalAidTranslations() {
    }

    public static Map<String, String> get(String lang) {
        if (lang != null && lang.toLowerCase().startsWith("zh") && !ZH_CN.isEmpty()) {
            return ZH_CN;
        }
        if (!EN_US.isEmpty()) {
            return EN_US;
        }
        return ZH_CN;
    }

    private static Map<String, String> load(String fileName) {
        try (InputStream in = SurvivalAidTranslations.class.getClassLoader()
                .getResourceAsStream("assets/survival_aid/lang/" + fileName)) {
            if (in == null) {
                return Map.of();
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, String> translations = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    translations.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            return Map.copyOf(translations);
        } catch (Throwable t) {
            return Map.of();
        }
    }
}
