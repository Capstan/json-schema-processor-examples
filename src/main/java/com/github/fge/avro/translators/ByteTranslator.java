package com.github.fge.avro.translators;

import com.github.fge.avro.MutableTree;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.report.ProcessingReport;
import org.apache.avro.Schema;

public final class ByteTranslator
    extends AvroTranslator
{
    private static final String BYTES_PATTERN = "^[\\u0000-\\u00ff]*$";

    private static final AvroTranslator INSTANCE = new ByteTranslator();

    private ByteTranslator()
    {
    }

    public static AvroTranslator getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void translate(final Schema avroSchema, final MutableTree jsonSchema,
        final ProcessingReport report)
        throws ProcessingException
    {
        jsonSchema.getCurrentNode().put("type", "string")
            .put("pattern", BYTES_PATTERN);
    }
}
