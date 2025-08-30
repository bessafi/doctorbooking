package com.codingshuttle.youtube.hospitalManagement.service;

import com.codingshuttle.youtube.hospitalManagement.dto.AppointmentResponseDto;
import com.codingshuttle.youtube.hospitalManagement.dto.CreateAppointmentRequestDto;
import com.codingshuttle.youtube.hospitalManagement.entity.Appointment;
import com.codingshuttle.youtube.hospitalManagement.entity.Doctor;
import com.codingshuttle.youtube.hospitalManagement.entity.DoctorCalendar;
import com.codingshuttle.youtube.hospitalManagement.entity.Patient;
import com.codingshuttle.youtube.hospitalManagement.repository.AppointmentRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.DoctorCalendarRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.DoctorRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.PatientRepository;
import com.google.api.services.calendar.model.Event;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorCalendarRepository doctorCalendarRepository;
    private final GoogleCalendarService googleCalendarService;
    private final ModelMapper modelMapper;

    @Transactional
    public AppointmentResponseDto createAppointment(CreateAppointmentRequestDto requestDto) {
        Doctor doctor = doctorRepository.findById(requestDto.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));
        Patient patient = patientRepository.findById(requestDto.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentTime(requestDto.getAppointmentTime())
                .reason(requestDto.getReason())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Sync with Google Calendar if connected
        doctorCalendarRepository.findByDoctorId(doctor.getId()).ifPresent(calendar -> {
            try {
                Event event = googleCalendarService.createEvent(savedAppointment, calendar);
                savedAppointment.setGoogleEventId(event.getId());
                appointmentRepository.save(savedAppointment);
                log.info("Successfully created Google Calendar event with ID: {}", event.getId());
            } catch (IOException | GeneralSecurityException e) {
                log.error("Failed to create Google Calendar event for appointment ID: {}. The appointment is saved locally.", savedAppointment.getId(), e);
            }
        });

        return modelMapper.map(savedAppointment, AppointmentResponseDto.class);
    }

    /**
     * Gets a synchronized list of appointments from both the local database and the doctor's Google Calendar.
     *
     * @param doctorId The ID of the doctor.
     * @return A merged and de-duplicated list of appointments.
     */
    public List<AppointmentResponseDto> getAppointmentsForDoctor(Long doctorId) {
        // 1. Fetch appointments from the local database
        List<Appointment> dbAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeAfter(doctorId, LocalDateTime.now());
        
        List<AppointmentResponseDto> appointmentDtos = dbAppointments.stream()
                .map(app -> modelMapper.map(app, AppointmentResponseDto.class))
                .collect(Collectors.toList());

        // Create a set of event IDs that are already in our database to avoid duplicates
        Set<String> existingEventIds = dbAppointments.stream()
                .map(Appointment::getGoogleEventId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 2. Fetch events from Google Calendar and merge
        doctorCalendarRepository.findByDoctorId(doctorId).ifPresent(calendar -> {
            try {
                // Fetch events from today onwards
                List<Event> googleEvents = googleCalendarService.getEvents(calendar, LocalDateTime.now());

                List<AppointmentResponseDto> manualEntries = googleEvents.stream()
                        // 3. Filter out events that are already in our database
                        .filter(event -> !existingEventIds.contains(event.getId()))
                        // 4. Map the remaining Google Events to our DTO
                        .map(event -> {
                            AppointmentResponseDto dto = new AppointmentResponseDto();
                            dto.setGoogleEventId(event.getId());
                            dto.setReason(event.getSummary()); // Use summary as the reason
                            if (event.getStart().getDateTime() != null) {
                                dto.setAppointmentTime(
                                    LocalDateTime.ofInstant(
                                        java.time.Instant.ofEpochMilli(event.getStart().getDateTime().getValue()),
                                        ZoneId.systemDefault()
                                    )
                                );
                            }
                            // Note: We don't have patient info for manual entries, so it will be null.
                            return dto;
                        })
                        .collect(Collectors.toList());

                // 5. Add the manual entries to our main list
                appointmentDtos.addAll(manualEntries);

            } catch (IOException | GeneralSecurityException e) {
                log.error("Could not sync with Google Calendar for doctor ID: {}", doctorId, e);
                // Return only DB appointments if Google sync fails
            }
        });

        return appointmentDtos;
    }


    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        // If it's linked to a Google Calendar event, delete that first
        if (appointment.getGoogleEventId() != null && !appointment.getGoogleEventId().isBlank()) {
            doctorCalendarRepository.findByDoctorId(appointment.getDoctor().getId()).ifPresent(calendar -> {
                try {
                    googleCalendarService.deleteEvent(appointment.getGoogleEventId(), calendar);
                    log.info("Successfully deleted Google Calendar event with ID: {}", appointment.getGoogleEventId());
                } catch (IOException | GeneralSecurityException e) {
                    log.error("Failed to delete Google Calendar event for appointment ID: {}. The local record will still be deleted.", appointment.getId(), e);
                }
            });
        }
        appointmentRepository.delete(appointment);
    }
}
