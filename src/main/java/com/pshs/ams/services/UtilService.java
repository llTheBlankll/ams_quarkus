package com.pshs.ams.services;

import com.pshs.ams.models.enums.AttendanceStatus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UtilService {

	public static List<AttendanceStatus> statusStringToList(String attendanceList) throws Exception {
		return Arrays.stream(attendanceList.split(","))
			.map(AttendanceStatus::valueOf)
			.collect(Collectors.toList());
	}
}