package com.pshs.ams.services.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pshs.ams.models.dto.attendance.AttendanceDTO;
import com.pshs.ams.models.dto.attendance.ClassroomDemographicsAttendanceDTO;
import com.pshs.ams.models.dto.custom.DateRange;
import com.pshs.ams.models.dto.custom.LineChartDTO;
import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.dto.custom.RFIDCardDTO;
import com.pshs.ams.models.entities.Attendance;
import com.pshs.ams.models.enums.AttendanceStatus;
import com.pshs.ams.models.enums.CodeStatus;
import com.pshs.ams.models.enums.Sex;
import com.pshs.ams.models.enums.TimeStack;
import com.pshs.ams.models.interfaces.AttendanceForeignEntity;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import java.util.List;

public interface AttendanceService {

	CodeStatus createAttendance(Attendance student);

	MessageDTO fromWebSocket(RFIDCardDTO rfidCardDTO) throws JsonProcessingException;

	/**
	 * Count total attendance of each status
	 *
	 * @param attendanceStatus Attendance Status
	 * @return total count of attendance
	 */
	long countTotalByAttendanceByStatus(List<AttendanceStatus> attendanceStatus, DateRange dateRange);

	/**
	 * Count total attendance of each status with a classroom filter
	 *
	 * @param attendanceStatus Attendance Status
	 * @param dateRange        range of dates to filter by
	 * @return total count of attendance
	 */
	long countTotalByAttendanceByStatus(List<AttendanceStatus> attendanceStatus, DateRange dateRange, Long id, AttendanceForeignEntity foreignEntity);

	// Region: Get All Attendance

	/**
	 * Get all attendance by status and date range
	 *
	 * @param attendanceStatus Attendance Status
	 * @param dateRange        range of dates to filter by
	 * @return list of {@link AttendanceDTO} objects
	 */
	List<Attendance> getAllAttendanceByStatusAndDateRange(List<AttendanceStatus> attendanceStatus, DateRange dateRange, Page page, Sort sort);

	/**
	 * Get all attendance by status, date range, and foreign entity
	 *
	 * @param attendanceStatus Attendance Status
	 * @param dateRange        range of dates to filter by
	 * @param foreignEntity    the foreign entity to filter by
	 * @return list of {@link AttendanceDTO} objects
	 */
	List<Attendance> getAllAttendanceByStatusAndDateRange(List<AttendanceStatus> attendanceStatus, DateRange dateRange, AttendanceForeignEntity foreignEntity, Integer id, Page page, Sort sort);

	/**
	 * Get line chart data
	 *
	 * @param statuses      list of {@link AttendanceStatus} to filter by
	 * @param dateRange     range of dates to filter by
	 * @param foreignEntity the foreign entity to filter by
	 * @param stack         the time stack to group by
	 * @return list of {@link LineChartDTO} objects
	 */
	LineChartDTO getLineChart(List<AttendanceStatus> statuses, DateRange dateRange, AttendanceForeignEntity foreignEntity, Long id, TimeStack stack);

	/**
	 * Get line chart data
	 *
	 * @param statuses  list of {@link AttendanceStatus} to filter by
	 * @param dateRange range of dates to filter by
	 * @param stack     the time stack to group by
	 * @return list of {@link LineChartDTO} objects
	 */
	LineChartDTO getLineChart(List<AttendanceStatus> statuses, DateRange dateRange, TimeStack stack);

	/**
	 * Get classroom demographics chart
	 *
	 * @param statuses  list of {@link AttendanceStatus} to filter by
	 * @param dateRange range of dates to filter by
	 * @param id        classroom id
	 * @return list of {@link ClassroomDemographicsAttendanceDTO} objects
	 */
	ClassroomDemographicsAttendanceDTO getClassroomAttendanceDemographicsChart(List<AttendanceStatus> statuses, DateRange dateRange, Long id);

	/**
	 * Count total attendance
	 *
	 * @param dateRange     range of dates to filter by
	 * @param statuses      list of {@link AttendanceStatus} to filter by
	 * @param foreignEntity the foreign entity to filter by
	 * @param sexes         list of {@link Sex} to filter by
	 * @return total count of attendance
	 */
	long countAttendances(DateRange dateRange, List<AttendanceStatus> statuses, AttendanceForeignEntity foreignEntity, Integer id, List<Sex> sexes);

	/**
	 * Count total attendance
	 *
	 * @param dateRange range of dates to filter by
	 * @param statuses  list of {@link AttendanceStatus} to filter by
	 * @param sexes     list of {@link Sex} to filter by
	 * @return total count of attendance
	 */
	long countAttendances(DateRange dateRange, List<AttendanceStatus> statuses, List<Sex> sexes);

	/**
	 * Count total attendance in a class
	 *
	 * @param classroomId classroom id
	 * @return total count of attendance
	 */
	long countAttendancesInClass(Long classroomId);

	/**
	 * Get all student attendance
	 *
	 * @param studentId student id
	 * @return list of attendance
	 */
	List<Attendance> getAllStudentAttendance(Long studentId, Page page);

	/**
	 * Get total student attendance
	 *
	 * @param studentId student id
	 * @return total count of attendance
	 */
	long totalStudentAttendance(Long studentId);
}