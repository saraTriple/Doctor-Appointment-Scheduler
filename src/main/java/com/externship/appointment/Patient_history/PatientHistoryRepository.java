package com.externship.appointment.Patient_history;

import com.externship.appointment.Patient_storage.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientHistoryRepository extends JpaRepository<PatientHistory, Long> {
    List<PatientHistory> findByPatientOrderByRecordDateDesc(Patient patient);
}
