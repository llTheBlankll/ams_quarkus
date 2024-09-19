package com.pshs.ams.services.impl;

import com.pshs.ams.models.dto.attendance.AttendanceDTO;
import com.pshs.ams.models.dto.custom.DateRange;
import com.pshs.ams.models.dto.custom.LineChartDTO;
import com.pshs.ams.models.entities.Attendance;
import com.pshs.ams.models.entities.Classroom;
import com.pshs.ams.models.entities.Student;
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

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public long countTotalByAttendanceByStatus(List<AttendanceStatus> attendanceStatus, DateRange dateRange) {
		logger.debug("Count total attendance by status: " + attendanceStatus);
		logger.debug("Date Range: " + dateRange);
		return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3", attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate());
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
	public long countTotalByAttendanceByStatus(List<AttendanceStatus> attendanceStatus, DateRange dateRange, AttendanceForeignEntity foreignEntity) {
		logger.debug("Count total attendance by status: " + attendanceStatus);
		logger.debug("Date Range: " + dateRange);
		logger.debug("Foreign Entity: " + foreignEntity);

		if (isStudentInstance(foreignEntity)) {
			return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3 AND student.id = ?4", attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate(), ((Student) foreignEntity).getId());
		} else if (isClassroomInstance(foreignEntity)) {
			return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3 AND classroom.id = ?4", attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate(), ((Classroom) foreignEntity).getId());
		}

		return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3", attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate());
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
	public List<Attendance> getAllAttendanceByStatusAndDateRange(List<AttendanceStatus> attendanceStatus, DateRange dateRange, Page page, Sort sort) {
		return Attendance.find("status IN ?1 BETWEEN ?2 AND ?3", sort, attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate())
			.page(page)
			.list();
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
	public List<Attendance> getAllAttendanceByStatusAndDateRange(List<AttendanceStatus> attendanceStatus, DateRange dateRange, AttendanceForeignEntity foreignEntity, Page page, Sort sort) {
		if (isStudentInstance(foreignEntity)) {
			return Attendance.find("status IN ?1 BETWEEN ?2 AND ?3 AND student.id = ?4", sort, attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate(), ((Student) foreignEntity).getId()).page(page).list();
		} else if (isClassroomInstance(foreignEntity)) {
			return Attendance.find("status IN ?1 BETWEEN ?2 AND ?3 AND classroom.id = ?4", sort, attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate(), ((Classroom) foreignEntity).getId()).page(page).list();
		}

		return getAllAttendanceByStatusAndDateRange(attendanceStatus, dateRange, page, sort);
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
	public LineChartDTO getLineChart(List<AttendanceStatus> statuses, DateRange dateRange, AttendanceForeignEntity foreignEntity, TimeStack stack) {
		List<String> labels = new ArrayList<>();
		List<String> data = new ArrayList<>();
		Map<String, Integer> dailyCounts = new HashMap<>();

		for (LocalDate date = dateRange.getStartDate(); date.isBefore(dateRange.getEndDate()); date = date.plusDays(1)) {
			String dateString;
			date = switch (stack) {
				case WEEK -> {
					dateString = "Week " + date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + ", " + date.getYear();
					yield date.plusWeeks(1);
				}
				case MONTH -> {
					dateString = date.getMonth().toString() + ", " + date.getYear();
					yield date.plusMonths(1);
				}
				default -> {
					dateString = date.toString();
					yield date.plusDays(1);
				}
			};
			if (!dailyCounts.containsKey(dateString)) {
				dailyCounts.put(dateString, 0);
			}

			long attendances = countTotalByAttendanceByStatus(statuses, new DateRange(dateRange.getStartDate(), date), foreignEntity);
			dailyCounts.put(dateString, dailyCounts.get(dateString) + (int) attendances); // Add attendance count to the date
		}

		for (Map.Entry<String, Integer> entry : dailyCounts.entrySet()) {
			labels.add(entry.getKey());
			data.add(entry.getValue().toString());
		}

		return new LineChartDTO(labels, data);
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
		logger.debug("Count total attendance: " + dateRange);
		if (isStudentInstance(foreignEntity)) {
			logger.debug("Student: " + foreignEntity);
			return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3 AND student.id = ?4", statuses, dateRange.getStartDate(), dateRange.getEndDate(), ((Student) foreignEntity).getId());
		} else if (isClassroomInstance(foreignEntity)) {
			logger.debug("Classroom: " + foreignEntity);
			return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3 AND classroom.id = ?4", statuses, dateRange.getStartDate(), dateRange.getEndDate(), ((Classroom) foreignEntity).getId());
		}

		logger.debug("All: " + dateRange);
		return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3", statuses, dateRange.getStartDate(), dateRange.getEndDate());
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
		logger.debug("Count total attendance: " + dateRange);
		return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3", statuses, dateRange.getStartDate(), dateRange.getEndDate());
	}

	/**
	 * Count total attendance in a class
	 *
	 * @param classroomId classroom id
	 * @return total count of attendance
	 */
	@Override
	public long countAttendancesInClass(Long classroomId) {
		logger.debug("Count total attendance in class: " + classroomId);
		return Attendance.count("classroom.id = ?1", classroomId);
	}

	/**
	 * Get all student attendance
	 *
	 * @param studentId student id
	 * @param page
	 * @return list of attendance
	 */
	@Override
	public List<Attendance> getAllStudentAttendance(Long studentId, Page page) {
		logger.debug("Get all student attendance: " + studentId);
		return Attendance.find("student.id", studentId).page(page).list();
	}

	/**
	 * Get total student attendance
	 *
	 * @param studentId student id
	 * @return total count of attendance
	 */
	@Override
	public long totalStudentAttendance(Long studentId) {
		logger.debug("Get total student attendance: " + studentId);
		return Attendance.count("student.id = ?1", studentId);
	}

	private boolean isStudentInstance(AttendanceForeignEntity foreignEntity) {
		return foreignEntity instanceof Student;
	}

	private boolean isClassroomInstance(AttendanceForeignEntity foreignEntity) {
		return foreignEntity instanceof Classroom;
	}
}