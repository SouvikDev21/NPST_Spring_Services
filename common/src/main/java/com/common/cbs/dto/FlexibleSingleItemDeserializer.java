package com.common.cbs.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;

public class FlexibleSingleItemDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private final JavaType targetType;

    public FlexibleSingleItemDeserializer() {
        this(null);
    }

    public FlexibleSingleItemDeserializer(JavaType targetType) {
        this.targetType = targetType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType type = property != null ? property.getType() : null;
        return new FlexibleSingleItemDeserializer(type);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isArray()) {
            if (!node.isEmpty()) {
                node = node.get(0);
            } else {
                return null;
            }
        }

        if (targetType != null) {
            return mapper.treeToValue(node, targetType.getRawClass());
        }
        return mapper.treeToValue(node, Object.class);
    }
}
