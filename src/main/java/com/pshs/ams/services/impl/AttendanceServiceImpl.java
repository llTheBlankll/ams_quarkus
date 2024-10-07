package com.pshs.ams.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pshs.ams.models.dto.attendance.AttendanceDTO;
import com.pshs.ams.models.dto.attendance.ClassroomDemographicsAttendanceDTO;
import com.pshs.ams.models.dto.custom.DateRange;
import com.pshs.ams.models.dto.custom.LineChartDTO;
import com.pshs.ams.models.dto.custom.MessageDTO;
import com.pshs.ams.models.dto.custom.RFIDCardDTO;
import com.pshs.ams.models.entities.*;
import com.pshs.ams.models.enums.*;
import com.pshs.ams.models.interfaces.AttendanceForeignEntity;
import com.pshs.ams.services.RealTimeAttendanceService;
import com.pshs.ams.services.interfaces.AttendanceService;
import com.pshs.ams.services.interfaces.ClassroomService;
import com.pshs.ams.services.interfaces.StudentService;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApplicationScoped
public class AttendanceServiceImpl implements AttendanceService {

	@Inject
	Logger logger;

	@Inject
	RealTimeAttendanceService realTimeAttendanceService;

	@Inject
	StudentService studentService;

	@Inject
	ClassroomService classroomService;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ModelMapper modelMapper = new ModelMapper();

	public AttendanceServiceImpl() {
		objectMapper.registerModule(new JavaTimeModule());
	}

	/**
	 * @param attendance
	 * @return
	 */
	@Override
	public CodeStatus createAttendance(Attendance attendance) {
		if (attendance == null) {
			logger.debug("Attendance is null");
			return CodeStatus.NULL;
		}

		// Check if the student has the same attendance for today correlated to the date and status
		boolean exists = Attendance.count("student = ?1 and date = ?2 and status = ?3", attendance.getStudent(), attendance.getDate(), attendance.getStatus()) > 0;
		if (exists) {
			logger.debug("Student already has attendance for today correlated to the date and status");
			return CodeStatus.EXISTS;
		}

		attendance.persist();
		return CodeStatus.OK;
	}

