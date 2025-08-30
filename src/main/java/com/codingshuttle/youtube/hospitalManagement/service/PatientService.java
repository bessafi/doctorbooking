package com.codingshuttle.youtube.hospitalManagement.service;

import com.codingshuttle.youtube.hospitalManagement.dto.PatientResponseDto;
import com.codingshuttle.youtube.hospitalManagement.entity.Patient;
import com.codingshuttle.youtube.hospitalManagement.repository.PatientRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.RoleRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.codingshuttle.youtube.hospitalManagement.dto.FindOrCreatePatientRequestDto;
import com.codingshuttle.youtube.hospitalManagement.entity.Role;
import com.codingshuttle.youtube.hospitalManagement.entity.User;
import com.codingshuttle.youtube.hospitalManagement.entity.type.AuthProviderType;
import java.util.HashSet;
import java.util.Set;


import java.util.List;
import java.util.stream.Collectors;
/* 
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public PatientResponseDto getPatientById(Long patientId) {
        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new EntityNotFoundException("Patient Not " +
                "Found with id: " + patientId));
        return modelMapper.map(patient, PatientResponseDto.class);
    }

    public List<PatientResponseDto> getAllPatients(Integer pageNumber, Integer pageSize) {
        return patientRepository.findAllPatients(PageRequest.of(pageNumber, pageSize))
                .stream()
                .map(patient -> modelMapper.map(patient, PatientResponseDto.class))
                .collect(Collectors.toList());
    }
}



package com.clinic.doctorappointment.service;

import com.clinic.doctorappointment.dto.PatientResponseDto;
import com.clinic.doctorappointment.entity.Patient;
import com.clinic.doctorappointment.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
*/
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    public PatientResponseDto getPatientById(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + patientId));
        return modelMapper.map(patient, PatientResponseDto.class);
    }

    /**
     * Finds a patient by email. If the patient does not exist, a new User and Patient
     * record are created. This is designed to be called by Botpress.
     *
     * @param requestDto DTO containing the patient's name and email.
     * @return The existing or newly created Patient entity.
     */
    @Transactional
    public Patient findOrCreatePatient(FindOrCreatePatientRequestDto requestDto) {
        // Try to find an existing patient by email
        return patientRepository.findByEmail(requestDto.getEmail())
                .orElseGet(() -> {
                    // If patient doesn't exist, create a new one
                    
                    // First, check if a user with this email already exists to avoid conflicts
                    if (userRepository.findByUsername(requestDto.getEmail()).isPresent()) {
                        throw new IllegalStateException("A user with this email already exists but is not a patient.");
                    }

                    // Create the base User entity
                    User newUser = new User();
                    newUser.setUsername(requestDto.getEmail());
                    newUser.setProviderType(AuthProviderType.EMAIL); // Default provider for bot-created users

                    // Assign the ROLE_PATIENT
                    Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                            .orElseGet(() -> roleRepository.save(new Role("ROLE_PATIENT")));
                    Set<Role> roles = new HashSet<>();
                    roles.add(patientRole);
                    newUser.setRoles(roles);

                    // Create the Patient entity and link it to the User
                    Patient newPatient = Patient.builder()
                            .user(newUser)
                            .name(requestDto.getName())
                            .email(requestDto.getEmail())
                            .build();

                    // Save the new patient (which will cascade and save the user as well)
                    return patientRepository.save(newPatient);
                });
    }
}

