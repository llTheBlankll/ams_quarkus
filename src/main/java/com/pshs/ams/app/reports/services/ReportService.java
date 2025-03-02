package com.pshs.ams.app.reports.services;

import com.pshs.ams.app.classrooms.exceptions.ClassroomNotFoundException;

public interface ReportService {

	void generateSF2Report(Integer classroomId) throws ClassroomNotFoundException, IllegalArgumentException;
}
