package com.cortex.currency.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class DoubleSerializer extends StdSerializer<Double> {

    public DoubleSerializer() {
        super(Double.class);
    }

    @Override
    public void serialize(Double valor, JsonGenerator generator, SerializerProvider sp) throws IOException {
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "EN"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);
        generator.writeNumber(nf.format(valor));
    }
}