	/**
	 * @param rfidCardDTO
	 * @return
	 */
	@Override
	@Transactional
	public MessageDTO fromWebSocket(RFIDCardDTO rfidCardDTO) throws JsonProcessingException {
		if (rfidCardDTO == null || rfidCardDTO.getHashedLrn() == null || rfidCardDTO.getMode() == null) {
			logger.debug("RFID Card DTO is null");
			return new MessageDTO(
				"RFID Card DTO is null",
				CodeStatus.NULL
			);
		}

		Optional<RfidCredential> rfidCredential = RfidCredential.find("hashedLrn = ?1", rfidCardDTO.getHashedLrn()).firstResultOptional();
		if (rfidCredential.isEmpty()) {
			logger.debug("RFID Card not found: " + rfidCardDTO.getHashedLrn());
			return new MessageDTO(
				"RFID Card not found",
				CodeStatus.NOT_FOUND
			);
		}

		LocalDate now = LocalDate.now();
		Attendance attendance = new Attendance();
		attendance.setStudent(rfidCredential.get().getStudent());
		attendance.setDate(LocalDate.now());
		Optional<Attendance> latestAttendanceOptional = Attendance.find("date = ?1 AND student.id = ?2", now, rfidCredential.get().getStudent().getId()).firstResultOptional();

		// * Now, what we have to do is to calculate the timeIn, timeOut, and status
		switch (rfidCardDTO.getMode()) {
			case IN -> {
				// Check if the student has the same attendance for today correlated to the date and status
				if (latestAttendanceOptional.isPresent()) {
					logger.debug("Student already has attendance for today correlated to the date and status");
					return new MessageDTO(
						"Already Checked In",
						CodeStatus.EXISTS
					);
				}

				// Create attendance
				logger.debug("Creating attendance for student: " + rfidCredential.get().getStudent());
				attendance.setTimeIn(LocalTime.now());
				attendance.setStatus(getAttendanceStatus(rfidCardDTO.getMode(), rfidCredential.get().getStudent()));
				attendance.persist();
				realTimeAttendanceService.broadcastMessage(objectMapper.writeValueAsString(
					modelMapper.map(attendance, AttendanceDTO.class)
				));
				return new MessageDTO(
					"Status: " + attendance.getStatus(),
					CodeStatus.OK
				);
			}

			case OUT -> {
				// Get the latest attendance
				if (latestAttendanceOptional.isEmpty()) {
					logger.debug("No attendance found for today");
					return new MessageDTO(
						"Not Checked In",
						CodeStatus.NOT_FOUND
					);
				}

				// Get Attendance
				Attendance latestAttendance = latestAttendanceOptional.get();
				if (latestAttendance.getTimeOut() != null) {
					logger.debug("Student already has attendance for today correlated to the date and status");
					return new MessageDTO(
						"Already Checked Out",
						CodeStatus.EXISTS
					);
				}

				// Update attendance
				logger.debug("Updating attendance for student: " + rfidCredential.get().getStudent());
				latestAttendance.setTimeOut(LocalTime.now());
				latestAttendance.persist();
				realTimeAttendanceService.broadcastMessage(objectMapper.writeValueAsString(
					modelMapper.map(latestAttendance, AttendanceDTO.class)
				));
				return new MessageDTO(
					"Status: " + latestAttendance.getStatus(),
					CodeStatus.OK
				);
			}

			case EXCUSED -> {
				if (latestAttendanceOptional.isEmpty()) {
					logger.debug("No attendance found, when excusing student, consult to the administrator or teachers.");
					return new MessageDTO(
						"Consult admin/teachers",
						CodeStatus.OK
					);
				}
				// Update attendance
				logger.debug("Updating attendance for student: " + rfidCredential.get().getStudent());
				Attendance latestAttendance = latestAttendanceOptional.get();
				if (latestAttendance.getStatus() == AttendanceStatus.EXCUSED) {
					logger.debug("Student already has excused attendance for today correlated to the date and status");
					return new MessageDTO(
						"Already Excused",
						CodeStatus.EXISTS
					);
				}

				latestAttendance.setNotes("This student was scanned as excused.");
				latestAttendance.setStatus(getAttendanceStatus(rfidCardDTO.getMode(), rfidCredential.get().getStudent()));
				latestAttendance.setTimeOut(LocalTime.now());
				latestAttendance.persist();

				realTimeAttendanceService.broadcastMessage(
					objectMapper.writeValueAsString(
						modelMapper.map(latestAttendance, AttendanceDTO.class)
					)
				);
				return new MessageDTO(
					"Status: EXCUSED",
					CodeStatus.OK
				);
			}

			default -> {
				throw new Error("Invalid attendance mode: " + rfidCardDTO.getMode());
			}
		}
	}

	private AttendanceStatus getAttendanceStatus(AttendanceMode mode, Student student) {
		StudentSchedule studentSchedule = student.getStudentSchedule();
		LocalTime now = LocalTime.now();
		switch (mode) {
			case IN -> {
				if (now.isBefore(studentSchedule.getLateTime())) {
					return AttendanceStatus.ON_TIME;
				} else {
					return AttendanceStatus.LATE;
				}
			}

			case EXCUSED -> {
				return AttendanceStatus.EXCUSED;
			}

			default -> {
				throw new Error("Invalid attendance mode: " + mode);
			}
		}
	}

