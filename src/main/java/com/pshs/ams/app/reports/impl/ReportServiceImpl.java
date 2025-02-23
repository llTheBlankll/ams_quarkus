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
		log.debug("Generating SF2 report and saving to: " + sf2ReportPath);
		// Open
		try (FileInputStream fileInputStream = new FileInputStream(sf2TemplatePath)) {
			Workbook workbook = new XSSFWorkbook(fileInputStream);
			workbook.setActiveSheet(0);
			XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(0);
			copyEmptyStudentRow((int) studentService.countBySex(Sex.MALE), sheet, Sex.MALE);
			copyEmptyStudentRow((int) studentService.countBySex(Sex.FEMALE), sheet, Sex.FEMALE);
			setNumberOfSchoolDays(numberOfSchoolDays(), sheet);

			// Save the modified file
			try (FileOutputStream fos = new FileOutputStream(sf2ReportPath)) {
				workbook.write(fos);
				log.debug("SF2 report generated and saved to: " + sf2ReportPath);
			}
			workbook.close();
		} catch (FileNotFoundException e) {
			log.error("File not found: " + sf2TemplatePath);
			throw new RuntimeException(e);
		} catch (IOException e) {
			log.error("Error reading file: " + sf2TemplatePath);
			throw new RuntimeException(e);
		}
	}

	private void copyEmptyStudentRow(int numberOfRows, XSSFSheet sheet, Sex sex) {
		int sourceRowIndex = (sex == Sex.MALE) ? 17 : 19;  // Row 18
		int lastRowNum = sheet.getLastRowNum();

		// Store merged regions to copy
		List<CellRangeAddress> mergedRegionsToAdd = new ArrayList<>();
		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
			if (mergedRegion.getFirstRow() == sourceRowIndex) {
				mergedRegionsToAdd.add(mergedRegion);
			}
		}

		// Shift existing rows down
		sheet.shiftRows(sourceRowIndex + 1, lastRowNum, numberOfRows, true, false);

		// Get source row
		XSSFRow sourceRow = (XSSFRow) sheet.getRow(sourceRowIndex);

		// Create and copy first destination row
		XSSFRow destinationRow = (XSSFRow) sheet.createRow(sourceRowIndex + 1);
		destinationRow.setHeight(sourceRow.getHeight());

		// Copy all cells from column A to BP (0 to 67)
		for (int i = 0; i <= 67; i++) {
			XSSFCell sourceCell = (XSSFCell) sourceRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
			XSSFCell destinationCell = (XSSFCell) destinationRow.createCell(i);

			// Copy complete cell style
			XSSFCellStyle newCellStyle = (XSSFCellStyle) sheet.getWorkbook().createCellStyle();
			newCellStyle.cloneStyleFrom(sourceCell.getCellStyle());
			destinationCell.setCellStyle(newCellStyle);

			// Copy cell content based on its type
			switch (sourceCell.getCellType()) {
				case STRING:
					destinationCell.setCellValue(sourceCell.getStringCellValue());
					break;
				case NUMERIC:
					if (DateUtil.isCellDateFormatted(sourceCell)) {
						destinationCell.setCellValue(sourceCell.getDateCellValue());
					} else {
						destinationCell.setCellValue(sourceCell.getNumericCellValue());
					}
					break;
				case BOOLEAN:
					destinationCell.setCellValue(sourceCell.getBooleanCellValue());
					break;
				case FORMULA:
					destinationCell.setCellFormula(sourceCell.getCellFormula());
					break;
				case BLANK:
					break;
				default:
					break;
			}
		}

		// Add merged regions for first copied row
		for (CellRangeAddress region : mergedRegionsToAdd) {
			CellRangeAddress newMergedRegion = new CellRangeAddress(
				sourceRowIndex + 1,
				sourceRowIndex + 1,
				region.getFirstColumn(),
				region.getLastColumn()
			);
			sheet.addMergedRegion(newMergedRegion);
		}

		// If we need more than one row, copy additional rows
		if (numberOfRows > 1) {
			for (int i = 1; i < numberOfRows; i++) {
				XSSFRow nextRow = (XSSFRow) sheet.createRow(sourceRowIndex + 1 + i);
				nextRow.setHeight(sourceRow.getHeight());

				// Copy cells with complete styles
				for (int j = 0; j <= 67; j++) {
					XSSFCell sourceCell = (XSSFCell) sourceRow.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					XSSFCell newCell = (XSSFCell) nextRow.createCell(j);

					// Create new cell style and clone from source
					XSSFCellStyle newCellStyle = (XSSFCellStyle) sheet.getWorkbook().createCellStyle();
					newCellStyle.cloneStyleFrom(sourceCell.getCellStyle());
					newCell.setCellStyle(newCellStyle);

					// Copy content
					switch (sourceCell.getCellType()) {
						case STRING:
							newCell.setCellValue(sourceCell.getStringCellValue());
							break;
						case NUMERIC:
							if (DateUtil.isCellDateFormatted(sourceCell)) {
								newCell.setCellValue(sourceCell.getDateCellValue());
							} else {
								newCell.setCellValue(sourceCell.getNumericCellValue());
							}
							break;
						case BOOLEAN:
							newCell.setCellValue(sourceCell.getBooleanCellValue());
							break;
						case FORMULA:
							newCell.setCellFormula(sourceCell.getCellFormula());
							break;
						case BLANK:
							break;
						default:
							break;
					}
				}

				// Add merged regions for additional rows
				for (CellRangeAddress region : mergedRegionsToAdd) {
					CellRangeAddress newMergedRegion = new CellRangeAddress(
						sourceRowIndex + 1 + i,
						sourceRowIndex + 1 + i,
						region.getFirstColumn(),
						region.getLastColumn()
					);
					sheet.addMergedRegion(newMergedRegion);
				}
			}
		}
	}

	private void setNumberOfSchoolDays(Integer numberOfSchoolDays, XSSFSheet sheet) {
		XSSFCell schoolDaysCell = sheet.getRow(16).getCell(80);
		schoolDaysCell.setCellValue(numberOfSchoolDays);
	}

	private int numberOfSchoolDays() {
		return 31;
	}
}
