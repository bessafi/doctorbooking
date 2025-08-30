package com.codingshuttle.youtube.hospitalManagement.service;

import com.codingshuttle.youtube.hospitalManagement.dto.AvailabilityResponseDto;
import com.codingshuttle.youtube.hospitalManagement.entity.Appointment;
import com.codingshuttle.youtube.hospitalManagement.entity.BlockedTimeSlot;
import com.codingshuttle.youtube.hospitalManagement.entity.DoctorCalendar;
import com.codingshuttle.youtube.hospitalManagement.entity.WorkingHours;
import com.codingshuttle.youtube.hospitalManagement.repository.AppointmentRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.BlockedTimeSlotRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.DoctorCalendarRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.WorkingHoursRepository;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final AppointmentRepository appointmentRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final BlockedTimeSlotRepository blockedTimeSlotRepository;
    private final DoctorCalendarRepository doctorCalendarRepository;
    private final GoogleCalendarService googleCalendarService;

    public AvailabilityResponseDto getAvailableSlots(Long doctorId, LocalDate date, int durationMinutes) {
        // Get doctor's working hours for the requested day
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<WorkingHours> workingHours = workingHoursRepository.findByDoctorId(doctorId)
                .stream()
                .filter(wh -> wh.getDayOfWeek() == dayOfWeek && wh.isAvailable())
                .collect(Collectors.toList());

        if (workingHours.isEmpty()) {
            return new AvailabilityResponseDto(new ArrayList<>());
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        // Get existing appointments and blocked slots from the local database
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, dayStart, dayEnd);
        List<BlockedTimeSlot> blockedSlots = blockedTimeSlotRepository.findByDoctorIdAndStartTimeBetween(doctorId, dayStart, dayEnd);

        // Get busy slots from Google Calendar
        List<Event> googleBusyEvents = new ArrayList<>();
        doctorCalendarRepository.findByDoctorId(doctorId).ifPresent(calendar -> {
            try {
                googleBusyEvents.addAll(googleCalendarService.getEvents(calendar, dayStart));
            } catch (IOException | GeneralSecurityException e) {
                log.error("Error fetching Google Calendar busy slots for doctorId {}: {}", doctorId, e.getMessage());
            }
        });

        // Calculate available time slots
        List<AvailabilityResponseDto.TimeSlot> availableSlots = new ArrayList<>();
        for (WorkingHours wh : workingHours) {
            LocalDateTime currentSlotStart = date.atTime(wh.getStartTime());
            LocalDateTime workingEnd = date.atTime(wh.getEndTime());

            while (!currentSlotStart.plusMinutes(durationMinutes).isAfter(workingEnd)) {
                LocalDateTime currentSlotEnd = currentSlotStart.plusMinutes(durationMinutes);

                if (isSlotAvailable(currentSlotStart, currentSlotEnd, appointments, blockedSlots, googleBusyEvents)) {
                    availableSlots.add(new AvailabilityResponseDto.TimeSlot(currentSlotStart, currentSlotEnd));
                }
                // Move to the next potential slot (e.g., every 15 minutes)
                currentSlotStart = currentSlotStart.plusMinutes(15);
            }
        }
        return new AvailabilityResponseDto(availableSlots);
    }

    private boolean isSlotAvailable(LocalDateTime start, LocalDateTime end,
                                    List<Appointment> appointments,
                                    List<BlockedTimeSlot> blockedSlots,
                                    List<Event> googleBusyEvents) {

        // Check against existing appointments
        for (Appointment appointment : appointments) {
            LocalDateTime appointmentStart = appointment.getAppointmentTime();
            LocalDateTime appointmentEnd = appointmentStart.plusMinutes(30); // Assuming 30-min appointments
            if (isOverlapping(start, end, appointmentStart, appointmentEnd)) {
                return false;
            }
        }

        // Check against manually blocked time slots
        for (BlockedTimeSlot blockedSlot : blockedSlots) {
            if (isOverlapping(start, end, blockedSlot.getStartTime(), blockedSlot.getEndTime())) {
                return false;
            }
        }

        // Check against Google Calendar busy events
        for (Event event : googleBusyEvents) {
            if (event.getStart() == null || event.getStart().getDateTime() == null) continue;
            
            LocalDateTime eventStart = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(event.getStart().getDateTime().getValue()), ZoneId.systemDefault());
            LocalDateTime eventEnd = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(event.getEnd().getDateTime().getValue()), ZoneId.systemDefault());
            
            if (isOverlapping(start, end, eventStart, eventEnd)) {
                return false;
            }
        }

        return true;
    }

    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        // Two slots overlap if one starts before the other ends, and vice-versa.
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
