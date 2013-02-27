package com.github.fge.avro.translators;

import com.github.fge.avro.UnsupportedAvroSchemaException;
import com.github.fge.jsonschema.report.ProcessingMessage;
import com.github.fge.jsonschema.util.NodeType;
import com.google.common.collect.ImmutableMap;
import org.apache.avro.Schema;

import java.util.Map;

import static com.github.fge.avro.messages.Avro2JsonSchemaMessages.*;

public final class AvroTranslators
{
    private static final Map<Schema.Type, AvroTranslator> TRANSLATORS;

    private AvroTranslators()
    {
    }

    static {
        final ImmutableMap.Builder<Schema.Type, AvroTranslator> builder
            = ImmutableMap.builder();

        Schema.Type avroType;
        AvroTranslator translator;

        avroType = Schema.Type.NULL;
        translator = new SimpleTypeTranslator(NodeType.NULL);
        builder.put(avroType, translator);

        avroType = Schema.Type.BOOLEAN;
        translator = new SimpleTypeTranslator(NodeType.BOOLEAN);
        builder.put(avroType, translator);

        avroType = Schema.Type.STRING;
        translator = new SimpleTypeTranslator(NodeType.STRING);
        builder.put(avroType, translator);

        // Reuse for "bytes"
        avroType = Schema.Type.BYTES;
        translator = ByteTranslator.getInstance();
        builder.put(avroType, translator);

        avroType = Schema.Type.INT;
        translator = IntTranslator.getInstance();
        builder.put(avroType, translator);

        avroType = Schema.Type.LONG;
        translator = LongTranslator.getInstance();
        builder.put(avroType, translator);

        avroType = Schema.Type.FLOAT;
        translator = new SimpleTypeTranslator(NodeType.NUMBER);
        builder.put(avroType, translator);

        // Reuse for "double"
        avroType = Schema.Type.DOUBLE;
        builder.put(avroType, translator);

        avroType = Schema.Type.MAP;
        translator = MapTranslator.getInstance();
        builder.put(avroType, translator);

        TRANSLATORS = builder.build();
    }

    public static AvroTranslator getTranslator(final Schema.Type avroType)
        throws UnsupportedAvroSchemaException
    {
        final AvroTranslator ret = TRANSLATORS.get(avroType);
        if (ret == null)
            throw new UnsupportedAvroSchemaException(new ProcessingMessage()
                .message(UNSUPPORTED_TYPE).put("type", avroType));
        return ret;
    }
}
