package com.codingshuttle.youtube.hospitalManagement.repository;

import com.codingshuttle.youtube.hospitalManagement.entity.DoctorCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DoctorCalendarRepository extends JpaRepository<DoctorCalendar, Long> {
    Optional<DoctorCalendar> findByDoctorId(Long doctorId);

}