	/**
	 * Count total attendance of each status with a classroom filter
	 *
	 * @param attendanceStatus Attendance Status
	 * @param dateRange        range of dates to filter by
	 * @param id
	 * @param foreignEntity
	 * @return total count of attendance
	 */
	@Override
	public long countTotalByAttendanceByStatus(List<AttendanceStatus> attendanceStatus, DateRange dateRange, Long id, AttendanceForeignEntity foreignEntity) {
		if (id <= 0) {
			throw new Error("Invalid classroom id: " + id);
		}
		if (attendanceStatus == null || attendanceStatus.isEmpty()) {
			throw new Error("Invalid attendance status: " + attendanceStatus);
		}

		logger.debug("Count total attendance by status: " + attendanceStatus);
		logger.debug("Date Range: " + dateRange);
		logger.debug("Foreign Entity: " + foreignEntity);

		if (foreignEntity == AttendanceForeignEntity.STUDENT) {
			// Check if exists
			if (studentService.getStudent(id.longValue()).isEmpty()) {
				return 0;
			}

			return Attendance.count("status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.id = ?4", attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate(), id);
		} else if (foreignEntity == AttendanceForeignEntity.CLASSROOM) {
			// Check if exists
			if (classroomService.getClassroom(id).isEmpty()) {
				return 0;
			}

			return Attendance.count("status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.classroom.id = ?4", attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate(), id);
		}

		return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3", attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate());
	}

