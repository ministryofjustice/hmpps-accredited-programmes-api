package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.time.LocalDateTime
import java.util.UUID

@Component
@Transactional
class PersistenceHelper {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  fun clearAllTableContent() {
    entityManager.createNativeQuery("DELETE FROM referral_status_history").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM prerequisite").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM course_participation").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM pni_result").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM staff").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM referral").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM offering").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM course_variant").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM course").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM referrer_user").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM audit_record").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM organisation").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM enabled_organisation").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM audience").executeUpdate()
  }

  fun createCourse(courseId: UUID, identifier: String, name: String, description: String, altName: String, audience: String, withdrawn: Boolean = false, audienceColour: String = "light-blue", displayOnProgrammeDirectory: Boolean = true) {
    entityManager.createNativeQuery("INSERT INTO course (course_id, identifier, name, description, alternate_name, audience, withdrawn, audience_colour, display_on_programme_directory) VALUES (:id, :identifier, :name, :description, :altName, :audience, :withdrawn, :audienceColour, :display_on_programme_directory)")
      .setParameter("id", courseId)
      .setParameter("identifier", identifier)
      .setParameter("name", name)
      .setParameter("description", description)
      .setParameter("altName", altName)
      .setParameter("audience", audience)
      .setParameter("withdrawn", withdrawn)
      .setParameter("audienceColour", audienceColour)
      .setParameter("display_on_programme_directory", displayOnProgrammeDirectory)
      .executeUpdate()
  }

  fun createPrerequisite(courseId: UUID, name: String, description: String) {
    entityManager.createNativeQuery("INSERT INTO prerequisite (course_id, name, description) VALUES (:id, :name, :description)")
      .setParameter("id", courseId)
      .setParameter("name", name)
      .setParameter("description", description)
      .executeUpdate()
  }

  fun createOffering(offeringId: UUID, courseId: UUID, orgId: String, contactEmail: String, secondaryContactEmail: String, referable: Boolean, withdrawn: Boolean = false) {
    entityManager.createNativeQuery("INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email, referable, withdrawn) VALUES (:id, :courseId, :orgId, :contactEmail, :secondaryContactEmail, :referable, :withdrawn)")
      .setParameter("id", offeringId)
      .setParameter("courseId", courseId)
      .setParameter("orgId", orgId)
      .setParameter("contactEmail", contactEmail)
      .setParameter("secondaryContactEmail", secondaryContactEmail)
      .setParameter("referable", referable)
      .setParameter("withdrawn", withdrawn)
      .executeUpdate()
  }

  fun createOrganisation(orgId: UUID = UUID.randomUUID(), code: String, name: String, gender: String = "MALE") {
    entityManager.createNativeQuery("INSERT INTO organisation (organisation_id, code, name, gender) VALUES (:organisation_id, :code, :name, :gender)")
      .setParameter("organisation_id", orgId)
      .setParameter("code", code)
      .setParameter("name", name)
      .setParameter("gender", gender)
      .executeUpdate()
  }

  fun createEnabledOrganisation(code: String, description: String) {
    entityManager.createNativeQuery("INSERT INTO enabled_organisation (code, description) VALUES (:code, :description)")
      .setParameter("code", code)
      .setParameter("description", description)
      .executeUpdate()
  }

  fun createReferrerUser(username: String) {
    entityManager.createNativeQuery("INSERT INTO referrer_user (referrer_username) VALUES (:username)")
      .setParameter("username", username)
      .executeUpdate()
  }

  fun createReferral(referralId: UUID, offeringId: UUID, prisonNumber: String, referrerUsername: String, additionalInformation: String, oasysConfirmed: Boolean, hasReviewedProgrammeHistory: Boolean, status: String, submittedOn: LocalDateTime?, primaryPomStaffId: BigInteger = "1".toBigInteger(), secondaryPomStaffId: BigInteger = "2".toBigInteger()) {
    entityManager.createNativeQuery("INSERT INTO referral (referral_id, offering_id, prison_number, referrer_username, additional_information, oasys_confirmed, has_reviewed_programme_history, status, submitted_on, primary_pom_staff_id, secondary_pom_staff_id) VALUES (:id, :offeringId, :prisonNumber, :referrerUsername, :additionalInformation, :oasysConfirmed, :hasReviewedProgrammeHistory, :status, :submittedOn, :primaryPomStaffId, :secondaryPomStaffId)")
      .setParameter("id", referralId)
      .setParameter("offeringId", offeringId)
      .setParameter("prisonNumber", prisonNumber)
      .setParameter("referrerUsername", referrerUsername)
      .setParameter("additionalInformation", additionalInformation)
      .setParameter("oasysConfirmed", oasysConfirmed)
      .setParameter("hasReviewedProgrammeHistory", hasReviewedProgrammeHistory)
      .setParameter("status", status)
      .setParameter("submittedOn", submittedOn)
      .setParameter("primaryPomStaffId", primaryPomStaffId)
      .setParameter("secondaryPomStaffId", secondaryPomStaffId)
      .executeUpdate()
  }

  fun createCourseParticipation(participationId: UUID, referralId: UUID?, prisonNumber: String, courseName: String, source: String, detail: String, location: String, type: String, outcomeStatus: String, yearStarted: Int?, yearCompleted: Int?, isDraft: Boolean? = false, createdByUsername: String, createdDateTime: LocalDateTime, lastModifiedByUsername: String?, lastModifiedDateTime: LocalDateTime?) {
    entityManager.createNativeQuery("INSERT INTO course_participation (course_participation_id, referral_id, prison_number, course_name, source, detail, location, type, outcome_status, year_started, year_completed, is_draft, created_by_username, created_date_time, last_modified_by_username, last_modified_date_time) VALUES (:id, :referralId, :prisonNumber, :courseName, :source, :detail, :location, :type, :outcomeStatus, :yearStarted, :yearCompleted, :isDraft, :createdByUsername, :createdDateTime, :lastModifiedByUsername, :lastModifiedDateTime)")
      .setParameter("id", participationId)
      .setParameter("referralId", referralId)
      .setParameter("prisonNumber", prisonNumber)
      .setParameter("courseName", courseName)
      .setParameter("source", source)
      .setParameter("detail", detail)
      .setParameter("location", location)
      .setParameter("type", type)
      .setParameter("outcomeStatus", outcomeStatus)
      .setParameter("yearStarted", yearStarted)
      .setParameter("yearCompleted", yearCompleted)
      .setParameter("isDraft", isDraft)
      .setParameter("createdByUsername", createdByUsername)
      .setParameter("createdDateTime", createdDateTime)
      .setParameter("lastModifiedByUsername", lastModifiedByUsername)
      .setParameter("lastModifiedDateTime", lastModifiedDateTime)
      .executeUpdate()
  }

  fun createAudience(id: UUID = UUID.randomUUID(), name: String, colour: String) {
    entityManager.createNativeQuery("INSERT INTO audience (audience_id, name, colour) VALUES (:id, :name, :colour)")
      .setParameter("id", id)
      .setParameter("name", name)
      .setParameter("colour", colour)
      .executeUpdate()
  }

  fun createCourseVariant(id: UUID = UUID.randomUUID(), courseId: UUID, variantCourseId: UUID = UUID.randomUUID()) {
    entityManager.createNativeQuery("INSERT INTO course_variant (id, course_id, variant_course_id) VALUES (:id, :courseId, :variantCourseId)")
      .setParameter("id", id)
      .setParameter("courseId", courseId)
      .setParameter("variantCourseId", variantCourseId)
      .executeUpdate()
  }

  fun createStaff(
    id: UUID = UUID.randomUUID(),
    staffId: BigInteger,
    firstName: String,
    lastName: String,
    username: String,
    primaryEmail: String,
    accountType: String = "GENERAL",
  ) {
    entityManager.createNativeQuery("INSERT INTO staff (id, staff_id, first_name, last_name, username, primary_email, account_type) VALUES (:id, :staffId, :firstName, :lastName, :username, :primaryEmail, :accountType)")
      .setParameter("id", id)
      .setParameter("staffId", staffId)
      .setParameter("firstName", firstName)
      .setParameter("lastName", lastName)
      .setParameter("username", username)
      .setParameter("primaryEmail", primaryEmail)
      .setParameter("accountType", accountType)
      .executeUpdate()
  }
}
