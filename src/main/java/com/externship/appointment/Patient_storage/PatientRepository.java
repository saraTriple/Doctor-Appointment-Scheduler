package com.externship.appointment.Patient_storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, String> {
    @Query("SELECT p FROM Patient p WHERE " +
            "(COALESCE(CAST(:email AS string), '') = '' OR LOWER(p.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%'))) AND " +
            "(COALESCE(CAST(:firstName AS string), '') = '' OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', CAST(:firstName AS string), '%'))) AND " +
            "(COALESCE(CAST(:lastName AS string), '') = '' OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', CAST(:lastName AS string), '%'))) AND " +
            "(COALESCE(CAST(:phoneNumber AS string), '') = '' OR p.phoneNumber LIKE CONCAT('%', CAST(:phoneNumber AS string), '%'))")
    Page<Patient> findByFilters(
            @Param("email") String email,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("phoneNumber") String phoneNumber,
            Pageable pageable
    );



    Optional<Patient> findByEmail(String email);
}
