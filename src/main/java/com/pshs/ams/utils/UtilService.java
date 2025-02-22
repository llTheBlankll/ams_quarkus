package com.pshs.ams.utils;

import com.pshs.ams.app.attendances.models.enums.AttendanceStatus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UtilService {

	public static List<AttendanceStatus> statusStringToList(String attendanceList) {
		try {
			if (!attendanceList.contains(",")) {
				return List.of(AttendanceStatus.valueOf(attendanceList));
			}

			return Arrays.stream(attendanceList.split(","))
				.map(AttendanceStatus::valueOf)
				.collect(Collectors.toList());
		} catch (Exception e) {
			return null;
		}
	}
}
