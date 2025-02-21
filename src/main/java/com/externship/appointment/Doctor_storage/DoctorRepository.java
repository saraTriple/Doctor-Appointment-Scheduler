package com.externship.appointment.Doctor_storage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor,String> {
    @Query("SELECT d FROM Doctor d WHERE " +
           "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) ) AND " +
           "(:email IS NULL OR LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:specialty IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialty, '%'))) AND " +
           "(:degree IS NULL OR LOWER(d.degree) LIKE LOWER(CONCAT('%', :degree, '%')))")
    Page<Doctor> findByFilters(
        @Param("name") String name,
        @Param("email") String email,
        @Param("specialty") String specialty,
        @Param("degree") String degree,
        Pageable pageable
    );
}
