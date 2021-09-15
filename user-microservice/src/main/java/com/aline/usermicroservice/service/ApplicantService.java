package com.aline.usermicroservice.service;

import com.aline.core.model.Applicant;
import com.aline.core.repository.ApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicantService {

    private final ApplicantRepository repository;

    // Save applicant
    public void saveApplicant(Applicant applicant) {
        repository.save(applicant);
    }

}
