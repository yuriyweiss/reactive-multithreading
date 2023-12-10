package yuriy.weiss.common.utils.json.adapter;

import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase;

import yuriy.weiss.common.utils.DateUtils;

/**
 * Serializer для конвертации полей типа LocalDate в JSON.
 */
@JacksonStdImpl
public class LocalDateSerializer extends DateTimeSerializerBase<LocalDate> {
    public LocalDateSerializer() {
        this( ( Boolean ) null, ( DateFormat ) null );
    }

    public LocalDateSerializer( Boolean useTimestamp, DateFormat customFormat ) {
        super( LocalDate.class, useTimestamp, customFormat );
    }

    public LocalDateSerializer withFormat( Boolean timestamp, DateFormat customFormat ) {
        return new LocalDateSerializer( timestamp, customFormat );
    }

    @Override
    protected long _timestamp( final LocalDate value ) {
        return value == null ? 0L : DateUtils.toDate( value ).getTime();
    }

    @Override
    public void serialize( final LocalDate value, final JsonGenerator g,
            final SerializerProvider provider ) throws IOException {
        if ( this._asTimestamp( provider ) ) {
            g.writeString( DateUtils.formatDate( value ) );
        } else {
            this._serializeAsString( DateUtils.toDate( value ), g, provider );
        }
    }
}
