package com.codingshuttle.youtube.hospitalManagement.dto;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientResponseDto {
    private Long id;
    private String name;
    private String email;
    private String gender;
    private LocalDate birthDate;
}
