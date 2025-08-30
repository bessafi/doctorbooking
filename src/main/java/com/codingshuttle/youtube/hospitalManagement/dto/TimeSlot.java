package com.codingshuttle.youtube.hospitalManagement.dto;

//package com.clinic.doctorappointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {
    private LocalDateTime start;
    private LocalDateTime end;

    /**
     * Checks if this time slot overlaps with another time slot.
     * Overlap occurs if one slot starts before the other ends, and ends after the other starts.
     * @param other The other TimeSlot to check against.
     * @return true if they overlap, false otherwise.
     */
    public boolean overlaps(TimeSlot other) {
        return this.start.isBefore(other.end) && this.end.isAfter(other.start);
    }
}
