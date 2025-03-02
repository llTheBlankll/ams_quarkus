package com.pshs.ams.app.reports.utils;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;

/**
 * Utility class for Excel-related operations
 */
public class ExcelUtils {

    // Custom color palette (defined as byte RGB values)
    private static final byte[] PRIMARY_COLOR = new byte[] {(byte)162, (byte)148, (byte)249}; // #A294F9
    private static final byte[] SECONDARY_COLOR = new byte[] {(byte)205, (byte)193, (byte)255}; // #CDC1FF
    private static final byte[] TERTIARY_COLOR = new byte[] {(byte)229, (byte)217, (byte)242}; // #E5D9F2
    private static final byte[] BACKGROUND_COLOR = new byte[] {(byte)245, (byte)239, (byte)255}; // #F5EFFF

    // For other uses
    private static final byte[] HEADER_COLOR = new byte[] {(byte)107, (byte)91, (byte)149}; // #6B5B95
    private static final byte[] MALE_COLOR = new byte[] {(byte)154, (byte)182, (byte)216}; // #9AB6D8
    private static final byte[] FEMALE_COLOR = new byte[] {(byte)240, (byte)181, (byte)179}; // #F0B5B3
    private static final byte[] WEEKEND_COLOR = new byte[] {(byte)221, (byte)221, (byte)221}; // #DDDDDD
    private static final byte[] TOTAL_COLOR = new byte[] {(byte)255, (byte)215, (byte)0}; // #FFD700

    /**
     * Creates a style for the main header
     */
    public static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            XSSFColor color = new XSSFColor(HEADER_COLOR, null);
            xssfStyle.setFillForegroundColor(color);
        } else {
            style.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Creates a style for sub-headers
     */
    public static CellStyle createSubHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            XSSFColor color = new XSSFColor(PRIMARY_COLOR, null);
            xssfStyle.setFillForegroundColor(color);
        } else {
            style.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Creates a style for day headers (vertical text)
     */
    public static CellStyle createDayHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            XSSFColor color = new XSSFColor(SECONDARY_COLOR, null);
            xssfStyle.setFillForegroundColor(color);
        } else {
            style.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setRotation((short) 90); // Vertical text
        return style;
    }

    /**
     * Creates a style for day abbreviations
     */
    public static CellStyle createDayAbbrevStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            XSSFColor color = new XSSFColor(TERTIARY_COLOR, null);
            xssfStyle.setFillForegroundColor(color);
        } else {
            style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Creates a style for information labels and values
     */
    public static CellStyle createInfoStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            XSSFColor color = new XSSFColor(BACKGROUND_COLOR, null);
            xssfStyle.setFillForegroundColor(color);
        } else {
            style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Creates a section header style with custom color
     */
    public static CellStyle createSectionHeaderStyle(Workbook workbook, boolean isMale) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            byte[] colorBytes = isMale ? MALE_COLOR : FEMALE_COLOR;
            XSSFColor color = new XSSFColor(colorBytes, null);
            xssfStyle.setFillForegroundColor(color);
        } else {
            style.setFillForegroundColor(isMale ? IndexedColors.LIGHT_BLUE.getIndex() : IndexedColors.ROSE.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Creates a data cell style with custom color
     */
    public static CellStyle createDataStyle(Workbook workbook, boolean isMale) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            // Use custom color with lighter shade
            byte[] baseColorBytes = isMale ? MALE_COLOR : FEMALE_COLOR;
            byte[] lighterColor = new byte[] {
                (byte)Math.min(255, (baseColorBytes[0] & 0xFF) + 20),
                (byte)Math.min(255, (baseColorBytes[1] & 0xFF) + 20),
                (byte)Math.min(255, (baseColorBytes[2] & 0xFF) + 20)
            };
            XSSFColor color = new XSSFColor(lighterColor, null);
            xssfStyle.setFillForegroundColor(color);
        } else {
            style.setFillForegroundColor(isMale ? IndexedColors.PALE_BLUE.getIndex() : IndexedColors.LIGHT_TURQUOISE.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Creates a name cell style with custom color
     */
    public static CellStyle createNameStyle(Workbook workbook, boolean isMale) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setIndention((short) 1); // Add minimal indentation

        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            // Use custom color with lighter shade
            byte[] baseColorBytes = isMale ? MALE_COLOR : FEMALE_COLOR;
            byte[] lighterColor = new byte[] {
                (byte)Math.min(255, (baseColorBytes[0] & 0xFF) + 20),
                (byte)Math.min(255, (baseColorBytes[1] & 0xFF) + 20),
                (byte)Math.min(255, (baseColorBytes[2] & 0xFF) + 20)
            };
            XSSFColor color = new XSSFColor(lighterColor, null);
            xssfStyle.setFillForegroundColor(color);
        } else {
            style.setFillForegroundColor(isMale ? IndexedColors.PALE_BLUE.getIndex() : IndexedColors.LIGHT_TURQUOISE.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Create style for weekend cells
     */
    public static CellStyle createWeekendStyle(Workbook workbook, boolean isMale) {
        CellStyle style = createDataStyle(workbook, isMale);

        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            XSSFColor color = new XSSFColor(WEEKEND_COLOR, null);
            xssfStyle.setFillForegroundColor(color);
        } else {
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        }

        return style;
    }

    /**
     * Create style for present marker
     */
    public static CellStyle createPresentStyle(Workbook workbook, boolean isMale) {
        CellStyle style = createDataStyle(workbook, isMale);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFont(font);
        return style;
    }

    /**
     * Create style for absent marker
     */
    public static CellStyle createAbsentStyle(Workbook workbook, boolean isMale) {
        CellStyle style = createDataStyle(workbook, isMale);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        return style;
    }

    /**
     * Create style for totals row
     */
    public static CellStyle createTotalsStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldFont.setFontHeightInPoints((short) 11);
        style.setFont(boldFont);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        if (workbook instanceof XSSFWorkbook && style instanceof XSSFCellStyle) {
            XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
            XSSFColor color = new XSSFColor(TOTAL_COLOR, null);
            xssfStyle.setFillForegroundColor(color);
        } else {
            style.setFillForegroundColor(IndexedColors.GOLD.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
