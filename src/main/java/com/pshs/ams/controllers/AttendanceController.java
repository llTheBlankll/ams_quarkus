package com.pshs.ams.controllers;

import com.pshs.ams.models.dto.attendance.AttendanceDTO;
import com.pshs.ams.models.dto.custom.DateRange;
import com.pshs.ams.models.dto.custom.LineChartDTO;
import com.pshs.ams.models.enums.AttendanceStatus;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class AttendanceController {


	/**
	 * Count total attendance of each status
	 *
	 * @param attendanceStatus Attendance Status
	 * @return total count of attendance
	 */
	public long countTotalByAttendanceByStatusAndDateRange(AttendanceStatus attendanceStatus, DateRange dateRange) {
		return 0;
	}



	/**
	 * Get all attendance by status and date range
	 *
	 * @param attendanceStatus Attendance Status
	 * @param startDate        start date
	 * @param endDate          end date
	 * @return list of attendance
	 */
	public List<AttendanceDTO> getAllAttendanceByStatusAndDateRange(AttendanceStatus attendanceStatus, LocalDate startDate,
	                                                                LocalDate endDate) {
		return null;
	}

	/**
	 * Get line chart data
	 *
	 * @param startDate start date
	 * @param endDate   end date
	 * @return list of line chart data
	 */
	public List<LineChartDTO> getLineChart(LocalDate startDate, LocalDate endDate) {
		return null;
	}

	/**
	 * Get line chart data of total attendance
	 *
	 * @param startDate start date
	 * @param endDate   end date
	 * @return list of line chart data
	 */
	public List<LineChartDTO> getLineChartOfTotalAttendance(LocalDate startDate, LocalDate endDate) {
		return null;
	}

	/**
	 * Count total attendance
	 *
	 * @return total count of attendance
	 */
	public long countAttendances() {
		return 0;
	}

	/**
	 * Count total attendance in a class
	 *
	 * @param classroomId classroom id
	 * @return total count of attendance
	 */
	public long countAttendancesInClass(Long classroomId) {
		return 0;
	}

	/**
	 * Get all student attendance
	 *
	 * @param studentId student id
	 * @return list of attendance
	 */
	public List<AttendanceDTO> getAllStudentAttendance(Long studentId) {
		return null;
	}

	/**
	 * Get total student attendance
	 *
	 * @param studentId student id
	 * @return total count of attendance
	 */
	public long totalStudentAttendance(Long studentId) {
		return 0;
	}
}