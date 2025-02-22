package com.pshs.ams.app.classrooms.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ClassroomRankingDTO {
    private Integer classroomId;
    private String classroomName;
    private String room;
    private Long totalAttendance;
    private Double attendanceRate;
    private Integer rank;
}
