package com.westmonroe.loansyndication.utils;

import java.util.List;
import java.util.Map;

public class MapUtils {

    /**
     * Traverses a nested map to get the value of a specified node.
     *
     * @param map   The map to traverse.
     * @param path  The path to the node, with the last element being the key of the desired value.
     *
     * @return The value of the specified node, or null if the path is invalid.
     */
    public static Object getNodeValue(Map<String, Object> map, List<String> path) {
        Object current = map;

        for ( String key : path ) {
            if ( current instanceof Map ) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                return null;
            }
        }

        return current;
    }

}