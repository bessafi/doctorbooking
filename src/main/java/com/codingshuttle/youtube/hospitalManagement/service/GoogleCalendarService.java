package com.codingshuttle.youtube.hospitalManagement.service;

import com.codingshuttle.youtube.hospitalManagement.entity.Appointment;
import com.codingshuttle.youtube.hospitalManagement.entity.DoctorCalendar;
import com.codingshuttle.youtube.hospitalManagement.repository.DoctorCalendarRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private GoogleAuthorizationCodeFlow flow;
    private final DoctorCalendarRepository doctorCalendarRepository;

    @PostConstruct
    public void init() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientId, clientSecret, SCOPES)
                .setAccessType("offline")
                .build();
    }

    /**
     * Fetches all events from the doctor's primary calendar from a given start time onwards.
     *
     * @param doctorCalendar The doctor's calendar credentials.
     * @param start          The time from which to fetch events.
     * @return A list of Google Calendar Events.
     * @throws IOException              If the API call fails.
     * @throws GeneralSecurityException If there is a security issue.
     */
    public List<Event> getEvents(DoctorCalendar doctorCalendar, LocalDateTime start) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(doctorCalendar);
        DateTime timeMin = new DateTime(start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        Events events = service.events().list("primary")
                .setTimeMin(timeMin)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }

    public Event createEvent(Appointment appointment, DoctorCalendar doctorCalendar) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(doctorCalendar);

        Event event = new Event()
                .setSummary("Appointment with " + appointment.getPatient().getName())
                .setDescription(appointment.getReason());

        DateTime startDateTime = new DateTime(appointment.getAppointmentTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        EventDateTime start = new EventDateTime().setDateTime(startDateTime);
        event.setStart(start);

        DateTime endDateTime = new DateTime(appointment.getAppointmentTime().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        EventDateTime end = new EventDateTime().setDateTime(endDateTime);
        event.setEnd(end);

        String calendarId = "primary";
        return service.events().insert(calendarId, event).execute();
    }

    public void deleteEvent(String eventId, DoctorCalendar doctorCalendar) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(doctorCalendar);
        service.events().delete("primary", eventId).execute();
    }

    // Helper method to get an authenticated Calendar service client
    private Calendar getCalendarService(DoctorCalendar doctorCalendar) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        // This is a simplified way to create a credential. In a real app, you'd handle token refresh.
        Credential credential = flow.createAndStoreCredential(new GoogleTokenResponse()
                .setAccessToken(doctorCalendar.getAccessToken())
                .setRefreshToken(doctorCalendar.getRefreshToken()), "user");

        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("Doctor Appointment System")
                .build();
    }
}
