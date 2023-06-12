package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

@Component
public final class CsvHttpMessageConverter
    extends AbstractGenericHttpMessageConverter<Object> {

    private static final ImmutableList<Class<?>> SUPPORTED_SUPER_TYPES =
        ImmutableList.of(Iterable.class, Stream.class);

    private final CsvMapper csvMapper;


    CsvHttpMessageConverter() {
        super(new MediaType("text", "csv"));
        this.csvMapper = new CsvMapper();
        csvMapper.registerModule(new KotlinModule.Builder().build());
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return canRead(mediaType)
            && TypeToken.of(type).getRawType().isAssignableFrom(ImmutableList.class);
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return getTableType(clazz).isPresent();
    }

    @Override
    public ImmutableList<?> read(
        final Type type, final Class<?> contextClazz, final HttpInputMessage inputMessage)
        throws IOException {
        return ImmutableList.copyOf(getReader(getElementType(type)).readValues(getInputReader(inputMessage)));
    }

    @Override
    protected ImmutableList<?> readInternal(
        final Class<?> clazz, final HttpInputMessage inputMessage) throws IOException {

        return ImmutableList.copyOf(getReader(getElementType(clazz)).readValues(getInputReader(inputMessage)));
    }

    private Reader getInputReader(final HttpInputMessage inputMessage) throws IOException {
        final Charset contentCharSet =
            Optional.ofNullable(inputMessage.getHeaders().getContentType())
                .map(MediaType::getCharset)
                .orElse(StandardCharsets.UTF_8);
        return new InputStreamReader(inputMessage.getBody(), contentCharSet);
    }

    @VisibleForTesting
    public ObjectReader getReader(final Class<?> clazz) {
        return csvMapper.reader(getSchema(clazz)).forType(clazz);
    }

    @Override
    protected void writeInternal(
        final Object instance, final Type type, final HttpOutputMessage outputMessage) {
    }

    private CsvSchema getSchema(final Class<?> clazz) {
        return CsvSchema.emptySchema().withHeader();
    }

    private Class<?> getElementType(final Type type) {
        final Class<?> superType = getTableType(type).orElse(null);
        checkArgument(superType != null, "Type %s cannot be represented as a table", type);
        @SuppressWarnings("unchecked")
        ParameterizedType tableType =
            (ParameterizedType) TypeToken.of(type).getSupertype((Class) superType).getType();
        return TypeToken.of(tableType.getActualTypeArguments()[0]).getRawType();
    }

    private Optional<Class<?>> getTableType(final Type type) {
        return SUPPORTED_SUPER_TYPES.stream().filter(TypeToken.of(type)::isSubtypeOf).findAny();
    }
}
