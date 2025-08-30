package com.codingshuttle.youtube.hospitalManagement.dto;

import lombok.Data;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
/*
@Data
public class CreateAppointmentRequestDto {
    private Long doctorId;
    private Long patientId;
    private LocalDateTime appointmentTime;
    private String reason;
}
*/



@Data
public class CreateAppointmentRequestDto {
    @NotNull
    private Long doctorId;

    @NotNull
    private Long patientId;

    @NotNull
    @Future
    private LocalDateTime appointmentTime;

    private String reason;
}

