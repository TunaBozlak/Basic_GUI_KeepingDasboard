package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelReader {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public List<String> getKPI(String dosyaYolu) {
        return getColumnData(dosyaYolu, 0);
    }

    public List<String> getCities(String dosyaYolu) {
        return getColumnData(dosyaYolu, 6);
    }

    public List<Date> getDates(String dosyaYolu) {
        List<Date> dates = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(dosyaYolu));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Date date = getCellValueAsDate(row.getCell(7));
                    if (date != null) {
                        dates.add(date);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Dosya okuma hatası: " + e.getMessage());
        }
        return dates;
    }

    private List<String> getColumnData(String dosyaYolu, int columnIndex) {
        List<String> data = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(dosyaYolu));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String value = getCellValueAsString(row.getCell(columnIndex));
                    if (value != null && !value.trim().isEmpty()) {
                        data.add(value);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Dosya okuma hatası: " + e.getMessage());
        }
        return data;
    }

    public String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return dateFormat.format(cell.getDateCellValue());
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    public Date getCellValueAsDate(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                try {
                    return dateFormat.parse(cell.getStringCellValue());
                } catch (ParseException e) {
                    System.err.println("Tarih format hatası: " + cell.getStringCellValue());
                    return null;
                }
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    System.err.println("Beklenmeyen sayısal tarih formatı: " + cell.getNumericCellValue());
                    return null;
                }
            default:
                return null;
        }
    }
}
