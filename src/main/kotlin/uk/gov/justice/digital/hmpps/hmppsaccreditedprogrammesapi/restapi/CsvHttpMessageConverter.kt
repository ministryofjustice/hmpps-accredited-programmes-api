package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi

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
import java.util.stream.Stream

@Component
class CsvHttpMessageConverter internal constructor() : AbstractGenericHttpMessageConverter<List<Any>>(MediaType("text", "csv")) {
  private val csvMapper = CsvMapper().apply {
    registerModule(KotlinModule.Builder().build())
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    configure(CsvParser.Feature.ALLOW_TRAILING_COMMA, true)
    configure(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE, true)
  }

  override fun canRead(type: Type, contextClass: Class<*>?, mediaType: MediaType?): Boolean = canRead(mediaType) && TypeToken.of(type).rawType.isAssignableFrom(List::class.java)

  override fun supports(clazz: Class<*>): Boolean = getTableType(clazz) != null

  override fun read(type: Type, contextClazz: Class<*>?, inputMessage: HttpInputMessage): List<Any> = getReader(getElementType(type)).readValues<Any>(getInputReader(inputMessage)).readAll()

  override fun readInternal(clazz: Class<out List<Any>>, inputMessage: HttpInputMessage): List<Any> = getReader(getElementType(clazz)).readValues<Any>(getInputReader(inputMessage)).readAll()

  private fun getInputReader(inputMessage: HttpInputMessage): Reader = InputStreamReader(
    inputMessage.body,
    inputMessage.headers.contentType?.charset ?: StandardCharsets.UTF_8,
  )

  private fun getReader(clazz: Class<*>): ObjectReader = csvMapper.reader(getSchema()).forType(clazz)

  private fun getSchema(): CsvSchema = CsvSchema.emptySchema().withHeader()

  private fun getElementType(type: Type): Class<*> =
    getTableType(type)?.let {
      @Suppress("UNCHECKED_CAST")
      val tableType = TypeToken.of(type).getSupertype(it as Class<Any>).type as ParameterizedType
      TypeToken.of(tableType.actualTypeArguments[0]).rawType
    } ?: throw throw IllegalArgumentException()

  private fun getTableType(type: Type): Class<out Any>? = SUPPORTED_SUPER_TYPES.find { TypeToken.of(type).isSubtypeOf(it) }

  override fun writeInternal(instance: List<Any>, type: Type?, outputMessage: HttpOutputMessage) {
  }

  companion object {
    private val SUPPORTED_SUPER_TYPES: List<Class<out Any>> = listOf(Iterable::class.java, Stream::class.java)
  }
}
