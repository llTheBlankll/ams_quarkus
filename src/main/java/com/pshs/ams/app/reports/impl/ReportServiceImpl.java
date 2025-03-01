package com.pshs.ams.app.reports.impl;

import com.pshs.ams.app.reports.services.ReportService;
import com.pshs.ams.app.students.services.StudentService;
import com.pshs.ams.global.models.enums.Sex;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ReportServiceImpl implements ReportService {

	@Inject
	Logger log;

	@Inject
	StudentService studentService;

	// Get data from application.properties Quarkus
	@ConfigProperty(name = "report.sf2_template")
	String sf2TemplatePath;

	@ConfigProperty(name = "report.sf2_report")
	String sf2ReportPath;

	@Override
	public void generateSF2Report() {
		
	}
}
