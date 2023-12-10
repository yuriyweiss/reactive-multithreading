package yuriy.weiss.common.utils.json.adapter;

import java.io.IOException;
import java.text.DateFormat;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase;

import yuriy.weiss.common.utils.DateUtils;

/**
 * Serializer для конвертации полей типа LocalDateTime в JSON.
 */
@JacksonStdImpl
public class LocalDateTimeSerializer extends DateTimeSerializerBase<LocalDateTime> {
    public LocalDateTimeSerializer() {
        this( ( Boolean ) null, ( DateFormat ) null );
    }

    public LocalDateTimeSerializer( Boolean useTimestamp, DateFormat customFormat ) {
        super( LocalDateTime.class, useTimestamp, customFormat );
    }

    public LocalDateTimeSerializer withFormat( Boolean timestamp, DateFormat customFormat ) {
        return new LocalDateTimeSerializer( timestamp, customFormat );
    }

    @Override
    protected long _timestamp( final LocalDateTime value ) {
        return value == null ? 0L : DateUtils.toDate( value ).getTime();
    }

    @Override
    public void serialize( final LocalDateTime value, final JsonGenerator g,
            final SerializerProvider provider ) throws IOException {
        if ( this._asTimestamp( provider ) ) {
            g.writeString( DateUtils.formatDateAndLongTime( value ) );
        } else {
            this._serializeAsString( DateUtils.toDate( value ), g, provider );
        }
    }
}

