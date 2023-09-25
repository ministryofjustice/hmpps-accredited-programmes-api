package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.util

import java.util.Locale
import kotlin.random.Random

private val charPoolUpperCase = ('A'..'Z').toList()
private val charPoolLowerCase = ('a'..'z').toList()
private val charPoolNumbers = ('0'..'9').toList()

private fun randomWithCharPool(charPool: List<Char>, length: Int) = (1..length)
  .map { Random.nextInt(0, charPool.size) }
  .map(charPool::get)
  .joinToString("")

fun randomStringMultiCaseWithNumbers(length: Int) = randomWithCharPool(charPoolUpperCase + charPoolLowerCase + charPoolNumbers, length)

fun randomStringUpperCase(length: Int) = randomWithCharPool(charPoolUpperCase, length)

fun randomStringLowerCase(length: Int) = randomWithCharPool(charPoolLowerCase, length)

fun randomStringUpperCaseWithNumbers(length: Int) = randomWithCharPool(charPoolUpperCase + charPoolNumbers, length)

fun randomSentence(wordCountRange: IntRange = 1..20, wordLengthRange: IntRange = 3..10): String =
  (1..randomInt(wordCountRange.first, wordCountRange.last))
    .joinToString(" ") {
      randomWithCharPool(charPoolLowerCase, randomInt(wordLengthRange.first, wordLengthRange.last))
    }.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun randomEmailAddress() = randomWithCharPool(charPoolLowerCase, 5) + "." + randomWithCharPool(charPoolLowerCase, 8) + "@" + randomWithCharPool(charPoolLowerCase, 6) + ".com"

fun randomInt(min: Int, max: Int) = Random.nextInt(min, max)
