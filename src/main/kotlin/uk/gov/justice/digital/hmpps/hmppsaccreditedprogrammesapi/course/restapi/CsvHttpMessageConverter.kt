package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.restapi

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.reflect.TypeToken
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractGenericHttpMessageConverter
import org.springframework.stereotype.Component
import java.io.InputStreamReader
import java.io.Reader
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

@Component
class CsvHttpMessageConverter : AbstractGenericHttpMessageConverter<Iterable<Any>>(MediaType("text", "csv")) {
  private val csvMapper = CsvMapper().apply {
    registerModule(KotlinModule.Builder().build())
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(CsvParser.Feature.ALLOW_TRAILING_COMMA, true)
    configure(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE, true)
  }

  override fun canRead(type: Type, contextClass: Class<*>?, mediaType: MediaType?): Boolean = canRead(mediaType) && isSupported(type)

  override fun supports(clazz: Class<*>): Boolean = isSupported(clazz)

  override fun read(type: Type, contextClazz: Class<*>?, inputMessage: HttpInputMessage): List<Any> = readAll(type, inputMessage)

  override fun readInternal(clazz: Class<out Iterable<Any>>, inputMessage: HttpInputMessage): List<Any> = readAll(clazz, inputMessage)

  private fun readAll(type: Type, inputMessage: HttpInputMessage) = getCsvReader(type).readValues<Any>(getInputReader(inputMessage)).readAll()

  private fun getCsvReader(type: Type): ObjectReader = csvMapper.reader(getSchema()).forType(getElementType(type))

  override fun writeInternal(instance: Iterable<Any>, type: Type?, outputMessage: HttpOutputMessage) {
  }

  companion object {
    private val supportedSupertype = Iterable::class.java

    private fun getSchema(): CsvSchema = CsvSchema.emptySchema().withHeader()

    private fun isSupported(type: Type) = TypeToken.of(type).isSubtypeOf(supportedSupertype)

    @Suppress("UNCHECKED_CAST")
    private fun getElementType(type: Type) =
      (TypeToken.of(type).getSupertype(supportedSupertype as Class<Any>).type as ParameterizedType)
        .let { TypeToken.of(it.actualTypeArguments[0]).rawType }

    private fun getInputReader(inputMessage: HttpInputMessage): Reader =
      InputStreamReader(
        inputMessage.body,
        inputMessage.headers.contentType?.charset ?: StandardCharsets.UTF_8,
      )
  }
}
