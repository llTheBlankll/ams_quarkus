package com.pshs.ams.repositories;

import com.pshs.ams.models.entities.Attendance;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository {
    List<Attendance> getFilteredAttendances(LocalDate date, Integer classroomId, Integer gradeLevelId, Integer strandId, Long studentId);
    
    // Add other attendance-related repository methods here
}
