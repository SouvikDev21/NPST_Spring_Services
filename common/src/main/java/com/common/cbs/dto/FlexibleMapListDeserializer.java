package com.common.cbs.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.util.*;

public class FlexibleMapListDeserializer extends JsonDeserializer<Map<String, List<Object>>> implements ContextualDeserializer {

    private final JavaType targetElementType;

    public FlexibleMapListDeserializer() {
        this(null);
    }

    public FlexibleMapListDeserializer(JavaType targetElementType) {
        this.targetElementType = targetElementType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType mapType = property != null ? property.getType() : null;
        if (mapType != null && mapType.hasGenericTypes()) {
            JavaType listType = mapType.getContentType();
            if (listType != null && listType.hasGenericTypes()) {
                JavaType elemType = listType.getContentType();
                return new FlexibleMapListDeserializer(elemType);
            }
        }
        return new FlexibleMapListDeserializer(null);
    }

    @Override
    public Map<String, List<Object>> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode rootNode = mapper.readTree(p);

        if (rootNode == null || rootNode.isNull()) {
            return new LinkedHashMap<>();
        }

        Map<String, List<Object>> result = new LinkedHashMap<>();

        if (rootNode.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode itemNode : rootNode) {
                list.add(deserializeNode(itemNode, mapper));
            }
            result.put("items", list);
            return result;
        }

        if (rootNode.isObject()) {
            rootNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode valNode = entry.getValue();
                List<Object> list = new ArrayList<>();
                if (valNode.isArray()) {
                    for (JsonNode item : valNode) {
                        try {
                            list.add(deserializeNode(item, mapper));
                        } catch (Exception ignored) {}
                    }
                } else if (valNode.isObject()) {
                    try {
                        list.add(deserializeNode(valNode, mapper));
                    } catch (Exception ignored) {}
                }
                result.put(key, list);
            });
            return result;
        }

        return result;
    }

    private Object deserializeNode(JsonNode node, ObjectMapper mapper) throws IOException {
        if (targetElementType != null) {
            return mapper.treeToValue(node, targetElementType.getRawClass());
        }
        return mapper.treeToValue(node, Object.class);
    }
}
