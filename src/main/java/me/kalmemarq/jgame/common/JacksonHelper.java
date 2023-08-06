package me.kalmemarq.jgame.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JacksonHelper {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static int[] arrayNodeToIntArray(ArrayNode arrayNode) {
        int[] arr = new int[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); ++i) {
            arr[i] = arrayNode.get(i).intValue();
        }
        return arr;
    }

    public static float[] arrayNodeToFloatArray(ArrayNode arrayNode) {
        float[] arr = new float[arrayNode.size()];
        for (int i = 0; i < arrayNode.size(); ++i) {
            arr[i] = arrayNode.get(i).floatValue();
        }
        return arr;
    }
}
