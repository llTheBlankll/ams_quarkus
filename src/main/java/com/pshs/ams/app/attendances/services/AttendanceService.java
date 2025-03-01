package com.pshs.ams.app.attendances.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pshs.ams.app.attendances.models.dto.AttendanceDTO;
import com.pshs.ams.app.attendances.models.dto.ClassroomDemographicsAttendances;
import com.pshs.ams.app.attendances.models.dto.FingerprintAttendance;
import com.pshs.ams.global.models.custom.DateRange;
import com.pshs.ams.global.models.custom.LineChart;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.RFIDCard;
import com.pshs.ams.app.attendances.models.entities.Attendance;
import com.pshs.ams.app.students.models.entities.Student;
import com.pshs.ams.app.attendances.models.enums.AttendanceStatus;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.global.models.enums.Sex;
import com.pshs.ams.global.models.enums.TimeStack;
import com.pshs.ams.global.models.enums.AttendanceForeignEntity;
import com.pshs.ams.app.classrooms.models.dto.ClassroomRankingDTO;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import java.util.List;

public interface AttendanceService {

	CodeStatus createAttendance(Attendance attendance, Boolean override, Boolean checkForAbsent) throws IllegalArgumentException;

	MessageResponse fromWebSocket(RFIDCard rfidCard) throws JsonProcessingException;

	void fromFingerprint(FingerprintAttendance attendance) throws IllegalArgumentException, JsonProcessingException;

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
	long countTotalByAttendanceByStatus(List<AttendanceStatus> attendanceStatus, DateRange dateRange, Long id,
			AttendanceForeignEntity foreignEntity);

	// Region: Get All Attendance

	/**
	 * Get all attendance by status and date range
	 *
	 * @param attendanceStatus Attendance Status
	 * @param dateRange        range of dates to filter by
	 * @return list of {@link AttendanceDTO} objects
	 */
	List<Attendance> getAllAttendanceByStatusAndDateRange(List<AttendanceStatus> attendanceStatus, DateRange dateRange,
			Page page, Sort sort);

	/**
	 * Get all attendance by status, date range, and foreign entity
	 *
	 * @param attendanceStatus Attendance Status
	 * @param dateRange        range of dates to filter by
	 * @param foreignEntity    the foreign entity to filter by
	 * @return list of {@link AttendanceDTO} objects
	 */
	List<Attendance> getAllAttendanceByStatusAndDateRange(List<AttendanceStatus> attendanceStatus, DateRange dateRange,
			AttendanceForeignEntity foreignEntity, Integer id, Page page, Sort sort);

	/**
	 * Get line chart data
	 *
	 * @param statuses      list of {@link AttendanceStatus} to filter by
	 * @param dateRange     range of dates to filter by
	 * @param foreignEntity the foreign entity to filter by
	 * @param stack         the time stack to group by
	 * @return list of {@link LineChart} objects
	 */
	LineChart getLineChart(List<AttendanceStatus> statuses, DateRange dateRange, AttendanceForeignEntity foreignEntity,
	                       Long id, TimeStack stack);

	/**
	 * Get line chart data
	 *
	 * @param statuses  list of {@link AttendanceStatus} to filter by
	 * @param dateRange range of dates to filter by
	 * @param stack     the time stack to group by
	 * @return list of {@link LineChart} objects
	 */
	LineChart getLineChart(List<AttendanceStatus> statuses, DateRange dateRange, TimeStack stack);

	/**
	 * Get classroom demographics chart
	 *
	 * @param statuses  list of {@link AttendanceStatus} to filter by
	 * @param dateRange range of dates to filter by
	 * @param id        classroom id
	 * @return list of {@link ClassroomDemographicsAttendances} objects
	 */
	ClassroomDemographicsAttendances getClassroomAttendanceDemographicsChart(List<AttendanceStatus> statuses,
	                                                                         DateRange dateRange, Long id);

	/**
	 * Count total attendance
	 *
	 * @param dateRange     range of dates to filter by
	 * @param statuses      list of {@link AttendanceStatus} to filter by
	 * @param foreignEntity the foreign entity to filter by
	 * @param sexes         list of {@link Sex} to filter by
	 * @return total count of attendance
	 */
	long countAttendances(DateRange dateRange, List<AttendanceStatus> statuses, AttendanceForeignEntity foreignEntity,
			Integer id, List<Sex> sexes);

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

	List<Attendance> getFilteredAttendances(DateRange dateRange, Integer classroomId, Integer gradeLevelId,
			Integer strandId, Long studentId, Page page, Sort sort);

	long countFilteredAttendances(DateRange dateRange, Integer classroomId, Integer gradeLevelId, Integer strandId,
			Long studentId);

	/**
	 * Get classroom ranking based on attendance rate
	 *
	 * @param dateRange date range to calculate ranking
	 * @param limit     maximum number of classrooms to return
	 * @return list of classroom rankings
	 */
	List<ClassroomRankingDTO> getClassroomRanking(DateRange dateRange, Integer limit);

	/**
	 * Update an attendance record
	 *
	 * @param id            ID of the attendance record to update
	 * @param attendanceDTO Updated attendance data
	 * @return Updated attendance record
	 */
	Attendance updateAttendance(Long id, AttendanceDTO attendanceDTO);

	/**
	 * Get absent students
	 *
	 * @param dateRange date range to filter by
	 * @return list of absent students
	 */
	List<Student> getAbsentStudents(DateRange dateRange);

	/**
	 * Get last hour attendance
	 *
	 * @return list of attendance
	 */
	List<Student> getLastHourAttendance(List<AttendanceStatus> attendanceStatus) throws IllegalArgumentException;

	/**
	 * Count last hour attendance
	 *
	 * @return total count of attendance
	 */
	long countLastHourAttendance(List<AttendanceStatus> attendanceStatuses) throws IllegalArgumentException;
}
