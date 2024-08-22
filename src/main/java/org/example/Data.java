package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Data {

    private final String dosyaYolu;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public Data(String dosyaYolu) {
        this.dosyaYolu = dosyaYolu;
    }

    public void verileriOku() {
        ExcelReader excelReader = new ExcelReader();

        try (FileInputStream fis = new FileInputStream(dosyaYolu);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row != null) {
                    String KPI = excelReader.getCellValueAsString(row.getCell(0));
                    String sehir = excelReader.getCellValueAsString(row.getCell(6));
                    Date tarih = excelReader.getCellValueAsDate(row.getCell(7));
                    double turkcellValue = getCellValueAsDouble(row, 9);
                    double vodafoneValue = getCellValueAsDouble(row, 10);
                    double turktelekomValue = getCellValueAsDouble(row, 11);

                    saveToDatabase(KPI, sehir, tarih, turkcellValue, vodafoneValue, turktelekomValue);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double getCellValueAsDouble(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell != null) {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    try {
                        return parseDouble(cell.getStringCellValue());
                    } catch (NumberFormatException e) {
                        System.err.println("Sayısal format hatası: " + cell.getStringCellValue());
                    }
                default:
                    break;
            }
        }
        return 0.0;
    }

    private double parseDouble(String value) throws NumberFormatException {
        NumberFormat format = DecimalFormat.getInstance(Locale.getDefault());
        try {
            return format.parse(value).doubleValue();
        } catch (ParseException e) {
            System.err.println("Çift format hatası: " + value);
            return 0.0;
        }
    }

    /*private void saveToDatabase(String kpiName, String city, Date date, double turkcellValue, double vodafoneValue, double turktelekomValue) {
        String insertSQL = "INSERT INTO exceldata (kpi_name, city, date, turkcellValue, vodafoneValue, turktelekomValue) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setString(1, kpiName);
            pstmt.setString(2, city);
            pstmt.setDate(3, new java.sql.Date(date.getTime()));
            pstmt.setDouble(4, turkcellValue);
            pstmt.setDouble(5, vodafoneValue);
            pstmt.setDouble(6, turktelekomValue);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: " + e.getMessage());
        }
    }*/

    private void saveToDatabase(String kpiName, String city, Date date, double turkcellValue, double vodafoneValue, double turktelekomValue) {
        String checkSQL = "SELECT COUNT(*) FROM exceldata WHERE kpi_name = ? AND city = ? AND date = ?";
        String insertSQL = "INSERT INTO exceldata (kpi_name, city, date, turkcellValue, vodafoneValue, turktelekomValue) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSQL);
             PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

            checkStmt.setString(1, kpiName);
            checkStmt.setString(2, city);
            checkStmt.setDate(3, new java.sql.Date(date.getTime()));

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count > 0) {
                        System.out.println("Bu kayıt zaten mevcut: KPI Adı = " + kpiName + ", Şehir = " + city + ", Tarih = " + new java.sql.Date(date.getTime()));
                    } else {
                        insertStmt.setString(1, kpiName);
                        insertStmt.setString(2, city);
                        insertStmt.setDate(3, new java.sql.Date(date.getTime()));
                        insertStmt.setDouble(4, turkcellValue);
                        insertStmt.setDouble(5, vodafoneValue);
                        insertStmt.setDouble(6, turktelekomValue);
                        System.out.println("Yeni kayıt eklendi: KPI Adı = " + kpiName + ", Şehir = " + city + ", Tarih = " + new java.sql.Date(date.getTime()));

                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: " + e.getMessage());
        }
    }
}
