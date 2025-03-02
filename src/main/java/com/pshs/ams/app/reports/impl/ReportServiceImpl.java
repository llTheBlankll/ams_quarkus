package com.pshs.ams.app.reports.impl;

import com.pshs.ams.app.classrooms.exceptions.ClassroomNotFoundException;
import com.pshs.ams.app.reports.services.ReportService;
import com.pshs.ams.app.reports.utils.ExcelUtils;
import com.pshs.ams.app.students.services.StudentService;
import com.pshs.ams.app.students.models.entities.Student;
import com.pshs.ams.app.classrooms.models.entities.Classroom;
import com.pshs.ams.app.classrooms.services.ClassroomService;
import com.pshs.ams.app.attendances.services.AttendanceService;
import com.pshs.ams.app.attendances.models.entities.Attendance;
import com.pshs.ams.app.attendances.models.enums.AttendanceStatus;
import com.pshs.ams.global.models.custom.DateRange;
import com.pshs.ams.global.models.enums.Sex;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Page;

@ApplicationScoped
public class ReportServiceImpl implements ReportService {

	@Inject
	Logger log;

	@Inject
	StudentService studentService;

	@Inject
	AttendanceService attendanceService;

	@Inject
	ClassroomService classroomService;

	@ConfigProperty(name = "report.sf2_report")
	String sf2ReportPath;

	/**
	 * Generates an SF2 report for a specific classroom
	 * @param classroomId The ID of the classroom to generate the report for
	 */
	public void generateSF2Report(Integer classroomId) throws ClassroomNotFoundException {
		log.info("Generating SF2 Report for classroom ID: " + classroomId);

		// Get the classroom
		Classroom classroom = classroomService.get(classroomId.longValue()).orElseThrow(
				() -> new ClassroomNotFoundException("Classroom not found with ID: " + classroomId)
		);

		if (classroom == null) {
			log.error("Classroom not found with ID: " + classroomId);
			throw new IllegalArgumentException("Classroom not found with ID: " + classroomId);
		}

		// Create a new workbook
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("SF2 Report - " + classroom.getClassroomName());

		// Initial column widths (will be auto-sized at the end)
		sheet.setColumnWidth(0, 1200); // Number column
		sheet.setColumnWidth(1, 6000); // Name column wider

		// Get current month and create date range
		LocalDate today = LocalDate.now();
		YearMonth yearMonth = YearMonth.from(today);
		LocalDate startOfMonth = yearMonth.atDay(1);
		LocalDate endOfMonth = yearMonth.atEndOfMonth();
		DateRange monthDateRange = new DateRange(startOfMonth, endOfMonth);

		// Calculate school days (excluding weekends)
		int schoolDays = countSchoolDays(startOfMonth, endOfMonth);

		// Create the report structure
		createReportStructure(sheet, yearMonth, schoolDays, classroom, startOfMonth);

		// Get male and female students from the classroom sorted by last name
		List<Student> maleStudents = getStudentsByClassroomAndGenderSortedByLastName(classroomId, Sex.MALE);
		List<Student> femaleStudents = getStudentsByClassroomAndGenderSortedByLastName(classroomId, Sex.FEMALE);

		// Starting row for student data (after headers)
		int currentRow = 3;

		// Add and fill Male section
		currentRow = addGenderSection(sheet, "MALE STUDENTS", maleStudents,
				startOfMonth, endOfMonth, currentRow, true);

		// Add spacing between sections
		currentRow += 1;

		// Add and fill Female section
		currentRow = addGenderSection(sheet, "FEMALE STUDENTS", femaleStudents,
				startOfMonth, endOfMonth, currentRow, false);

		// Add spacing before totals
		currentRow += 1;

		// Calculate and add daily totals
		addDailyTotals(sheet, maleStudents.size() + femaleStudents.size(), startOfMonth, endOfMonth, currentRow);

		// Count weekdays for auto-sizing
		int weekdayCount = 0;
		for (int day = 1; day <= endOfMonth.getDayOfMonth(); day++) {
			LocalDate date = startOfMonth.plusDays(day - 1);
			if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
				weekdayCount++;
			}
		}

		// Auto-size all columns for better fit
		for (int i = 0; i <= weekdayCount + 3; i++) {
			sheet.autoSizeColumn(i);
			// Add small padding to auto-sized width
			int currentWidth = sheet.getColumnWidth(i);
			sheet.setColumnWidth(i, currentWidth + 256); // 256 = 1 character width
		}

