package com.multikube_rest_service.common.utils;

import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

public class FilterStringParser {

    /**
     * Parses a filter string (e.g., "name==cluster1,status==ACTIVE") into a map.
     * Keys are normalized to lowercase.
     *
     * @param filterString The filter string to parse.
     * @return A map of filter keys to filter values. Returns an empty map if the filterString is null or empty.
     */
    public static Map<String, String> parse(String filterString) {
        Map<String, String> filters = new HashMap<>();
        if (StringUtils.hasText(filterString)) {
            String[] pairs = filterString.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split("==", 2); // Split only on the first "=="
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().toLowerCase(); // Normalize key to lowercase
                    String value = keyValue[1].trim();
                    if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                        filters.put(key, value);
                    }
                }
            }
        }
        return filters;
    }
}