package com.pshs.ams.services.impl;

import com.pshs.ams.models.dto.attendance.AttendanceDTO;
import com.pshs.ams.models.dto.custom.DateRange;
import com.pshs.ams.models.dto.custom.LineChartDTO;
import com.pshs.ams.models.enums.AttendanceStatus;
import com.pshs.ams.models.enums.Sex;
import com.pshs.ams.models.enums.TimeStack;
import com.pshs.ams.models.interfaces.AttendanceForeignEntity;
import com.pshs.ams.services.AttendanceService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class AttendanceServiceImpl implements AttendanceService {

	@Inject
	Logger logger;

	/**
	 * Count total attendance of each status
	 *
	 * @param attendanceStatus Attendance Status
	 * @param dateRange
	 * @return total count of attendance
	 */
	@Override
	public long countTotalByAttendanceByStatus(AttendanceStatus attendanceStatus, DateRange dateRange) {
		return 0;
	}

	/**
	 * Count total attendance of each status with a foreign entity filter
	 *
	 * @param attendanceStatus Attendance Status
	 * @param dateRange        range of dates to filter by
	 * @param foreignEntity    the foreign entity to filter by
	 * @return total count of attendance
	 */
	@Override
	public long countTotalByAttendanceByStatus(AttendanceStatus attendanceStatus, DateRange dateRange, AttendanceForeignEntity foreignEntity) {
		return 0;
	}

	/**
	 * Get all attendance by status and date range
	 *
	 * @param attendanceStatus Attendance Status
	 * @param dateRange        range of dates to filter by
	 * @param page
	 * @param sort
	 * @return list of {@link AttendanceDTO} objects
	 */
	@Override
	public List<AttendanceDTO> getAllAttendanceByStatusAndDateRange(AttendanceStatus attendanceStatus, DateRange dateRange, Page page, Sort sort) {
		return List.of();
	}

	/**
	 * Get all attendance by status, date range, and foreign entity
	 *
	 * @param attendanceStatus Attendance Status
	 * @param dateRange        range of dates to filter by
	 * @param foreignEntity    the foreign entity to filter by
	 * @param page
	 * @param sort
	 * @return list of {@link AttendanceDTO} objects
	 */
	@Override
	public List<AttendanceDTO> getAllAttendanceByStatusAndDateRange(AttendanceStatus attendanceStatus, DateRange dateRange, AttendanceForeignEntity foreignEntity, Page page, Sort sort) {
		return List.of();
	}

	/**
	 * Get line chart data
	 *
	 * @param statuses      list of {@link AttendanceStatus} to filter by
	 * @param dateRange     range of dates to filter by
	 * @param foreignEntity the foreign entity to filter by
	 * @param stack         the time stack to group by
	 * @return list of {@link LineChartDTO} objects
	 */
	@Override
	public List<LineChartDTO> getLineChart(List<AttendanceStatus> statuses, DateRange dateRange, AttendanceForeignEntity foreignEntity, TimeStack stack) {
		return List.of();
	}

	/**
	 * Count total attendance
	 *
	 * @param dateRange     range of dates to filter by
	 * @param statuses      list of {@link AttendanceStatus} to filter by
	 * @param foreignEntity the foreign entity to filter by
	 * @param sexes         list of {@link Sex} to filter by
	 * @return total count of attendance
	 */
	@Override
	public long countAttendances(DateRange dateRange, List<AttendanceStatus> statuses, AttendanceForeignEntity foreignEntity, List<Sex> sexes) {
		return 0;
	}

	/**
	 * Count total attendance
	 *
	 * @param dateRange range of dates to filter by
	 * @param statuses  list of {@link AttendanceStatus} to filter by
	 * @param sexes     list of {@link Sex} to filter by
	 * @return total count of attendance
	 */
	@Override
	public long countAttendances(DateRange dateRange, List<AttendanceStatus> statuses, List<Sex> sexes) {
		return 0;
	}

	/**
	 * Count total attendance in a class
	 *
	 * @param classroomId classroom id
	 * @return total count of attendance
	 */
	@Override
	public long countAttendancesInClass(Long classroomId) {
		return 0;
	}

	/**
	 * Get all student attendance
	 *
	 * @param studentId student id
	 * @param page
	 * @return list of attendance
	 */
	@Override
	public List<AttendanceDTO> getAllStudentAttendance(Long studentId, Page page) {
		return List.of();
	}

	/**
	 * Get total student attendance
	 *
	 * @param studentId student id
	 * @return total count of attendance
	 */
	@Override
	public long totalStudentAttendance(Long studentId) {
		return 0;
	}
}