	/**
	 * Get classroom demographics chart
	 *
	 * @param statuses  list of {@link AttendanceStatus} to filter by
	 * @param dateRange range of dates to filter by
	 * @param id        classroom id
	 * @return list of {@link ClassroomDemographicsAttendanceDTO} objects
	 */
	@Override
	public ClassroomDemographicsAttendanceDTO getClassroomAttendanceDemographicsChart(List<AttendanceStatus> statuses, DateRange dateRange, Long id) {
		if (id <= 0) {
			logger.debug("Invalid classroom id: " + id);
			throw new Error("Invalid classroom id: " + id);
		}
		if (statuses == null || statuses.isEmpty()) {
			logger.debug("Invalid attendance status: " + statuses);
			throw new Error("Invalid attendance status: " + statuses);
		}

		// Check if classroom exists
		Optional<Classroom> classroom = classroomService.getClassroom(id);
		if (classroom.isEmpty()) {
			logger.debug("Classroom not found: " + id);
			throw new Error("Classroom not found: " + id);
		}

		long male = Attendance.count("status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.classroom.id = ?4 AND cast(student.sex AS text) = ?5", statuses, dateRange.getStartDate(), dateRange.getEndDate(), id, Sex.MALE.name());
		long female = Attendance.count("status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.classroom.id = ?4 AND cast(student.sex AS text) = ?5", statuses, dateRange.getStartDate(), dateRange.getEndDate(), id, Sex.FEMALE.name());
		return new ClassroomDemographicsAttendanceDTO(male, female);
	}

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
		return Attendance.count("status IN ?1 AND date BETWEEN ?2 AND ?3", attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate());
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
		return Attendance.find("status IN ?1 BETWEEN ?2 AND ?3", sort, attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate()).page(page).list();
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
	public List<Attendance> getAllAttendanceByStatusAndDateRange(List<AttendanceStatus> attendanceStatus, DateRange dateRange, AttendanceForeignEntity foreignEntity, Integer id, Page page, Sort sort) {
		// TODO: Add checks
		sort = Sort.by("date", "timeIn", "timeOut").descending();

		logger.debug("Sorting by date, time in, and time out");

		if (foreignEntity == AttendanceForeignEntity.STUDENT) {
			return Attendance.find("status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.id = ?4", sort, attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate(), id).page(page).list();
		} else if (foreignEntity == AttendanceForeignEntity.CLASSROOM) {
			return Attendance.find("status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.classroom.id = ?4", sort, attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate(), id).page(page).list();
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
	public LineChartDTO getLineChart(List<AttendanceStatus> statuses, DateRange dateRange, AttendanceForeignEntity foreignEntity, Long id, TimeStack stack) {
		logger.debug("Called getLineChart");
		List<String> labels = new ArrayList<>();
		List<String> data = new ArrayList<>();
		Map<DateRange, Long> dailyCounts = new TreeMap<>(Comparator.comparing(DateRange::getStartDate));

		LocalDate currentDate = dateRange.getStartDate();
		while (currentDate.isBefore(dateRange.getEndDate())) {
			LocalDate nextDate = switch (stack) {
				case WEEK -> currentDate.plusWeeks(1);
				case MONTH -> currentDate.plusMonths(1);
				case YEAR -> currentDate.plusYears(1);
				default -> currentDate.plusDays(1);
			};

			DateRange periodRange = new DateRange(currentDate, nextDate);
			if (!dailyCounts.containsKey(periodRange)) {
				dailyCounts.put(periodRange, 0L);
			}

			logger.debug("Count total attendance: " + periodRange);
			long attendances = 0;
			if (foreignEntity == AttendanceForeignEntity.STUDENT || foreignEntity == AttendanceForeignEntity.CLASSROOM) {
				attendances = countTotalByAttendanceByStatus(statuses, periodRange, id, foreignEntity);
			}
			dailyCounts.put(periodRange, dailyCounts.get(periodRange) + Math.toIntExact(attendances));

			currentDate = nextDate;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
		DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");

		for (Map.Entry<DateRange, Long> entry : dailyCounts.entrySet()) {
			LocalDate startDate = entry.getKey().getStartDate();
			LocalDate endDate = entry.getKey().getEndDate().minusDays(1); // Subtract one day to show inclusive range

			String label = switch (stack) {
				case WEEK -> String.format("%s to %s, %s",
					startDate.format(formatter),
					endDate.format(formatter),
					startDate.format(yearFormatter));
				case MONTH -> String.format("%s to %s, %s",
					startDate.format(formatter),
					endDate.format(formatter),
					startDate.format(yearFormatter));
				case YEAR -> String.format("%s to %s",
					startDate.format(yearFormatter),
					endDate.format(yearFormatter));
				default -> startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
			};

			labels.add(label);
			data.add(entry.getValue().toString());
		}

		return new LineChartDTO(labels, data);
	}

	/**
	 * @param statuses
	 * @param dateRange
	 * @param stack
	 * @return
	 */
	@Override
	public LineChartDTO getLineChart(List<AttendanceStatus> statuses, DateRange dateRange, TimeStack stack) {
		logger.debug("Called getLineChart, no foreign entity");
		List<String> labels = new ArrayList<>();
		List<String> data = new ArrayList<>();
		Map<LocalDate, Integer> dailyCounts = new TreeMap<>();

		LocalDate currentDate = dateRange.getStartDate();
		while (currentDate.isBefore(dateRange.getEndDate())) {
//			String date;
			LocalDate date;
			LocalDate nextDate = switch (stack) {
				case WEEK -> {
//					date = "Week " + currentDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + ", " + currentDate.getYear();
					date = currentDate.plusWeeks(1);
					yield currentDate.plusWeeks(1);
				}
				case MONTH -> {
//					date = currentDate.getMonth().toString() + ", " + currentDate.getYear();
					date = currentDate.plusMonths(1);
					yield currentDate.plusMonths(1);
				}
				default -> {
//					date = currentDate.toString();
					date = currentDate.plusDays(1);
					yield currentDate.plusDays(1);
				}
			};

			if (!dailyCounts.containsKey(date)) {
				dailyCounts.put(date, 0);
			}

			logger.debug("Count total attendance: " + new DateRange(currentDate, nextDate));
			long attendances = countTotalByAttendanceByStatus(statuses, new DateRange(currentDate, nextDate));
			dailyCounts.put(date, dailyCounts.get(date) + (int) attendances);

			currentDate = nextDate;
		}

		for (Map.Entry<LocalDate, Integer> entry : dailyCounts.entrySet()) {
			labels.add(entry.getKey().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
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
	public long countAttendances(DateRange dateRange, List<AttendanceStatus> statuses, AttendanceForeignEntity foreignEntity, Integer id, List<Sex> sexes) {
		logger.debug("Count total attendance: " + dateRange);
		if (foreignEntity == AttendanceForeignEntity.STUDENT) {
			logger.debug("Student: " + foreignEntity);
			return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3 AND student.id = ?4", statuses, dateRange.getStartDate(), dateRange.getEndDate(), id);
		} else if (foreignEntity == AttendanceForeignEntity.CLASSROOM) {
			logger.debug("Classroom: " + foreignEntity);
			return Attendance.count("status IN ?1 BETWEEN ?2 AND ?3 AND classroom.id = ?4", statuses, dateRange.getStartDate(), dateRange.getEndDate(), id);
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
}