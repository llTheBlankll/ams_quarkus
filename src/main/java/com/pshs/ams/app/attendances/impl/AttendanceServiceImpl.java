package com.pshs.ams.app.attendances.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.pshs.ams.app.attendances.models.entities.Attendance;
import com.pshs.ams.app.classrooms.models.entities.Classroom;
import com.pshs.ams.app.rfid_credentials.models.entities.RfidCredential;
import com.pshs.ams.app.student_schedules.models.entities.StudentSchedule;
import com.pshs.ams.app.students.models.entities.Student;
import org.jboss.logging.Logger;
import org.modelmapper.ModelMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pshs.ams.app.attendances.models.dto.AttendanceDTO;
import com.pshs.ams.app.attendances.models.dto.ClassroomDemographicsAttendances;
import com.pshs.ams.app.classrooms.models.dto.ClassroomRankingDTO;
import com.pshs.ams.global.models.custom.DateRange;
import com.pshs.ams.global.models.custom.LineChart;
import com.pshs.ams.global.models.custom.MessageResponse;
import com.pshs.ams.global.models.custom.RFIDCard;
import com.pshs.ams.app.attendances.models.enums.AttendanceMode;
import com.pshs.ams.app.attendances.models.enums.AttendanceStatus;
import com.pshs.ams.global.models.enums.CodeStatus;
import com.pshs.ams.global.models.enums.Sex;
import com.pshs.ams.global.models.enums.TimeStack;
import com.pshs.ams.global.models.enums.AttendanceForeignEntity;
import com.pshs.ams.app.attendances.services.AttendanceService;
import com.pshs.ams.app.classrooms.services.ClassroomService;
import com.pshs.ams.app.students.services.StudentService;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

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
	 * Creates or updates an attendance record
	 *
	 * @param attendance The attendance record to create or update
	 * @param override   Whether to override an existing record
	 * @return CodeStatus indicating the result of the operation
	 */
	@Override
	@Transactional
	public CodeStatus createAttendance(Attendance attendance, Boolean override) {
		if (attendance == null) {
			logger.debug("Attendance is null");
			return CodeStatus.NULL;
		}

		override = override != null && override;

		try {
			// Check if the student has the same attendance for today correlated to the date
			// and status
			Optional<Attendance> existingAttendance = Attendance.find(
				"student = ?1 and date = ?2 and status = ?3",
				attendance.getStudent(), attendance.getDate(), attendance.getStatus()
			).firstResultOptional();

			if (existingAttendance.isPresent()) {
				if (!override) {
					logger.debug("Student already has attendance for today correlated to the date and status");
					return CodeStatus.EXISTS;
				}
				logger.debug("Overriding attendance for student: " + attendance.getStudent().getId());
				Attendance.deleteById(existingAttendance.get().getId());
				Attendance.flush();
			}

			attendance.setId(null);
			attendance.persistAndFlush();
			return CodeStatus.OK;
		} catch (Exception e) {
			logger.error("Error creating attendance: " + e.getMessage(), e);
			return CodeStatus.FAILED;
		}
	}

	/**
	 * @param rfidCard
	 * @return
	 */
	@Override
	@Transactional
	public MessageResponse fromWebSocket(RFIDCard rfidCard) throws JsonProcessingException {
		if (rfidCard == null || rfidCard.getHashedLrn() == null || rfidCard.getMode() == null) {
			logger.debug("RFID Card DTO is null");
			return new MessageResponse(
				"RFID Card DTO is null",
				CodeStatus.NULL
			);
		}

		Optional<RfidCredential> rfidCredential = RfidCredential.find("hashedLrn = ?1", rfidCard.getHashedLrn())
			.firstResultOptional();
		if (rfidCredential.isEmpty()) {
			logger.debug("RFID Card not found: " + rfidCard.getHashedLrn());
			return new MessageResponse(
				"RFID Card not found",
				CodeStatus.NOT_FOUND
			);
		}

		LocalDate now = LocalDate.now();
		Attendance attendance = new Attendance();
		attendance.setStudent(rfidCredential.get().getStudent());
		attendance.setDate(LocalDate.now());
		Optional<Attendance> latestAttendanceOptional = Attendance
			.find("date = ?1 AND student.id = ?2", now, rfidCredential.get().getStudent().getId())
			.firstResultOptional();

		// * Now, what we have to do is to calculate the timeIn, timeOut, and status
		switch (rfidCard.getMode()) {
			case IN -> {
				// Check if the student has the same attendance for today correlated to the date
				// and status
				if (latestAttendanceOptional.isPresent()) {
					logger.debug("Student already has attendance for today correlated to the date and status");
					return new MessageResponse(
						"Hi " + rfidCredential.get().getStudent().getLastName() + ", welcome! :D",
						CodeStatus.EXISTS
					);
				}

				// Create attendance
				logger.debug("Creating attendance for student: " + rfidCredential.get().getStudent());
				attendance.setTimeIn(LocalTime.now());
				attendance.setStatus(getAttendanceStatus(rfidCard.getMode(), rfidCredential.get().getStudent()));
				attendance.persist();
				realTimeAttendanceService.broadcastMessage(objectMapper.writeValueAsString(
					modelMapper.map(attendance, AttendanceDTO.class)));
				return new MessageResponse(
					"Welcome " + rfidCredential.get().getStudent().getLastName() + ", you are " + attendance.getStatus().name(),
					CodeStatus.OK
				);
			}

			case OUT -> {
				// Get the latest attendance
				if (latestAttendanceOptional.isEmpty()) {
					logger.debug("No attendance found for today");
					return new MessageResponse(
						"Not Checked In",
						CodeStatus.NOT_FOUND
					);
				}

				// Get Attendance
				Attendance latestAttendance = latestAttendanceOptional.get();
				if (latestAttendance.getTimeOut() != null) {
					logger.debug("Updating time out for student");
					latestAttendance.setTimeOut(LocalTime.now());
					latestAttendance.persist();
					return new MessageResponse(
						"Time out updated",
						CodeStatus.OK
					);
				}

				// Update attendance
				logger.debug("Updating attendance for student: " + rfidCredential.get().getStudent());
				latestAttendance.setTimeOut(LocalTime.now());
				latestAttendance.persist();
				realTimeAttendanceService.broadcastMessage(objectMapper.writeValueAsString(
					modelMapper.map(latestAttendance, AttendanceDTO.class)));
				return new MessageResponse(
					"Status: " + latestAttendance.getStatus(),
					CodeStatus.OK
				);
			}

			case EXCUSED -> {
				if (latestAttendanceOptional.isEmpty()) {
					logger.debug(
						"No attendance found, when excusing student, consult to the administrator or teachers.");
					return new MessageResponse(
						"Consult admin/teachers",
						CodeStatus.OK
					);
				}
				// Update attendance
				logger.debug("Updating attendance for student: " + rfidCredential.get().getStudent());
				Attendance latestAttendance = latestAttendanceOptional.get();
				if (latestAttendance.getStatus() == AttendanceStatus.EXCUSED) {
					logger.debug("Student already has excused attendance for today correlated to the date and status");
					return new MessageResponse(
						"Already Excused",
						CodeStatus.EXISTS
					);
				}

				latestAttendance.setNotes("This student was scanned as excused.");
				latestAttendance
					.setStatus(getAttendanceStatus(rfidCard.getMode(), rfidCredential.get().getStudent()));
				latestAttendance.setTimeOut(LocalTime.now());
				latestAttendance.persist();

				realTimeAttendanceService.broadcastMessage(
					objectMapper.writeValueAsString(
						modelMapper.map(latestAttendance, AttendanceDTO.class)));
				return new MessageResponse(
					"Status: EXCUSED",
					CodeStatus.OK
				);
			}

			default -> {
				throw new Error("Invalid attendance mode: " + rfidCard.getMode());
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
	public long countTotalByAttendanceByStatus(
		List<AttendanceStatus> attendanceStatus, DateRange dateRange, Long id,
		AttendanceForeignEntity foreignEntity
	) {
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
			if (studentService.getStudent(id).isEmpty()) {
				return 0;
			}

			return Attendance.count(
				"status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.id = ?4", attendanceStatus,
				dateRange.getStartDate(), dateRange.getEndDate(), id
			);
		} else if (foreignEntity == AttendanceForeignEntity.CLASSROOM) {
			// Check if exists
			if (classroomService.getClassroom(id).isEmpty()) {
				return 0;
			}

			return Attendance.count(
				"status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.classroom.id = ?4",
				attendanceStatus,
				dateRange.getStartDate(), dateRange.getEndDate(), id
			);
		}

		return Attendance.count(
			"status IN ?1 BETWEEN ?2 AND ?3", attendanceStatus, dateRange.getStartDate(),
			dateRange.getEndDate()
		);
	}

	/**
	 * Get classroom demographics chart
	 *
	 * @param statuses  list of {@link AttendanceStatus} to filter by
	 * @param dateRange range of dates to filter by
	 * @param id        classroom id
	 * @return list of {@link ClassroomDemographicsAttendances} objects
	 */
	@Override
	public ClassroomDemographicsAttendances getClassroomAttendanceDemographicsChart(
		List<AttendanceStatus> statuses,
		DateRange dateRange, Long id
	) {
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

		long male = Attendance.count(
			"status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.classroom.id = ?4 AND cast(student.sex AS text) = ?5",
			statuses, dateRange.getStartDate(), dateRange.getEndDate(), id, Sex.MALE.name()
		);
		long female = Attendance.count(
			"status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.classroom.id = ?4 AND cast(student.sex AS text) = ?5",
			statuses, dateRange.getStartDate(), dateRange.getEndDate(), id, Sex.FEMALE.name()
		);
		return new ClassroomDemographicsAttendances(male, female);
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
		return Attendance.count(
			"status IN ?1 AND date BETWEEN ?2 AND ?3", attendanceStatus, dateRange.getStartDate(),
			dateRange.getEndDate()
		);
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
	public List<Attendance> getAllAttendanceByStatusAndDateRange(
		List<AttendanceStatus> attendanceStatus,
		DateRange dateRange, Page page, Sort sort
	) {
		return Attendance.find(
			"status IN ?1 BETWEEN ?2 AND ?3", sort, attendanceStatus, dateRange.getStartDate(),
			dateRange.getEndDate()
		).page(page).list();
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
	public List<Attendance> getAllAttendanceByStatusAndDateRange(
		List<AttendanceStatus> attendanceStatus,
		DateRange dateRange, AttendanceForeignEntity foreignEntity, Integer id, Page page, Sort sort
	) {
		// Check if parameters are valid
		if (attendanceStatus == null || attendanceStatus.isEmpty()) {
			logger.debug("Invalid attendance status provided");
			throw new IllegalArgumentException("Attendance status list cannot be null or empty");
		}
		if (dateRange == null || dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
			logger.debug("Invalid date range provided");
			throw new IllegalArgumentException("Date range cannot be null and must have both start and end dates");
		}
		if (foreignEntity == null) {
			logger.debug("Foreign entity is null");
			throw new IllegalArgumentException("Foreign entity cannot be null");
		}
		if (id == null || id <= 0) {
			logger.debug("Invalid id provided: " + id);
			throw new IllegalArgumentException("Id must be a positive integer");
		}

		sort = Sort.by("date", "timeIn", "timeOut").descending();
		logger.debug("Sorting by date, time in, and time out");
		if (foreignEntity == AttendanceForeignEntity.STUDENT) {
			return Attendance
				.find(
					"status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.id = ?4", sort, attendanceStatus,
					dateRange.getStartDate(), dateRange.getEndDate(), id
				)
				.page(page).list();
		} else if (foreignEntity == AttendanceForeignEntity.CLASSROOM) {
			return Attendance.find(
				"status IN ?1 AND date BETWEEN ?2 AND ?3 AND student.classroom.id = ?4", sort,
				attendanceStatus, dateRange.getStartDate(), dateRange.getEndDate(), id
			).page(page).list();
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
	 * @return list of {@link LineChart} objects
	 */
	@Override
	public LineChart getLineChart(
		List<AttendanceStatus> statuses, DateRange dateRange,
		AttendanceForeignEntity foreignEntity, Long id, TimeStack stack
	) {
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
			if (foreignEntity == AttendanceForeignEntity.STUDENT
				|| foreignEntity == AttendanceForeignEntity.CLASSROOM) {
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
				case WEEK -> String.format(
					"%s to %s, %s",
					startDate.format(formatter),
					endDate.format(formatter),
					startDate.format(yearFormatter)
				);
				case MONTH -> String.format(
					"%s to %s, %s",
					startDate.format(formatter),
					endDate.format(formatter),
					startDate.format(yearFormatter)
				);
				case YEAR -> String.format(
					"%s to %s",
					startDate.format(yearFormatter),
					endDate.format(yearFormatter)
				);
				default -> startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
			};

			labels.add(label);
			data.add(entry.getValue().toString());
		}

		return new LineChart(labels, data);
	}

	/**
	 * @param statuses
	 * @param dateRange
	 * @param stack
	 * @return
	 */
	@Override
	public LineChart getLineChart(List<AttendanceStatus> statuses, DateRange dateRange, TimeStack stack) {
		logger.debug("Called getLineChart, no foreign entity");
		List<String> labels = new ArrayList<>();
		List<String> data = new ArrayList<>();
		Map<LocalDate, Integer> dailyCounts = new TreeMap<>();

		LocalDate currentDate = dateRange.getStartDate();
		while (currentDate.isBefore(dateRange.getEndDate())) {
			// String date;
			LocalDate date;
			LocalDate nextDate = switch (stack) {
				case WEEK -> {
					// date = "Week " + currentDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) + ", " +
					// currentDate.getYear();
					date = currentDate.plusWeeks(1);
					yield currentDate.plusWeeks(1);
				}
				case MONTH -> {
					// date = currentDate.getMonth().toString() + ", " + currentDate.getYear();
					date = currentDate.plusMonths(1);
					yield currentDate.plusMonths(1);
				}
				default -> {
					// date = currentDate.toString();
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

		return new LineChart(labels, data);
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
	public long countAttendances(
		DateRange dateRange, List<AttendanceStatus> statuses,
		AttendanceForeignEntity foreignEntity, Integer id, List<Sex> sexes
	) {
		logger.debug("Count total attendance: " + dateRange);
		if (foreignEntity == AttendanceForeignEntity.STUDENT) {
			logger.debug("Student: " + foreignEntity);
			return Attendance.count(
				"status IN ?1 BETWEEN ?2 AND ?3 AND student.id = ?4", statuses,
				dateRange.getStartDate(),
				dateRange.getEndDate(), id
			);
		} else if (foreignEntity == AttendanceForeignEntity.CLASSROOM) {
			logger.debug("Classroom: " + foreignEntity);
			return Attendance.count(
				"status IN ?1 BETWEEN ?2 AND ?3 AND classroom.id = ?4", statuses,
				dateRange.getStartDate(), dateRange.getEndDate(), id
			);
		}

		logger.debug("All: " + dateRange);
		return Attendance.count(
			"status IN ?1 BETWEEN ?2 AND ?3", statuses, dateRange.getStartDate(),
			dateRange.getEndDate()
		);
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
		return Attendance.count(
			"status IN ?1 BETWEEN ?2 AND ?3", statuses, dateRange.getStartDate(),
			dateRange.getEndDate()
		);
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

	@Override
	public List<Attendance> getFilteredAttendances(
		DateRange dateRange, Integer classroomId, Integer gradeLevelId,
		Integer strandId, Long studentId, Page page, Sort sort
	) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder query = new StringBuilder("date BETWEEN :startDate AND :endDate");
		params.put("startDate", dateRange.getStartDate());
		params.put("endDate", dateRange.getEndDate());

		if (classroomId != null) {
			query.append(" and student.classroom.id = :classroomId");
			params.put("classroomId", classroomId);
		}

		if (gradeLevelId != null) {
			query.append(" and student.gradeLevel.id = :gradeLevelId");
			params.put("gradeLevelId", gradeLevelId);
		}

		if (strandId != null) {
			query.append(" and student.strand.id = :strandId");
			params.put("strandId", strandId);
		}

		if (studentId != null) {
			query.append(" and student.id = :studentId");
			params.put("studentId", studentId);
		}

		return Attendance.find(query.toString(), sort, params).page(page).list();
	}

	@Override
	public long countFilteredAttendances(
		DateRange dateRange, Integer classroomId, Integer gradeLevelId, Integer strandId,
		Long studentId
	) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder query = new StringBuilder("date BETWEEN :startDate AND :endDate");
		params.put("startDate", dateRange.getStartDate());
		params.put("endDate", dateRange.getEndDate());

		if (classroomId != null) {
			query.append(" AND student.classroom.id = :classroomId");
			params.put("classroomId", classroomId);
		}

		if (gradeLevelId != null) {
			query.append(" AND student.gradeLevel.id = :gradeLevelId");
			params.put("gradeLevelId", gradeLevelId);
		}

		if (strandId != null) {
			query.append(" AND student.strand.id = :strandId");
			params.put("strandId", strandId);
		}

		if (studentId != null) {
			query.append(" AND student.id = :studentId");
			params.put("studentId", studentId);
		}

		return Attendance.count(query.toString(), params);
	}

	@Override
	public List<ClassroomRankingDTO> getClassroomRanking(DateRange dateRange, Integer limit) {
		logger.debug("Classroom Ranking Date range: " + dateRange);
		// Get all classrooms
		List<Classroom> classrooms = Classroom.listAll();

		// Create list to store rankings
		List<ClassroomRankingDTO> rankings = new ArrayList<>();

		// Calculate attendance rate for each classroom
		for (Classroom classroom : classrooms) {
			// Get total number of students in classroom
			long totalStudents = classroom.getStudents().size();

			if (totalStudents > 0) { // Only include classrooms with students
				// Count total attendance for this classroom in date range
				long totalAttendance = Attendance.count(
					"student.classroom.id = ?1 AND date BETWEEN ?2 AND ?3 AND status in ?4",
					classroom.getId(), dateRange.getStartDate(), dateRange.getEndDate(),
					Arrays.asList(AttendanceStatus.ON_TIME, AttendanceStatus.LATE)
				);
				logger.debug("Total attendance: " + totalAttendance);

				// Calculate attendance rate (attendance per student)
				double attendanceRate = (double) totalAttendance / totalStudents;
				logger.debug("Attendance rate: " + attendanceRate);
				// Create ranking DTO
				ClassroomRankingDTO rankingDTO = new ClassroomRankingDTO()
					.setClassroomId(classroom.getId())
					.setClassroomName(classroom.getClassroomName())
					.setRoom(classroom.getRoom())
					.setTotalAttendance(totalAttendance)
					.setAttendanceRate(attendanceRate);
				logger.debug("Ranking: " + rankingDTO);

				rankings.add(rankingDTO);
			}
		}

		// Sort by attendance rate in descending order
		rankings.sort((a, b) -> Double.compare(b.getAttendanceRate(), a.getAttendanceRate()));

		// Assign ranks (handle ties by giving same rank)
		int currentRank = 1;
		double previousRate = -1;

		for (ClassroomRankingDTO ranking : rankings) {
			if (ranking.getAttendanceRate() != previousRate) {
				currentRank = rankings.indexOf(ranking) + 1;
			}
			ranking.setRank(currentRank);
			previousRate = ranking.getAttendanceRate();
		}

		// Limit results if specified
		if (limit != null && limit > 0) {
			return rankings.stream()
				.limit(limit)
				.collect(Collectors.toList());
		}
		logger.debug("Rankings: " + rankings);
		return rankings;
	}

	@Override
	@Transactional
	public Attendance updateAttendance(Long id, AttendanceDTO attendanceDTO) {
		Attendance attendance = Attendance.findById(id);
		if (attendance == null) {
			throw new NotFoundException("Attendance record not found");
		}

		// Update only the allowed fields
		attendance.setStatus(attendanceDTO.getStatus());
		attendance.setNotes(attendanceDTO.getNotes());
		attendance.setTimeIn(attendanceDTO.getTimeIn());
		attendance.setTimeOut(attendanceDTO.getTimeOut());
		attendance.setDate(attendanceDTO.getDate());

		// Persist changes
		attendance.persist();

		return attendance;
	}

	@Override
	public List<Student> getAbsentStudents(DateRange dateRange) {
		logger.debug("Getting absent students for date range: " + dateRange);

		if (dateRange == null || dateRange.getStartDate() == null || dateRange.getEndDate() == null) {
			logger.error("Invalid date range provided");
			throw new IllegalArgumentException("Date range cannot be null and must have both start and end dates");
		}

		// Using a subquery to find students who don't have attendance records in the
		// date range
		String query = "FROM Student s WHERE NOT EXISTS (" +
			"SELECT 1 FROM Attendance a " +
			"WHERE a.student = s " +
			"AND a.date BETWEEN ?1 AND ?2)";

		return Student.find(
				query,
				dateRange.getStartDate(),
				dateRange.getEndDate()
			)
			.list();
	}

	@Override
	public long countLastHourAttendance(List<AttendanceStatus> attendanceStatuses) throws IllegalArgumentException {
		if (attendanceStatuses == null || attendanceStatuses.isEmpty()) {
			throw new IllegalArgumentException("Attendance statuses cannot be null or empty");
		}

		// Get current time and one hour ago
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime oneHourAgo = now.minusHours(1);
		LocalDate today = now.toLocalDate();
		LocalTime currentTime = now.toLocalTime();
		LocalTime hourAgoTime = oneHourAgo.toLocalTime();

		// Count attendance records in the last hour where either timeIn or timeOut
		// falls within the range, using proper time comparison
		long total = Attendance.count(
			"status IN ?1 AND date = ?2 AND " +
				"((timeIn BETWEEN ?3 AND ?4) OR (timeOut BETWEEN ?3 AND ?4))",
			attendanceStatuses, today, hourAgoTime, currentTime
		);

		logger.debug("Total last hour attendance (between " + hourAgoTime + " and " + currentTime + "): " + total);
		return total;
	}

	@Override
	public List<Student> getLastHourAttendance(List<AttendanceStatus> attendanceStatuses)
		throws IllegalArgumentException {
		if (attendanceStatuses == null || attendanceStatuses.isEmpty()) {
			throw new IllegalArgumentException("Attendance statuses cannot be null or empty");
		}

		// Get current time and one hour ago
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime oneHourAgo = now.minusHours(1);
		LocalDate today = now.toLocalDate();
		LocalTime currentTime = now.toLocalTime();
		LocalTime hourAgoTime = oneHourAgo.toLocalTime();

		// Get attendance records in the last hour where either timeIn or timeOut
		// falls within the range, using proper time comparison
		List<Attendance> attendances = Attendance.find(
			"status IN ?1 AND date = ?2 AND " +
				"((timeIn BETWEEN ?3 AND ?4) OR (timeOut BETWEEN ?3 AND ?4))",
			attendanceStatuses, today, hourAgoTime, currentTime
		).list();

		logger.debug(
			"Total last hour attendance (between " + hourAgoTime + " and " + currentTime + "): " + attendances.size());

		// Extract and return the list of students from attendance records
		return attendances.stream()
			.map(Attendance::getStudent)
			.collect(Collectors.toList());
	}
}
