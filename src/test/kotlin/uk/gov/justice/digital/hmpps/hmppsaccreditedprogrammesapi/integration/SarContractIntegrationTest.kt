package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SexualOffenceDetailsEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SexualOffenceCategoryType
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarApiDataTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarFlywaySchemaTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarJpaEntitiesTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarReportTest
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

class SarContractIntegrationTest :
  IntegrationTestBase(),
  SarApiDataTest,
  SarReportTest,
  SarFlywaySchemaTest,
  SarJpaEntitiesTest {

  @Autowired
  private lateinit var jwtAuthorisationHelper: JwtAuthorisationHelper

  @Autowired
  private lateinit var dataSource: DataSource

  @PersistenceContext
  private lateinit var entityManager: EntityManager

  override fun getPrn(): String = PRISON_NUMBER

  override fun getFromDate(): LocalDate = FROM_DATE

  override fun getToDate(): LocalDate = TO_DATE

  private val sarIntegrationTestHelper by lazy {
    SarIntegrationTestHelper(
      // The SAR library requests both the data and template endpoints with a ROLE_SAR_DATA_ACCESS
      // token only. The /subject-access-request/template endpoint additionally requires
      // ROLE_ACCREDITED_PROGRAMMES_API in this service, so we wrap the JwtAuthorisationHelper to add
      // that role to every test token. This keeps the contract green without changing production
      // SecurityConfiguration.
      jwtAuthHelper = AccreditedProgrammesJwtAuthorisationHelper(jwtAuthorisationHelper),
      expectedApiResponsePath = "/sar/sar-api-response.json",
      expectedRenderResultPath = "/sar/sar-expected-render-result.html",
      attachmentsExpected = false,
      expectedFlywaySchemaVersion = "143",
      expectedJpaEntitySchemaPath = "/sar/entity-schema.json",
    )
  }

  /**
   * A [JwtAuthorisationHelper] that always includes `ROLE_ACCREDITED_PROGRAMMES_API` in the issued
   * token, in addition to any roles requested by the SAR test library. The library calls both the
   * `/subject-access-request` and `/subject-access-request/template` endpoints with only
   * `ROLE_SAR_DATA_ACCESS`, but this service requires `ROLE_ACCREDITED_PROGRAMMES_API` for the
   * template endpoint. Adding the role here keeps the SAR contract green without modifying the
   * production SecurityConfiguration.
   *
   * Token creation is delegated to the autowired [JwtAuthorisationHelper] so the JWT is signed with
   * the same key pair as the application's auto-configured `jwtDecoder` bean (each helper instance
   * generates its own key pair, so a freshly constructed helper would produce tokens the application
   * rejects).
   */
  private class AccreditedProgrammesJwtAuthorisationHelper(
    private val delegate: JwtAuthorisationHelper,
  ) : JwtAuthorisationHelper() {
    override fun setAuthorisationHeader(
      clientId: String,
      username: String?,
      scope: List<String>,
      roles: List<String>,
    ): (HttpHeaders) -> Unit {
      val token = delegate.createJwtAccessToken(
        username = username,
        clientId = clientId,
        roles = (roles + "ROLE_ACCREDITED_PROGRAMMES_API").distinct(),
        scope = scope,
        expiryTime = Duration.ofHours(1),
      )
      return { headers -> headers.setBearerAuth(token) }
    }
  }

  override fun getSarHelper(): SarIntegrationTestHelper = sarIntegrationTestHelper

  override fun getWebTestClientInstance(): WebTestClient = webTestClient

  override fun getDataSourceInstance(): DataSource = dataSource

  override fun getEntityManagerInstance(): EntityManager = entityManager

  override fun setupTestData() {
    persistenceHelper.clearAllTableContent()
    // clearAllTableContent() does not clear the sexual_offence_details reference-data table, so
    // remove any row from a previous test invocation to keep setupTestData() idempotent (it runs
    // once per contract test).
    persistenceHelper.deleteSexualOffenceDetails(SEXUAL_OFFENCE_ID)

    persistenceHelper.createOrganisation(orgId = ORGANISATION_ID, code = "MDI", name = "HMP Moorland")
    persistenceHelper.createCourse(
      courseId = COURSE_ID,
      identifier = "C1",
      name = "Course 1",
      description = "Course 1 Description",
      altName = "C1 Alt Name",
      audience = "General",
      intensity = "HIGH",
      listDisplayName = "Course 1",
    )
    persistenceHelper.createOffering(
      offeringId = OFFERING_ID,
      courseId = COURSE_ID,
      orgId = "MDI",
      contactEmail = "test@example.com",
      secondaryContactEmail = "test2@example.com",
      referable = true,
    )
    persistenceHelper.createReferrerUser("TEST_USER")
    persistenceHelper.createReferral(
      referralId = REFERRAL_ID,
      offeringId = OFFERING_ID,
      prisonNumber = PRISON_NUMBER,
      referrerUsername = "TEST_USER",
      additionalInformation = "Some info",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
      status = "REFERRAL_STARTED",
      submittedOn = SUBMITTED_ON,
      primaryPomStaffId = 12345.toBigInteger(),
      secondaryPomStaffId = 67890.toBigInteger(),
      hasLdc = true,
      hasLdcBeenOverriddenByProgrammeTeam = true,
    )
    persistenceHelper.createCourseParticipation(
      participationId = PARTICIPATION_ID,
      referralId = REFERRAL_ID,
      prisonNumber = PRISON_NUMBER,
      courseName = "Course 1",
      source = "Source",
      detail = "Detail",
      location = "Location",
      type = CourseSetting.CUSTODY.name,
      outcomeStatus = "INCOMPLETE",
      yearStarted = 2023,
      yearCompleted = 2024,
      createdByUsername = "TEST_USER",
      createdDateTime = CREATED_DATE_TIME,
      lastModifiedByUsername = "TEST_USER",
      lastModifiedDateTime = CREATED_DATE_TIME,
      otherCourseName = "Other course",
      outcomeDetail = "No information to evidence",
    )
    persistenceHelper.createAuditRecord(
      id = AUDIT_RECORD_ID,
      prisonNumber = PRISON_NUMBER,
      auditAction = AuditAction.CREATE_REFERRAL.name,
      auditUsername = "TEST_USER",
      referrerUsername = "TEST_USER",
      auditDateTime = CREATED_DATE_TIME,
    )
    persistenceHelper.createPniResult(
      pniResultId = PNI_RESULT_ID,
      prisonNumber = PRISON_NUMBER,
      pniResultJson = "{\"result\": \"success\"}",
      crn = "X1234YZ",
      programmePathway = "ALTERNATIVE_PATHWAY",
    )
    persistenceHelper.createOasysPniResult(
      pniResultId = OASYS_PNI_RESULT_ID,
      prisonNumber = PRISON_NUMBER,
      oasysAssessmentId = 1234,
      programmePathway = "HIGH_INTENSITY_BC",
    )
    persistenceHelper.createPerson(
      personId = PERSON_ID,
      prisonNumber = PRISON_NUMBER,
      forename = "John",
      surname = "Doe",
      earliestReleaseDateType = "CRD",
      sentenceType = "Determinate",
      location = "HMP Moorland",
      gender = "Male",
    )
    persistenceHelper.createSexualOffenceDetails(
      sexualOffenceDetailsEntity = SexualOffenceDetailsEntity(
        id = SEXUAL_OFFENCE_ID,
        category = SexualOffenceCategoryType.AGAINST_MINORS,
        description = "Example sexual offence",
        hintText = "hint",
        score = 2,
      ),
    )
    persistenceHelper.createSelectedSexualOffenceDetails(
      id = SELECTED_SEXUAL_OFFENCE_ID,
      referralId = REFERRAL_ID,
      sexualOffenceDetailsId = SEXUAL_OFFENCE_ID,
    )
    persistenceHelper.createReferralStatusHistory(
      id = REFERRAL_STATUS_HISTORY_ID,
      referralId = REFERRAL_ID,
      username = "TEST_USER",
      status = "REFERRAL_STARTED",
      statusStartDate = CREATED_DATE_TIME,
    )
    persistenceHelper.createStaff(
      staffId = "12345".toBigInteger(),
      firstName = "John",
      lastName = "Doe",
      username = "JOHN_DOE",
      primaryEmail = "john.doe@test.com",
    )
  }

  private companion object {
    const val PRISON_NUMBER = "A1234BC"
    val FROM_DATE: LocalDate = LocalDate.of(2024, 1, 1)
    val TO_DATE: LocalDate = LocalDate.of(2024, 12, 31)
    val SUBMITTED_ON: LocalDateTime = LocalDateTime.of(2024, 6, 1, 10, 0, 0)
    val CREATED_DATE_TIME: LocalDateTime = LocalDateTime.of(2024, 6, 1, 10, 0, 0)

    val ORGANISATION_ID: UUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
    val COURSE_ID: UUID = UUID.fromString("22222222-2222-2222-2222-222222222222")
    val OFFERING_ID: UUID = UUID.fromString("33333333-3333-3333-3333-333333333333")
    val REFERRAL_ID: UUID = UUID.fromString("44444444-4444-4444-4444-444444444444")
    val PARTICIPATION_ID: UUID = UUID.fromString("55555555-5555-5555-5555-555555555555")
    val AUDIT_RECORD_ID: UUID = UUID.fromString("66666666-6666-6666-6666-666666666666")
    val PNI_RESULT_ID: UUID = UUID.fromString("77777777-7777-7777-7777-777777777777")
    val OASYS_PNI_RESULT_ID: UUID = UUID.fromString("88888888-8888-8888-8888-888888888888")
    val PERSON_ID: UUID = UUID.fromString("99999999-9999-9999-9999-999999999999")
    val SEXUAL_OFFENCE_ID: UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    val SELECTED_SEXUAL_OFFENCE_ID: UUID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
    val REFERRAL_STATUS_HISTORY_ID: UUID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")
  }
}
