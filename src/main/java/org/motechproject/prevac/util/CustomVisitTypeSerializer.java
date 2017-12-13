package org.motechproject.prevac.util;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.motechproject.prevac.domain.enums.VisitType;

import java.io.IOException;

/**
 * Serializer for VisitType representation in UI
 */
public class CustomVisitTypeSerializer extends JsonSerializer<VisitType> {

    @Override
    public void serialize(VisitType value, JsonGenerator gen,
                          SerializerProvider arg2)
            throws IOException {
        gen.writeString(value.getDisplayValue());
    }
}
