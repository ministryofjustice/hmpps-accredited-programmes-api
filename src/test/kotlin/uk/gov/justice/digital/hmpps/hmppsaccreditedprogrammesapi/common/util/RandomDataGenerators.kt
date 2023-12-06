package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util

import kotlin.random.Random

private val upperCase = ('A'..'Z').toList()
private val lowerCase = ('a'..'z').toList()
private val digits = ('0'..'9').toList()

fun randomAlphanumericString(length: Int) = (upperCase + lowerCase + digits)(length).asString()

fun randomUppercaseString(length: Int) = upperCase(length).asString()

fun randomLowercaseString(length: Int) = lowerCase(length).asString()

fun randomUppercaseAlphanumericString(length: Int) = (upperCase + digits)(length).asString()

fun randomSentence(wordRange: IntRange = 1..20, wordLength: IntRange = 3..10): String =
  (sequenceOf(capitalisedWord(wordLength)) + generateSequence { word(wordLength) })
    .take(wordRange.random())
    .reduce { left, right -> left + space() + right }
    .asString()

fun randomInt(min: Int, max: Int) = Random.nextInt(min, max)

private fun space() = sequenceOf(' ')

fun word(length: IntRange) = lowerCase(length.random())

fun capitalisedWord(length: IntRange) = upperCase(1) + lowerCase((length).random() - 1)

fun randomEmailAddress() = (lowerCase(5) + ".".asSequence() + lowerCase(8) + "@".asSequence() + lowerCase(6) + ".com".asSequence()).asString()

fun randomPrisonNumber(): String = (upperCase(1) + digits(4) + upperCase(2)).asString()
fun randomPrisonName(): String = (upperCase(1) + digits(4) + upperCase(2)).asString()
fun randomReferrerId(): String = (upperCase(3) + digits(4) + upperCase(2)).asString()

fun Sequence<Char>.asString() = fold(StringBuilder(), StringBuilder::append).toString()

private operator fun Collection<Char>.invoke(n: Int) = generateSequence { random() }.take(n)
