package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi

import java.io.InputStream
import java.io.InputStreamReader

object LoremIpsum {
  private val lines = InputStreamReader(inputStream()).readLines()
  private fun inputStream(): InputStream = this.javaClass.getResourceAsStream("loremipsum.txt")!!
  fun words(range: IntRange = 1..20) = (0 until range.random()).joinToString(" ") { lines.random() }
}
