package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import java.io.FileNotFoundException
import java.io.InputStream

object CoursesCsvTestData {
  val requestData = listOf(
    CourseRecord(name = "Becoming New Me Plus", description = "Lorem ipsum dolor sit amet, Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", audience = "Sexual offence, Intimate partner violence, Non-intimate partner violence", acronym = "BNM+", comments = "General comment: Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. "),
    CourseRecord(name = "Building Better Relationships", description = "Lorem ipsum dolor sit amet, Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ", audience = "Intimate partner violence ", acronym = "BBR", comments = ""),
    CourseRecord(name = "Healthy Identity Intervention", description = "Lorem ipsum dolor sit amet, Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Extremism offence", acronym = "HI", comments = ""),
    CourseRecord(name = "Healthy Sex Programme", description = "Lorem ipsum dolor sit amet, Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Sexual offence", acronym = "HSP", comments = ""),
    CourseRecord(name = "Horizon", description = "Lorem ipsum dolor sit amet, Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Sexual offence", acronym = "", comments = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."),
    CourseRecord(name = "iHorizon", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. ", audience = "Sexual offence", acronym = "", comments = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."),
    CourseRecord(name = "Identity Matters", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. ", audience = "Gang offence, Extremism offence", acronym = "IM", comments = ""),
    CourseRecord(name = "Kaizen", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Violent offence", acronym = "", comments = ""),
    CourseRecord(name = "Kaizen", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Intimate partner violence", acronym = "", comments = ""),
    CourseRecord(name = "Kaizen", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Sexual offence", acronym = "", comments = ""),
    CourseRecord(name = "Living as New Me (custody)", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Violent offence, Sexual offence, Intimate partner violence ", acronym = "LNM", comments = ""),
    CourseRecord(name = "Living as New Me (community)", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Sexual offence", acronym = "LNM", comments = ""),
    CourseRecord(name = "Motivation and Engagement", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Violent offence, Sexual offence, Intimate partner violence ", acronym = "M&E", comments = ""),
    CourseRecord(name = "New Me MOT", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. ", audience = "Violent offence, Sexual offence, Intimate partner violence ", acronym = "NMM", comments = ""),
    CourseRecord(name = "New Me Strengths", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Violent offence, Sexual offence, Intimate partner violence ", acronym = "NMS", comments = ""),
    CourseRecord(name = "Thinking Skills Programme", description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.", audience = "Violent offence, Intimate partner violence", acronym = "TSP", comments = ""),
  )

  fun csvInputStream(): InputStream = fromResource().openStream()
  private fun fromResource() = this::class.java.getResource("Courses.csv") ?: throw FileNotFoundException("Courses.csv")

  fun csvText() = fromResource().readText()
}
