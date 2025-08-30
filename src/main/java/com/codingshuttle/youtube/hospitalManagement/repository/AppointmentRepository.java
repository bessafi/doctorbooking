package com.codingshuttle.youtube.hospitalManagement.repository;

import com.codingshuttle.youtube.hospitalManagement.entity.Appointment;
//import com.clinic.doctorappointment.entity.Appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Finds all appointments for a specific doctor within a given time interval.
     * This is crucial for checking availability.
     *
     * @param doctorId The ID of the doctor.
     * @param start    The start of the time interval.
     * @param end      The end of the time interval.
     * @return A list of appointments.
     */
    List<Appointment> findAllByDoctor_IdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
     

    List<Appointment> findByPatient_Id(Long patientId);
    List<Appointment> findByDoctor_Id(Long doctorId);
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Appointment> findByPatient_IdAndAppointmentTimeBetween(Long patientId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByDoctor_IdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByDoctor_IdAndPatient_Id(Long doctorId, Long patientId);
    List<Appointment> findByDoctor_IdAndPatient_IdAndAppointmentTimeBetween(Long doctorId, Long patientId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByAppointmentTime(LocalDateTime appointmentTime);
    List<Appointment> findByDoctor_IdAndAppointmentTime(Long doctorId, LocalDateTime appointmentTime);
    List<Appointment> findByPatient_IdAndAppointmentTime(Long patientId, LocalDateTime appointmentTime);
    List<Appointment> findByDoctor_IdAndPatient_IdAndAppointmentTime(Long doctorId, Long patientId, LocalDateTime appointmentTime);
    List<Appointment> findByPatientIdAndAppointmentTimeAfter(Long patientId, LocalDateTime dateTime);
    List<Appointment> findByDoctorIdAndAppointmentTimeAfter(Long doctorId, LocalDateTime dateTime);
}
   
