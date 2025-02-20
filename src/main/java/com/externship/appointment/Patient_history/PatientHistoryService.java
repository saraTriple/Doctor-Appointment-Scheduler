package com.externship.appointment.Patient_history;

import com.externship.appointment.Patient_storage.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PatientHistoryService {
    
    @Autowired
    private PatientHistoryRepository patientHistoryRepository;
    
    public PatientHistory save(PatientHistory history) {
        return patientHistoryRepository.save(history);
    }
    
    public List<PatientHistory> getPatientHistory(Patient patient) {
        return patientHistoryRepository.findByPatientOrderByRecordDateDesc(patient);
    }
    
    public PatientHistory getById(Long id) {
        return patientHistoryRepository.findById(id).orElse(null);
    }
}
