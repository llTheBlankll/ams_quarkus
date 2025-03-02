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

	@ConfigProperty(name = "report.sf2_template")
	String sf2TemplatePath;

	@ConfigProperty(name = "report.sf2_report")
	String sf2ReportPath;

	/**
	 * Generates an SF2 report for a specific classroom
	 *
	 * @param classroomId The ID of the classroom to generate the report for
	 */
	public void generateSF2Report(Integer classroomId) throws ClassroomNotFoundException {
		log.info("Generating SF2 Report for classroom ID: " + classroomId);

		// Get the classroom
		Classroom classroom = classroomService.get(classroomId.longValue()).orElseThrow(
				() -> new ClassroomNotFoundException("Classroom not found with ID: " + classroomId));

		if (classroom == null) {
			log.error("Classroom not found with ID: " + classroomId);
			throw new IllegalArgumentException("Classroom not found with ID: " + classroomId);
		}

		// Get current month and create date range
		LocalDate today = LocalDate.now();
		YearMonth yearMonth = YearMonth.from(today);
		LocalDate startOfMonth = yearMonth.atDay(1);
		LocalDate endOfMonth = yearMonth.atEndOfMonth();
		DateRange monthDateRange = new DateRange(startOfMonth, endOfMonth);

		// Calculate school days (excluding weekends)
		int schoolDays = countSchoolDays(startOfMonth, endOfMonth);

		// Load the SF2 template instead of creating a new workbook
		XSSFWorkbook workbook;
		XSSFSheet sheet;
		try {
			// Load the template file
			java.io.FileInputStream fis = new java.io.FileInputStream(sf2TemplatePath);
			workbook = new XSSFWorkbook(fis);
			sheet = workbook.getSheetAt(0); // Get the first sheet
			fis.close();

			log.info("Successfully loaded SF2 template from: " + sf2TemplatePath);

			// Update header information in the template
			updateHeaderInformation(sheet, classroom, yearMonth, schoolDays);
		} catch (IOException e) {
			log.error("Error loading SF2 template: " + e.getMessage(), e);
			throw new RuntimeException("Failed to load SF2 template: " + e.getMessage(), e);
		}

		// Get male and female students from the classroom sorted by last name
		List<Student> maleStudents = getStudentsByClassroomAndGenderSortedByLastName(classroomId, Sex.MALE);
		List<Student> femaleStudents = getStudentsByClassroomAndGenderSortedByLastName(classroomId, Sex.FEMALE);

		// Starting row for student data (at row 19 as specified in the template)
		int currentRow = 19; // Corresponds to row 19 in Excel (1-indexed)

		// Remove gender section headers as requested and directly fill student data
		currentRow = fillStudentData(sheet, maleStudents, startOfMonth, endOfMonth, currentRow);

		// Add male totals
		currentRow = addGenderTotals(sheet, "MALE DAILY TOTALS (Present)", maleStudents.size(), startOfMonth, endOfMonth, currentRow);

		// Remove spacing between sections

		// Fill female students data
		currentRow = fillStudentData(sheet, femaleStudents, startOfMonth, endOfMonth, currentRow);

		// Add female totals
		currentRow = addGenderTotals(sheet, "FEMALE DAILY TOTALS (Present)", femaleStudents.size(), startOfMonth, endOfMonth, currentRow);

		// Remove spacing and total totals section

		// Add borders to all cells in additional columns (AE onwards) for all student and total rows
		addBordersToAdditionalColumns(sheet, 19, currentRow - 1);

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
	 *
	 * @param classroomId the classroom ID
	 * @param gender      the gender to filter by
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
	 * Fills student data into the template at the specified row
	 *
	 * @param sheet      the sheet to update
	 * @param students   the list of students
	 * @param startDate  the start date of the month
	 * @param endDate    the end date of the month
	 * @param startRow   the starting row index (0-based)
	 * @return the next available row index
	 */
	private int fillStudentData(XSSFSheet sheet, List<Student> students, LocalDate startDate, LocalDate endDate,
			int startRow) {
		int daysInMonth = YearMonth.from(startDate).lengthOfMonth();
		int currentRow = startRow;

		// Fill student data
		for (int i = 0; i < students.size(); i++) {
			Student student = students.get(i);
			// Excel uses 1-based indexing, but POI uses 0-based. Subtract 1 to convert.
			Row row = sheet.getRow(currentRow - 1); // Convert to 0-based index
			if (row == null) {
				row = sheet.createRow(currentRow - 1);
			}

			// Add student number in column B (index 1)
			XSSFCell numberCell = (XSSFCell) row.getCell(1);
			if (numberCell == null) {
				numberCell = (XSSFCell) row.createCell(1);
			}
			numberCell.setCellValue(i + 1);
			addBlackBorder(numberCell);

			// Fill student name in column C (index 2)
			XSSFCell nameCell = (XSSFCell) row.getCell(2);
			if (nameCell == null) {
				nameCell = (XSSFCell) row.createCell(2);
			}
			String middleInitial = student.getMiddleInitial() != null ? student.getMiddleInitial() : "";
			String fullName = student.getLastName() + ", " + student.getFirstName() + " " + middleInitial;
			nameCell.setCellValue(fullName);
			addBlackBorder(nameCell);

			// Get student attendance for the month
			Map<LocalDate, AttendanceStatus> studentAttendance = getStudentAttendanceForMonth(student.getId(),
					startDate, endDate);

			// Fill attendance data for each day starting at column D (index 3)
			int columnIndex = 3;
            // First make sure to add borders to all potential day cells (D-Z, AA-AD)
            // even if they don't contain attendance data
            for (int dayColIndex = 3; dayColIndex <= 29; dayColIndex++) {
                XSSFCell dayCell = (XSSFCell) row.getCell(dayColIndex);
                if (dayCell == null) {
                    dayCell = (XSSFCell) row.createCell(dayColIndex);
                }
                addBlackBorder(dayCell);
            }

			for (int day = 1; day <= daysInMonth; day++) {
				LocalDate date = startDate.plusDays(day - 1);

				// Skip weekends
				if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
					continue;
				}

				XSSFCell dayCell = (XSSFCell) row.getCell(columnIndex);
				if (dayCell == null) {
					dayCell = (XSSFCell) row.createCell(columnIndex);
				}

				if (studentAttendance.containsKey(date)) {
					AttendanceStatus status = studentAttendance.get(date);
					if (status == AttendanceStatus.ABSENT) {
						dayCell.setCellValue("X"); // Mark absent with X
					} else {
						// Present (ON_TIME, LATE, or any other status that's not ABSENT)
						dayCell.setCellValue(""); // Leave cell empty for present
					}
				} else {
					// No attendance record for this day (default to absent)
					dayCell.setCellValue("X");
				}
				addBlackBorder(dayCell);
				columnIndex++;
			}

			// Set formulas for absent and present counts instead of calculating values
			// Column AC (index 28) - Absent count - counts cells with "X"
			XSSFCell absentCell = (XSSFCell) row.getCell(28);
			if (absentCell == null) {
				absentCell = (XSSFCell) row.createCell(28);
			}

			// Create a formula to count cells with "X" in the range D to AB columns
			String startColumn = getExcelColumnName(3); // Column D
			String endColumn = getExcelColumnName(27); // Column AB or whatever is the last day column
			String formula = "COUNTIF(" + startColumn + currentRow + ":" + endColumn + currentRow + ",\"X\")";
			absentCell.setCellFormula(formula);
			addBlackBorder(absentCell);

			// Column AD (index 29) - Present count - counts school days minus absent days
			XSSFCell presentCell = (XSSFCell) row.getCell(29);
			if (presentCell == null) {
				presentCell = (XSSFCell) row.createCell(29);
			}

			// Count school days minus absent days
			int schoolDays = countSchoolDays(startDate, endDate);
			formula = schoolDays + "-AC" + currentRow;
			presentCell.setCellFormula(formula);
			addBlackBorder(presentCell);

			currentRow++;
		}

		return currentRow;
	}

	/**
	 * Gets a student's attendance records for a specified month
	 */
	private Map<LocalDate, AttendanceStatus> getStudentAttendanceForMonth(Long studentId, LocalDate startDate,
			LocalDate endDate) {
		// Create a date range for the month
		DateRange dateRange = new DateRange(startDate, endDate);

		// Get all attendance records for this student in the date range
		List<Attendance> attendances = attendanceService.getFilteredAttendances(
				dateRange, null, null, null, studentId, Page.ofSize(100), Sort.by("date"));

		// Map the attendance records by date
		Map<LocalDate, AttendanceStatus> attendanceMap = new HashMap<>();
		for (Attendance attendance : attendances) {
			attendanceMap.put(attendance.getDate(), attendance.getStatus());
		}

		return attendanceMap;
	}

	/**
	 * Add gender-specific totals (male or female) to the sheet
	 *
	 * @param sheet      the sheet to update
	 * @param label      the label for the totals row
	 * @param count      the number of students in this gender
	 * @param startDate  the start date of the month
	 * @param endDate    the end date of the month
	 * @param rowNum     the row where to add the totals
	 * @return the next available row index
	 */
	private int addGenderTotals(XSSFSheet sheet, String label, int count, LocalDate startDate, LocalDate endDate, int rowNum) {
		int daysInMonth = YearMonth.from(startDate).lengthOfMonth();

		// Get or create gender totals row
		Row totalsRow = sheet.getRow(rowNum - 1); // Convert to 0-based index
		if (totalsRow == null) {
			totalsRow = sheet.createRow(rowNum - 1);
		}

		// Empty cell for column B
		XSSFCell emptyCell = (XSSFCell) totalsRow.getCell(1);
		if (emptyCell == null) {
			emptyCell = (XSSFCell) totalsRow.createCell(1);
		}
		emptyCell.setCellValue("");
		addBlackBorder(emptyCell);

		// Label for gender totals row in column C
		XSSFCell labelCell = (XSSFCell) totalsRow.getCell(2);
		if (labelCell == null) {
			labelCell = (XSSFCell) totalsRow.createCell(2);
		}
		labelCell.setCellValue(label);
		addBlackBorder(labelCell);

		// Find the rows where this gender's data starts and ends
		int startRow = rowNum - count;
		int endRow = rowNum - 1;

		// First make sure to add borders to all potential day cells (D-Z, AA-AD)
		// and set default value of 0 for days without attendance data
		for (int dayColIndex = 3; dayColIndex <= 29; dayColIndex++) {
			XSSFCell dayCell = (XSSFCell) totalsRow.getCell(dayColIndex);
			if (dayCell == null) {
				dayCell = (XSSFCell) totalsRow.createCell(dayColIndex);
			}
			// Set default value of 0 for this cell
			dayCell.setCellValue(0);
			addBlackBorder(dayCell);
		}

		// Also add border to column AE for this row
		XSSFCell aeCell = (XSSFCell) totalsRow.getCell(30); // Column AE (index 30)
		if (aeCell == null) {
			aeCell = (XSSFCell) totalsRow.createCell(30);
		}
		addBlackBorder(aeCell);

		// Calculate daily totals for weekdays only starting at column D (index 3)
		int columnIndex = 3;
		for (int day = 1; day <= daysInMonth; day++) {
			LocalDate date = startDate.plusDays(day - 1);

			// Skip weekends
			if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
				continue;
			}

			XSSFCell dayTotalCell = (XSSFCell) totalsRow.getCell(columnIndex);
			if (dayTotalCell == null) {
				dayTotalCell = (XSSFCell) totalsRow.createCell(columnIndex);
			}

			// Calculate present students using a formula that counts non-X cells
			String column = getExcelColumnName(columnIndex);
			String formula = count + "-COUNTIF(" + column + startRow + ":" + column + endRow + ",\"X\")";
			dayTotalCell.setCellFormula(formula);
			addBlackBorder(dayTotalCell);

			columnIndex++;
		}

		// Total absent for this gender in column AC (index 28)
		XSSFCell totalAbsentCell = (XSSFCell) totalsRow.getCell(28);
		if (totalAbsentCell == null) {
			totalAbsentCell = (XSSFCell) totalsRow.createCell(28);
		}
		totalAbsentCell.setCellFormula("SUM(AC" + startRow + ":AC" + endRow + ")");
		addBlackBorder(totalAbsentCell);

		// Total present for this gender in column AD (index 29)
		XSSFCell totalPresentCell = (XSSFCell) totalsRow.getCell(29);
		if (totalPresentCell == null) {
			totalPresentCell = (XSSFCell) totalsRow.createCell(29);
		}
		totalPresentCell.setCellFormula("SUM(AD" + startRow + ":AD" + endRow + ")");
		addBlackBorder(totalPresentCell);

		return rowNum + 1;
	}

	/**
	 * Adds black borders to all cells in column AE
	 * from startRow to endRow
	 *
	 * @param sheet     the sheet to update
	 * @param startRow  the starting Excel row (1-indexed)
	 * @param endRow    the ending Excel row (1-indexed)
	 */
	private void addBordersToAdditionalColumns(XSSFSheet sheet, int startRow, int endRow) {
		// Add borders to column AE only
		int colIndex = 30; // Column AE (0-indexed)

		for (int rowIndex = startRow - 1; rowIndex <= endRow - 1; rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row == null) {
				row = sheet.createRow(rowIndex);
			}

			XSSFCell cell = (XSSFCell) row.getCell(colIndex);
			if (cell == null) {
				cell = (XSSFCell) row.createCell(colIndex);
			}
			addBlackBorder(cell);
		}
	}

	/**
	 * Adds a black border to a cell
	 *
	 * @param cell the cell to add borders to
	 */
	private void addBlackBorder(XSSFCell cell) {
		CellStyle style = cell.getCellStyle();
		if (style == null) {
			style = cell.getSheet().getWorkbook().createCellStyle();
		} else {
			// We need to clone the style to modify it
			style = cell.getSheet().getWorkbook().createCellStyle();
			style.cloneStyleFrom(cell.getCellStyle());
		}

		// Add black borders on all sides
		style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);

		cell.setCellStyle(style);
	}

	/**
	 * Converts a 0-based column index to Excel column name (A, B, C, ... Z, AA, AB, etc.)
	 *
	 * @param columnIndex the 0-based column index
	 * @return the Excel column name
	 */
	private String getExcelColumnName(int columnIndex) {
		StringBuilder columnName = new StringBuilder();

		while (columnIndex >= 0) {
			int remainder = columnIndex % 26;
			columnName.insert(0, (char) ('A' + remainder));
			columnIndex = (columnIndex / 26) - 1;
		}

		return columnName.toString();
	}

	/**
	 * Updates the header information in the template (classroom details, month, school days, etc.)
	 *
	 * @param sheet       the sheet to update
	 * @param classroom   the classroom details
	 * @param yearMonth   the current year and month
	 * @param schoolDays  the number of school days
	 */
	private void updateHeaderInformation(XSSFSheet sheet, Classroom classroom, YearMonth yearMonth, int schoolDays) {
		// Format month and year
		String monthYear = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

		// Specific cells in the template may vary - assuming these positions based on common SF2 templates
		// Get or create row for month/year (commonly in row 2 or 3)
		Row monthRow = sheet.getRow(2);
		if (monthRow != null) {
			// Find cell that typically shows month/year and update it
			XSSFCell monthCell = findCellInRow(monthRow, "Month:");
			if (monthCell != null) {
				monthCell.setCellValue("Month: " + monthYear + " - School Days: " + schoolDays);
			}
		}

		// Update classroom information (commonly in rows 3-7)
		updateCellValueIfFound(sheet, "Classroom:", classroom.getClassroomName());

		// Update teacher information
		String teacherName = classroom.getTeacher() != null ?
			classroom.getTeacher().getLastName() + ", " +
			classroom.getTeacher().getFirstName() + " " +
			(classroom.getTeacher().getMiddleInitial() != null ? classroom.getTeacher().getMiddleInitial() : "") :
			"Not Assigned";
		updateCellValueIfFound(sheet, "Teacher:", teacherName);

		// Update grade level
		String gradeLevel = classroom.getGradeLevel() != null && classroom.getGradeLevel().getName() != null ?
			classroom.getGradeLevel().getName() : "Not Set";
		updateCellValueIfFound(sheet, "Grade Level:", gradeLevel);

		// Update room
		String room = classroom.getRoom() != null ? classroom.getRoom() : "Not Set";
		updateCellValueIfFound(sheet, "Room:", room);
	}

	/**
	 * Helper method to find a cell in a row that contains specific text
	 *
	 * @param row  the row to search in
	 * @param text the text to search for
	 * @return the cell if found, null otherwise
	 */
	private XSSFCell findCellInRow(Row row, String text) {
		for (int i = 0; i < row.getLastCellNum(); i++) {
			XSSFCell cell = (XSSFCell) row.getCell(i);
			if (cell != null && cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING
					&& cell.getStringCellValue().contains(text)) {
				return cell;
			}
		}
		return null;
	}

	/**
	 * Updates a cell value if a matching label cell is found in the sheet
	 *
	 * @param sheet      the sheet to search in
	 * @param labelText  the label text to search for
	 * @param value      the value to set in the adjacent cell
	 */
	private void updateCellValueIfFound(XSSFSheet sheet, String labelText, String value) {
		// Search rows that typically contain header information (rows 0-10)
		for (int rowIndex = 0; rowIndex <= 10; rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row != null) {
				XSSFCell labelCell = findCellInRow(row, labelText);
				if (labelCell != null) {
					// Assume the value cell is next to the label cell
					int labelColIndex = labelCell.getColumnIndex();
					XSSFCell valueCell = (XSSFCell) row.getCell(labelColIndex + 1);
					if (valueCell == null) {
						valueCell = (XSSFCell) row.createCell(labelColIndex + 1);
					}
					valueCell.setCellValue(value);
					return;
				}
			}
		}
	}

	/**
	 * Saves the report to the specified path
	 *
	 * @param workbook   the workbook to save
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
