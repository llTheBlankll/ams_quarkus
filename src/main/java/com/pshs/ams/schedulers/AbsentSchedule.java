package com.pshs.ams.schedulers;

import java.time.LocalDate;
import java.util.List;
import org.jboss.logging.Logger;
import com.pshs.ams.models.dto.custom.DateRange;
import com.pshs.ams.models.entities.Attendance;
import com.pshs.ams.models.entities.Student;
import com.pshs.ams.models.enums.AttendanceStatus;
import com.pshs.ams.services.interfaces.AttendanceService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AbsentSchedule {

	@Inject
	Logger logger;

	@Inject
	AttendanceService attendanceService;

	@Scheduled(cron = "59 59 23 * * ?") // Runs every day at 11:59:59 PM
	public void checkAbsent() {
		logger.info("Checking absent students");
		List<Student> absentStudents =
				attendanceService.getAbsentStudents(new DateRange(LocalDate.now(), LocalDate.now()));
		// Create absent attendance for each student
		Attendance attendance = new Attendance();
		for (Student student : absentStudents) {
			attendance.setStudent(student);
			attendance.setDate(LocalDate.now());
			attendance.setStatus(AttendanceStatus.ABSENT);
			attendanceService.createAttendance(attendance, true);
		}
	}
}
