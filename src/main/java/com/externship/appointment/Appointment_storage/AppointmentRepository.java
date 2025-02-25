package com.externship.appointment.Appointment_storage;

import com.externship.appointment.Doctor_storage.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing appointments.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {


    @Query("select a from Appointment a where a.id = :id")
    List<Appointment> findByIdList(Long id);
    /**
     * Finds an appointment by date, time, and doctor's email.
     *
     * @param date        the date of the appointment
     * @param time        the time of the appointment
     * @param doctorEmail the email of the doctor
     * @return the appointment if found, otherwise an empty optional
     */
    List<Appointment> findByDateAndTimeAndDoctor_Email(LocalDate date, LocalTime time, String doctorEmail);

    /**
     * Finds all appointments for a patient by their email.
     *
     * @param email the email of the patient
     * @return a list of appointments for the patient
     */
    List<Appointment> findByPerson_Email(String email);

    /**
     * Finds all appointments for a doctor by their email and date.
     *
     * @param date       the date of the appointments
     * @param email      the email of the doctor
     * @param pageable   the pagination information
     * @return a page of appointments for the doctor on the specified date
     */
    Page<Appointment> findByDateAndDoctor_Email(LocalDate date, String email, Pageable pageable);

    Page<Appointment> findByDoctor_Email(String email, Pageable pageable);

    Page<Appointment> findByAppointmentStatus_Status(String status, Pageable pageable);

    List<Appointment> findByAppointmentStatus_Status(String status);


    List<Appointment> findByDoctor_Email(String email);

    /**
     * Finds all appointments for a doctor by their email and date greater than or equal to the specified date.
     *
     * @param email      the email of the doctor
     * @param date       the date from which to retrieve appointments
     * @param pageable   the pagination information
     * @return a page of appointments for the doctor on or after the specified date
     */
    Page<Appointment> findByDoctor_EmailAndDateGreaterThanEqual(String email, LocalDate date, Pageable pageable);

    /**
     * Finds all appointments on or after the specified date.
     *
     * @param date      the date from which to retrieve appointments
     * @param pageable  the pagination information
     * @return a page of appointments on or after the specified date
     */
    Page<Appointment> findByDateGreaterThanEqual(LocalDate date, Pageable pageable);

    /**
     * Finds all appointments on the specified date.
     *
     * @param date      the date for which to retrieve appointments
     * @param pageable  the pagination information
     * @return a page of appointments on the specified date
     */
    Page<Appointment> findByDate(LocalDate date, Pageable pageable);

    Page<Appointment> findByTime(LocalTime time, Pageable pageable);

    /**
     * Finds all available appointments on or after the specified date.
     *
     * @param date      the date from which to retrieve available appointments
     * @param pageable  the pagination information
     * @return a page of available appointments on or after the specified date
     */
    Page<Appointment> findByDateGreaterThanEqualAndPerson_EmailIsNull(LocalDate date, Pageable pageable);

    /**
     * Finds all available appointments on the specified date.
     *
     * @param date      the date for which to retrieve available appointments
     * @param pageable  the pagination information
     * @return a page of available appointments on the specified date
     */
    Page<Appointment> findByDateAndPerson_EmailIsNull(LocalDate date, Pageable pageable);

    /**
     * Finds all available appointments for a doctor by their email and date greater than or equal to the specified date.
     *
     * @param email      the email of the doctor
     * @param date       the date from which to retrieve available appointments
     * @param pageable   the pagination information
     * @return a page of available appointments for the doctor on or after the specified date
     */
    Page<Appointment> findByDoctor_EmailAndDateGreaterThanEqualAndPerson_EmailIsNull(
            String email, LocalDate date, Pageable pageable);

    /**
     * Finds all available appointments for a doctor by their email and date.
     *
     * @param date       the date for which to retrieve available appointments
     * @param email      the email of the doctor
     * @param pageable   the pagination information
     * @return a page of available appointments for the doctor on the specified date
     */
    Page<Appointment> findByDateAndDoctor_EmailAndPerson_EmailIsNull(
            LocalDate date, String email, Pageable pageable);

    Page<Appointment> findByTimeAndDoctor_EmailAndPerson_EmailIsNull(
            LocalTime time, String email, Pageable pageable);

    Page<Appointment> findByDateAndTimeAndDoctor_EmailAndPerson_EmailIsNull(
            LocalDate date,LocalTime time, String email, Pageable pageable);

    List<Appointment> findByDateBetween(LocalDate startDate, LocalDate endDate);

    List<Appointment> findByDoctorAndDateBetween(Doctor doctor, LocalDate startDate, LocalDate endDate);

    List<Appointment> findByAppointmentStatus_StatusAndDateBetween(String status, LocalDate startDate, LocalDate endDate);

    /**
     * Finds appointments based on the specified filters.
     *
     * @param currentDate       the date from which to retrieve appointments
     * @param specialty  the specialty of the doctor (optional)
     * @param doctorName the name of the doctor (optional)
     * @param pageable   the pagination information
     * @return a page of appointments matching the specified filters
     */
    @Query("SELECT a FROM Appointment a WHERE " +
            "(CAST(:currentDate AS date) IS NULL OR a.date >= CAST(:currentDate AS date)) AND " +
            "(CAST(:filteredDate AS date) IS NULL OR a.date = CAST(:filteredDate AS date)) AND " +
            "a.person IS NULL AND " +
            "(CAST(:specialty AS string) IS NULL OR LOWER(a.doctor.specialization) LIKE LOWER(CONCAT('%', CAST(:specialty AS string), '%'))) AND " +
            "(CAST(:doctorName AS string) IS NULL OR LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', CAST(:doctorName AS string), '%')))")
    Page<Appointment> findByFilters(
            @Param("currentDate") LocalDate currentDate,
            @Param("filteredDate") LocalDate filteredDate,
            @Param("specialty") String specialty,
            @Param("doctorName") String doctorName,
            Pageable pageable);



    Page<Appointment> findByDateAndTime(LocalDate filterDate, LocalTime filterTime, Pageable pageable);
}