		try {
			// Generate filename with classroom name
			String classroomName = classroom.getClassroomName().replaceAll("[^a-zA-Z0-9]", "_");
			String reportPath = sf2ReportPath.replace(".xlsx", "_" + classroomName + ".xlsx");

			// Save the report
			saveReport(workbook, reportPath);
			log.info("SF2 Report generated successfully at: " + reportPath);
		} catch (IOException e) {
			log.error("Error generating SF2 Report", e);
		}
	}

	/**
	 * Creates the basic structure of the report with headers and days
	 * @param sheet the sheet to update
	 * @param yearMonth the current year and month
	 * @param schoolDays the number of school days in the month
	 * @param classroom the classroom for which the report is being generated
	 * @param startOfMonth the first day of the month
	 */
	private void createReportStructure(XSSFSheet sheet, YearMonth yearMonth, int schoolDays, Classroom classroom, LocalDate startOfMonth) {
		// Create styles using the utility class
		CellStyle headerStyle = ExcelUtils.createHeaderStyle(sheet.getWorkbook());
		CellStyle subHeaderStyle = ExcelUtils.createSubHeaderStyle(sheet.getWorkbook());
		CellStyle infoStyle = ExcelUtils.createInfoStyle(sheet.getWorkbook());

		// Format month and year
		String monthYear = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
		int daysInMonth = yearMonth.lengthOfMonth();

		// Count weekdays (excluding weekends)
		int weekdayCount = 0;
		Map<Integer, Integer> dayToColumnMap = new HashMap<>(); // Maps actual day to column index

		for (int day = 1; day <= daysInMonth; day++) {
			LocalDate date = startOfMonth.plusDays(day - 1);
			if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
				weekdayCount++;
				dayToColumnMap.put(day, weekdayCount);
			}
		}

		// Create title row with the updated header text
		Row titleRow = sheet.createRow(0);
		titleRow.setHeightInPoints(30); // Reduced height
		XSSFCell titleCell = (XSSFCell) titleRow.createCell(0);
		titleCell.setCellValue("School Form 2 Daily Attendance Report of Learners For Senior High School (SF2-SHS)");
		titleCell.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, weekdayCount + 3));

		// Create month/year row
		Row monthRow = sheet.createRow(1);
		monthRow.setHeightInPoints(25); // Reduced height
		XSSFCell monthCell = (XSSFCell) monthRow.createCell(0);
		monthCell.setCellValue("Month: " + monthYear + " - School Days: " + schoolDays);
		monthCell.setCellStyle(subHeaderStyle);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, weekdayCount + 3));

		// Create header row for number, name and day numbers
		Row headerRow = sheet.createRow(2);
		headerRow.setHeightInPoints(20); // Reduced height

		// Create cell style for day headers with vertical text
		CellStyle dayHeaderStyle = ExcelUtils.createDayHeaderStyle(sheet.getWorkbook());

		// Number header
		XSSFCell numberCell = (XSSFCell) headerRow.createCell(0);
		numberCell.setCellValue("No.");
		numberCell.setCellStyle(subHeaderStyle);

		// Student name header
		XSSFCell nameCell = (XSSFCell) headerRow.createCell(1);
		nameCell.setCellValue("Name");
		nameCell.setCellStyle(subHeaderStyle);

		// Create day columns (only for weekdays)
		int columnIndex = 2; // Start after Name column
		for (int day = 1; day <= daysInMonth; day++) {
			LocalDate date = startOfMonth.plusDays(day - 1);
			if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
				XSSFCell dayCell = (XSSFCell) headerRow.createCell(columnIndex);
				dayCell.setCellValue(day);
				dayCell.setCellStyle(dayHeaderStyle);
				columnIndex++;
			}
		}

		// Create totals headers
		XSSFCell presentCell = (XSSFCell) headerRow.createCell(columnIndex++);
		presentCell.setCellValue("Present");
		presentCell.setCellStyle(subHeaderStyle);

		XSSFCell absentCell = (XSSFCell) headerRow.createCell(columnIndex++);
		absentCell.setCellValue("Absent");
		absentCell.setCellStyle(subHeaderStyle);

		// Add classroom information on the right side of the report
		XSSFCell classroomNameLabel = (XSSFCell) titleRow.createCell(columnIndex + 1);
		classroomNameLabel.setCellValue("Classroom:");
		classroomNameLabel.setCellStyle(infoStyle);

		XSSFCell classroomNameValue = (XSSFCell) titleRow.createCell(columnIndex + 2);
		classroomNameValue.setCellValue(classroom.getClassroomName());
		classroomNameValue.setCellStyle(infoStyle);

		// Row 1 - Teacher
		XSSFCell teacherLabel = (XSSFCell) monthRow.createCell(columnIndex + 1);
		teacherLabel.setCellValue("Teacher:");
		teacherLabel.setCellStyle(infoStyle);

		XSSFCell teacherValue = (XSSFCell) monthRow.createCell(columnIndex + 2);
		teacherValue.setCellValue(classroom.getTeacher() != null ?
			classroom.getTeacher().getLastName() + ", " + classroom.getTeacher().getFirstName() + " " +
			(classroom.getTeacher().getMiddleInitial() != null ? classroom.getTeacher().getMiddleInitial() : "") :
			"Not Assigned");
		teacherValue.setCellStyle(infoStyle);

		// Row 2 - Grade Level and Room Number
		XSSFCell gradeLevelLabel = (XSSFCell) headerRow.createCell(columnIndex + 1);
		gradeLevelLabel.setCellValue("Grade Level:");
		gradeLevelLabel.setCellStyle(infoStyle);

		XSSFCell gradeLevelValue = (XSSFCell) headerRow.createCell(columnIndex + 2);
		gradeLevelValue.setCellValue(classroom.getGradeLevel().getName() != null ? classroom.getGradeLevel().getName() : "Not Set");
		gradeLevelValue.setCellStyle(infoStyle);

		// Row 3 - Room Number (create row here if needed)
		Row roomRow = sheet.getRow(3);
		if (roomRow == null) {
			roomRow = sheet.createRow(3);
		}

		XSSFCell roomLabel = (XSSFCell) roomRow.createCell(columnIndex + 1);
		roomLabel.setCellValue("Room:");
		roomLabel.setCellStyle(infoStyle);

		XSSFCell roomValue = (XSSFCell) roomRow.createCell(columnIndex + 2);
		roomValue.setCellValue(classroom.getRoom() != null ? classroom.getRoom() : "Not Set");
		roomValue.setCellStyle(infoStyle);

		// Store the day-to-column mapping in a sheet property to use in other methods
		sheet.getWorkbook().getCreationHelper().createFormulaEvaluator()
			.evaluateAll();
	}

	/**
	 * Counts the number of school days in a given date range (excluding weekends)
	 */
	private int countSchoolDays(LocalDate startDate, LocalDate endDate) {
		int schoolDays = 0;
		LocalDate currentDate = startDate;

		while (!currentDate.isAfter(endDate)) {
			DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
			if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
				schoolDays++;
			}
			currentDate = currentDate.plusDays(1);
		}

		return schoolDays;
	}

	/**
	 * Gets students of a specific classroom and gender sorted by last name
	 * @param classroomId the classroom ID
	 * @param gender the gender to filter by
	 * @return list of students
	 */
	private List<Student> getStudentsByClassroomAndGenderSortedByLastName(Integer classroomId, Sex gender) {
		// Get all students
		List<Student> allStudents = studentService.getAllStudents(Sort.by("lastName"), Page.ofSize(1000));

		// Filter students by classroom and gender
		return allStudents.stream()
				.filter(student -> student.getClassroom() != null &&
						student.getClassroom().getId().equals(classroomId) &&
						student.getSex() == gender)
				.sorted(Comparator.comparing(Student::getLastName))
				.collect(Collectors.toList());
	}

	/**
	 * Adds a gender section with header and student data
	 */
	private int addGenderSection(XSSFSheet sheet, String sectionTitle, List<Student> students,
			LocalDate startDate, LocalDate endDate, int startRow, boolean isMale) {

		int daysInMonth = YearMonth.from(startDate).lengthOfMonth();
		int currentRow = startRow;

		// Create section header row
		Row sectionRow = sheet.createRow(currentRow++);
		sectionRow.setHeightInPoints(20); // Reduced height

		// Create gender header (only spans columns 0 and 1)
		XSSFCell sectionCell = (XSSFCell) sectionRow.createCell(0);
		sectionCell.setCellValue(sectionTitle);
		sectionCell.setCellStyle(ExcelUtils.createSectionHeaderStyle(sheet.getWorkbook(), isMale));
		sheet.addMergedRegion(new CellRangeAddress(sectionRow.getRowNum(), sectionRow.getRowNum(), 0, 1));

		// Add day abbreviations in the same row as gender header
		CellStyle dayAbbrevStyle = ExcelUtils.createDayAbbrevStyle(sheet.getWorkbook());

		// Add day abbreviations based on actual days of the week (excluding weekends)
		int columnIndex = 2; // Start after Name column
		for (int day = 1; day <= daysInMonth; day++) {
			LocalDate date = startDate.plusDays(day - 1);
			DayOfWeek dayOfWeek = date.getDayOfWeek();

			// Skip weekends
			if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
				continue;
			}

			String abbreviation = "";
			switch (dayOfWeek) {
				case MONDAY:
					abbreviation = "M";
					break;
				case TUESDAY:
					abbreviation = "T";
					break;
				case WEDNESDAY:
					abbreviation = "W";
					break;
				case THURSDAY:
					abbreviation = "TH";
					break;
				case FRIDAY:
					abbreviation = "F";
					break;
			}

			XSSFCell abbrevCell = (XSSFCell) sectionRow.createCell(columnIndex++);
			abbrevCell.setCellValue(abbreviation);
			abbrevCell.setCellStyle(dayAbbrevStyle);
		}

		// Empty cells for present and absent columns in abbreviation row
		XSSFCell emptyPresent = (XSSFCell) sectionRow.createCell(columnIndex++);
		emptyPresent.setCellStyle(dayAbbrevStyle);

		XSSFCell emptyAbsent = (XSSFCell) sectionRow.createCell(columnIndex++);
		emptyAbsent.setCellStyle(dayAbbrevStyle);

		// Create styles for the data cells
		CellStyle dataStyle = ExcelUtils.createDataStyle(sheet.getWorkbook(), isMale);
		CellStyle nameStyle = ExcelUtils.createNameStyle(sheet.getWorkbook(), isMale);
		CellStyle numberStyle = dataStyle; // Use the same data style for numbers
		CellStyle presentStyle = ExcelUtils.createPresentStyle(sheet.getWorkbook(), isMale);
		CellStyle absentStyle = ExcelUtils.createAbsentStyle(sheet.getWorkbook(), isMale);

		// Fill student data
		for (int i = 0; i < students.size(); i++) {
			Student student = students.get(i);
			Row row = sheet.createRow(currentRow++);
			row.setHeightInPoints(18); // Reduced height for compactness

			// Add student number
			XSSFCell numberCell = (XSSFCell) row.createCell(0);
			numberCell.setCellValue(i + 1);
			numberCell.setCellStyle(numberStyle);

			// Fill student name (Last Name, First Name M.I.)
			String middleInitial = student.getMiddleInitial() != null ? student.getMiddleInitial() : "";
			String fullName = student.getLastName() + ", " + student.getFirstName() + " " + middleInitial;
			XSSFCell nameCell = (XSSFCell) row.createCell(1);
			nameCell.setCellValue(fullName);
			nameCell.setCellStyle(nameStyle);

			// Get student attendance for the month
			Map<LocalDate, AttendanceStatus> studentAttendance = getStudentAttendanceForMonth(student.getId(), startDate, endDate);

			// Track present and absent counts
			int presentCount = 0;
			int absentCount = 0;

			// Fill attendance data for each day (excluding weekends)
			columnIndex = 2; // Reset column index for each student
			for (int day = 1; day <= daysInMonth; day++) {
				LocalDate date = startDate.plusDays(day - 1);

				// Skip weekends
				if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
					continue;
				}

				XSSFCell dayCell = (XSSFCell) row.createCell(columnIndex++);

				if (studentAttendance.containsKey(date)) {
					AttendanceStatus status = studentAttendance.get(date);
					if (status == AttendanceStatus.ABSENT) {
						dayCell.setCellValue("X");
						dayCell.setCellStyle(absentStyle);
						absentCount++;
					} else {
						// Present (ON_TIME, LATE, or any other status that's not ABSENT)
						dayCell.setCellValue("");
						dayCell.setCellStyle(presentStyle);
						presentCount++;
					}
				} else {
					// No attendance record for this day (default to absent)
					dayCell.setCellValue("X");
					dayCell.setCellStyle(absentStyle);
					absentCount++;
				}
			}

			// Fill present count
			XSSFCell presentCell = (XSSFCell) row.createCell(columnIndex++);
			presentCell.setCellValue(presentCount);
			presentCell.setCellStyle(dataStyle);

			// Fill absent count
			XSSFCell absentCell = (XSSFCell) row.createCell(columnIndex++);
			absentCell.setCellValue(absentCount);
			absentCell.setCellStyle(dataStyle);
		}

		return currentRow;
	}

	/**
	 * Gets a student's attendance records for a specified month
	 */
	private Map<LocalDate, AttendanceStatus> getStudentAttendanceForMonth(Long studentId, LocalDate startDate, LocalDate endDate) {
		// Create a date range for the month
		DateRange dateRange = new DateRange(startDate, endDate);

		// Get all attendance records for this student in the date range
		List<Attendance> attendances = attendanceService.getFilteredAttendances(
			dateRange, null, null, null, studentId, Page.ofSize(100), Sort.by("date")
		);

		// Map the attendance records by date
		Map<LocalDate, AttendanceStatus> attendanceMap = new HashMap<>();
		for (Attendance attendance : attendances) {
			attendanceMap.put(attendance.getDate(), attendance.getStatus());
		}

		return attendanceMap;
	}

	/**
	 * Adds daily totals row at the bottom of the report
	 */
	private void addDailyTotals(XSSFSheet sheet, int totalStudents, LocalDate startDate, LocalDate endDate, int rowNum) {
		int daysInMonth = YearMonth.from(startDate).lengthOfMonth();

		// Create totals row
		Row totalsRow = sheet.createRow(rowNum);
		totalsRow.setHeightInPoints(24); // Reduced height for compactness

		// Create cell style for totals
		CellStyle totalsStyle = ExcelUtils.createTotalsStyle(sheet.getWorkbook());

		// Empty cell for number column
		XSSFCell emptyNumberCell = (XSSFCell) totalsRow.createCell(0);
		emptyNumberCell.setCellStyle(totalsStyle);

		// Label for totals row
		XSSFCell labelCell = (XSSFCell) totalsRow.createCell(1);
		labelCell.setCellValue("DAILY TOTALS (Present)");
		labelCell.setCellStyle(totalsStyle);

		// Calculate daily totals for weekdays only
		int columnIndex = 2; // Start after Name column
		for (int day = 1; day <= daysInMonth; day++) {
			LocalDate date = startDate.plusDays(day - 1);

			// Skip weekends
			if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
				continue;
			}

			XSSFCell dayTotalCell = (XSSFCell) totalsRow.createCell(columnIndex++);
			dayTotalCell.setCellStyle(totalsStyle);

			// Count present students for this day (non-absent attendances)
			int presentCount = countDailyAttendances(date, totalStudents);
			dayTotalCell.setCellValue(presentCount);
		}

		// Add empty cells for the totals columns to maintain alignment
		XSSFCell emptyCell1 = (XSSFCell) totalsRow.createCell(columnIndex++);
		emptyCell1.setCellStyle(totalsStyle);

		XSSFCell emptyCell2 = (XSSFCell) totalsRow.createCell(columnIndex++);
		emptyCell2.setCellStyle(totalsStyle);
	}

	/**
	 * Counts the number of present students for a specific day
	 */
	private int countDailyAttendances(LocalDate date, int totalStudents) {
		// Create a date range for just this day
		DateRange dateRange = new DateRange(date, date);

		// Count attendances with status ON_TIME or LATE (present)
		List<AttendanceStatus> presentStatuses = List.of(AttendanceStatus.ON_TIME, AttendanceStatus.LATE);
		long presentCount = attendanceService.countTotalByAttendanceByStatus(presentStatuses, dateRange);

		return (int) presentCount;
	}

	/**
	 * Saves the report to the specified path
	 * @param workbook the workbook to save
	 * @param reportPath the path to save the report to
	 * @throws IOException if an I/O error occurs
	 */
	private void saveReport(XSSFWorkbook workbook, String reportPath) throws IOException {
		// Create parent directories if they don't exist
		java.nio.file.Path path = java.nio.file.Paths.get(reportPath);
		java.nio.file.Files.createDirectories(path.getParent());

		// Save the workbook
		try (FileOutputStream outputStream = new FileOutputStream(reportPath)) {
			workbook.write(outputStream);
		}
	}
}
