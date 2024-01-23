package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
@Transactional
class PersistenceHelper {

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  fun clearAllTableContent() {
    entityManager.createNativeQuery("DELETE FROM course_participation").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM referral").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM offering").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM course_audience").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM course").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM audience").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM referrer_user").executeUpdate()
  }

  fun createAudience(audienceId: UUID, audienceValue: String) {
    entityManager.createNativeQuery("INSERT INTO audience (audience_id, audience_value) VALUES (:id, :value)")
      .setParameter("id", audienceId)
      .setParameter("value", audienceValue)
      .executeUpdate()
  }

  fun createCourse(courseId: UUID, identifier: String, name: String, description: String, altName: String, referable: Boolean) {
    entityManager.createNativeQuery("INSERT INTO course (course_id, identifier, name, description, alternate_name, referable) VALUES (:id, :identifier, :name, :description, :altName, :referable)")
      .setParameter("id", courseId)
      .setParameter("identifier", identifier)
      .setParameter("name", name)
      .setParameter("description", description)
      .setParameter("altName", altName)
      .setParameter("referable", referable)
      .executeUpdate()
  }

  fun createCourseAudience(courseId: UUID, audienceId: UUID) {
    entityManager.createNativeQuery("INSERT INTO course_audience (course_id, audience_id) VALUES (:courseId, :audienceId)")
      .setParameter("courseId", courseId)
      .setParameter("audienceId", audienceId)
      .executeUpdate()
  }

  fun createOffering(offeringId: UUID, courseId: UUID, orgId: String, contactEmail: String, secondaryContactEmail: String) {
    entityManager.createNativeQuery("INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email) VALUES (:id, :courseId, :orgId, :contactEmail, :secondaryContactEmail)")
      .setParameter("id", offeringId)
      .setParameter("courseId", courseId)
      .setParameter("orgId", orgId)
      .setParameter("contactEmail", contactEmail)
      .setParameter("secondaryContactEmail", secondaryContactEmail)
      .executeUpdate()
  }

  fun createReferrerUser(username: String) {
    entityManager.createNativeQuery("INSERT INTO referrer_user (referrer_username) VALUES (:username)")
      .setParameter("username", username)
      .executeUpdate()
  }

  fun createReferral(referralId: UUID, offeringId: UUID, prisonNumber: String, referrerUsername: String, additionalInformation: String, oasysConfirmed: Boolean, hasReviewedProgrammeHistory: Boolean, status: String, submittedOn: LocalDateTime?) {
    entityManager.createNativeQuery("INSERT INTO referral (referral_id, offering_id, prison_number, referrer_username, additional_information, oasys_confirmed, has_reviewed_programme_history, status, submitted_on) VALUES (:id, :offeringId, :prisonNumber, :referrerUsername, :additionalInformation, :oasysConfirmed, :hasReviewedProgrammeHistory, :status, :submittedOn)")
      .setParameter("id", referralId)
      .setParameter("offeringId", offeringId)
      .setParameter("prisonNumber", prisonNumber)
      .setParameter("referrerUsername", referrerUsername)
      .setParameter("additionalInformation", additionalInformation)
      .setParameter("oasysConfirmed", oasysConfirmed)
      .setParameter("hasReviewedProgrammeHistory", hasReviewedProgrammeHistory)
      .setParameter("status", status)
      .setParameter("submittedOn", submittedOn)
      .executeUpdate()
  }

  fun createParticipation(participationId: UUID, prisonNumber: String, courseName: String, source: String, detail: String, location: String, type: String, outcomeStatus: String, yearStarted: Int?, yearCompleted: Int?, createdByUsername: String, createdDateTime: LocalDateTime, lastModifiedByUsername: String?, lastModifiedDateTime: LocalDateTime?) {
    entityManager.createNativeQuery("INSERT INTO course_participation (course_participation_id, prison_number, course_name, source, detail, location, type, outcome_status, year_started, year_completed, created_by_username, created_date_time, last_modified_by_username, last_modified_date_time) VALUES (:id, :prisonNumber, :courseName, :source, :detail, :location, :type, :outcomeStatus, :yearStarted, :yearCompleted, :createdByUsername, :createdDateTime, :lastModifiedByUsername, :lastModifiedDateTime)")
      .setParameter("id", participationId)
      .setParameter("prisonNumber", prisonNumber)
      .setParameter("courseName", courseName)
      .setParameter("source", source)
      .setParameter("detail", detail)
      .setParameter("location", location)
      .setParameter("type", type)
      .setParameter("outcomeStatus", outcomeStatus)
      .setParameter("yearStarted", yearStarted)
      .setParameter("yearCompleted", yearCompleted)
      .setParameter("createdByUsername", createdByUsername)
      .setParameter("createdDateTime", createdDateTime)
      .setParameter("lastModifiedByUsername", lastModifiedByUsername)
      .setParameter("lastModifiedDateTime", lastModifiedDateTime)
      .executeUpdate()
  }
}
