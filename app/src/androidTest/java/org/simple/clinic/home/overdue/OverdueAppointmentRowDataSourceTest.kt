package org.simple.clinic.home.overdue

import androidx.paging.PositionalDataSource
import androidx.paging.PositionalDataSource.LoadInitialParams
import androidx.paging.PositionalDataSource.LoadRangeParams
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.TestUserClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class OverdueAppointmentRowDataSourceTest {

  @Inject
  lateinit var appDatabase: org.simple.clinic.AppDatabase

  @Inject
  lateinit var clock: TestUserClock

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var bpRepository: BloodPressureRepository

  @Inject
  lateinit var appointmentRepository: AppointmentRepository

  private val currentDate = LocalDate.parse("2018-01-01")

  private val dateFormatter = DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH)

  private val facility = TestData.facility(uuid = UUID.fromString("083979ab-7844-41af-8610-91899baff6bd"))

  private lateinit var dataSource: PositionalDataSource<OverdueAppointmentRow>

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
    clock.setDate(currentDate)

    dataSource = OverdueAppointmentRowDataSource(
        appDatabase = appDatabase,
        userClock = clock,
        dateFormatter = dateFormatter,
        currentFacility = facility,
        source = appDatabase
            .overdueAppointmentDao()
            .overdueAtFacilityDataSource(
                facilityUuid = facility.uuid,
                scheduledBefore = currentDate,
                scheduledAfter = currentDate.minusMonths(1)
            ).create() as PositionalDataSource<OverdueAppointment>
    )
  }

  @After
  fun tearDown() {
    appDatabase.clearAllTables()
  }

  @Test
  fun loading_the_initial_data_set_should_work_correctly() {
    // given
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("139dca32-e495-4951-8ca6-b74d48feb227"),
        patientUuid = UUID.fromString("518bc8ec-0859-4a70-9735-1f6276c6c1b4"),
        facilityUuid = facility.uuid
    )

    createOverdueAppointment(
        appointmentUuid = UUID.fromString("fb25a7b0-ab89-4ffa-a453-0be226332ae4"),
        patientUuid = UUID.fromString("b777be75-a83d-484a-98bb-593522b203e7"),
        facilityUuid = facility.uuid
    )

    // when
    var loaded: List<OverdueAppointmentRow>? = null
    val params = LoadInitialParams(0, 20, 20, false)
    dataSource.loadInitial(params, object : PositionalDataSource.LoadInitialCallback<OverdueAppointmentRow>() {

      override fun onResult(
          data: List<OverdueAppointmentRow>,
          position: Int,
          totalCount: Int
      ) {
        loaded = data
      }

      override fun onResult(data: List<OverdueAppointmentRow>, position: Int) {
      }
    })

    // then
    assertThat(loaded).hasSize(2)
  }

  @Test
  fun loading_a_range_should_work_correctly() {
    // given
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("139dca32-e495-4951-8ca6-b74d48feb227"),
        patientUuid = UUID.fromString("518bc8ec-0859-4a70-9735-1f6276c6c1b4"),
        facilityUuid = facility.uuid
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("fb25a7b0-ab89-4ffa-a453-0be226332ae4"),
        patientUuid = UUID.fromString("b777be75-a83d-484a-98bb-593522b203e7"),
        facilityUuid = facility.uuid
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("333570df-efff-4a6f-9d72-f41e24d29d2c"),
        patientUuid = UUID.fromString("f96e65ce-88de-4d6f-a84f-aaad85d6b7a2"),
        facilityUuid = facility.uuid
    )
    createOverdueAppointment(
        appointmentUuid = UUID.fromString("11070602-4825-4723-b44f-fc37b7abcba3"),
        patientUuid = UUID.fromString("cd357f7f-92d5-4954-9533-803de3b7791e"),
        facilityUuid = facility.uuid
    )
    assertThat(appointmentRepository.overdueAppointmentsCount(currentDate, facility).blockingFirst()).isEqualTo(4)

    // when
    var loaded: List<OverdueAppointmentRow>? = null
    val params = LoadRangeParams(1, 3)
    dataSource.loadRange(params, object : PositionalDataSource.LoadRangeCallback<OverdueAppointmentRow>() {
      override fun onResult(data: List<OverdueAppointmentRow>) {
        loaded = data
      }
    })

    // then
    assertThat(loaded).hasSize(3)
  }

  private fun createOverdueAppointment(
      appointmentUuid: UUID,
      patientUuid: UUID,
      facilityUuid: UUID
  ): Appointment {
    val patientProfile = TestData.patientProfile(
        patientUuid = patientUuid,
        generatePhoneNumber = true
    )
    patientRepository.save(listOf(patientProfile)).blockingAwait()

    val bp = TestData.bloodPressureMeasurement(
        patientUuid = patientUuid,
        facilityUuid = facilityUuid
    )
    bpRepository.save(listOf(bp)).blockingAwait()

    val appointment = TestData.appointment(
        uuid = appointmentUuid,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        scheduledDate = currentDate.minusDays(1),
        status = Appointment.Status.Scheduled,
        cancelReason = null
    )
    appointmentRepository.save(listOf(appointment)).blockingAwait()

    return appointment
  }
}
