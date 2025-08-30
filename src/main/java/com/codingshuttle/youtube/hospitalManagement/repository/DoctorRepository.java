package com.codingshuttle.youtube.hospitalManagement.repository;
import com.codingshuttle.youtube.hospitalManagement.entity.Doctor;
//import com.clinic.doctorappointment.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}