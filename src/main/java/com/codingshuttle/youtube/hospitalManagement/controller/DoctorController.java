package com.codingshuttle.youtube.hospitalManagement.controller;

import com.codingshuttle.youtube.hospitalManagement.dto.AppointmentResponseDto;
import com.codingshuttle.youtube.hospitalManagement.dto.DoctorResponseDto;

import com.codingshuttle.youtube.hospitalManagement.entity.User;
import com.codingshuttle.youtube.hospitalManagement.service.AppointmentService;
import com.codingshuttle.youtube.hospitalManagement.service.DoctorService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    /**
     * Retrieves the profile of the currently authenticated doctor.
     * @param user The authenticated user principal.
     * @return The doctor's profile information.
     */
    @GetMapping("/me")
    public ResponseEntity<DoctorResponseDto> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(doctorService.getDoctorById(user.getId()));
    }

    /**
     * Retrieves all appointments for the currently authenticated doctor.
     * @param user The authenticated user principal.
     * @return A list of the doctor's appointments.
     */
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(user.getId()));
    }

    /**
     * Cancels an appointment.
     * @param appointmentId The ID of the appointment to cancel.
     * @return A response entity with no content.
     */
    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long appointmentId) {
        appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }
}

