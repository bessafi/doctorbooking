package com.codingshuttle.youtube.hospitalManagement.controller;

import com.codingshuttle.youtube.hospitalManagement.dto.*; // <-- THIS IMPORT IS NEEDED
import com.codingshuttle.youtube.hospitalManagement.entity.Patient;
import com.codingshuttle.youtube.hospitalManagement.service.AppointmentService;
import com.codingshuttle.youtube.hospitalManagement.service.AvailabilityService;
import com.codingshuttle.youtube.hospitalManagement.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/botpress")
@RequiredArgsConstructor
@Slf4j
public class BotpressController {

    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;

    @GetMapping("/availability")
public ResponseEntity<List<AvailabilityResponseDto.TimeSlot>> getAvailability(
        @RequestParam Long doctorId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AvailabilityResponseDto responseDto = availabilityService.getAvailableSlots(doctorId, date, 30);
        return ResponseEntity.ok(responseDto.getAvailableSlots());
    
}

    @PostMapping("/patients/find-or-create")
    public ResponseEntity<FindOrCreatePatientResponseDto> findOrCreatePatient(@Valid @RequestBody FindOrCreatePatientRequestDto requestDto) {
        Patient patient = patientService.findOrCreatePatient(requestDto);
        return ResponseEntity.ok(new FindOrCreatePatientResponseDto(patient.getId()));
    }

    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponseDto> createAppointment(@Valid @RequestBody CreateAppointmentRequestDto requestDto) {
        AppointmentResponseDto createdAppointment = appointmentService.createAppointment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAppointment);
    }
}
