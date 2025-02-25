package com.externship.appointment.Doctor_storage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface DoctorRepository extends JpaRepository<Doctor,String> {
    @Query("SELECT d FROM Doctor d WHERE " +
            "(CAST(:name AS string) IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
            "(CAST(:email AS string) IS NULL OR LOWER(d.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%'))) AND " +
            "(CAST(:specialty AS string) IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', CAST(:specialty AS string), '%'))) AND " +
            "(CAST(:degree AS string) IS NULL OR LOWER(d.qualification) LIKE LOWER(CONCAT('%', CAST(:degree AS string), '%')))")
    Page<Doctor> findByFilters(
            @Param("name") String name,
            @Param("email") String email,
            @Param("specialty") String specialty,
            @Param("degree") String degree,
            Pageable pageable
    );


}