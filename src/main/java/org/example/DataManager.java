package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {
    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private Connection connection;

    public DataManager() {
        try {
            this.connection = DatabaseConnector.getConnection();
        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantısı sağlanamadı: " + e.getMessage());
        }
    }

    public String[] getSortedUniqueIsimlerArray() {
        List<String> isimler = fetchUniqueIsimlerFromDatabase();
        return getSortedUniqueArray(isimler);
    }

    public String[] getSortedUniqueSehirlerArray() {
        List<String> sehirler = fetchUniqueSehirlerFromDatabase();
        return getSortedUniqueArray(sehirler);
    }

    public String[] getSortedTarihlerArray() {
        List<Date> tarihler = fetchUniqueDatesFromDatabase();
        Set<Date> uniqueTarihler = new TreeSet<>(tarihler);
        return uniqueTarihler.stream()
                .map(sdf::format)
                .toArray(String[]::new);
    }

    private List<String> fetchUniqueIsimlerFromDatabase() {
        List<String> isimler = new ArrayList<>();
        String sql = "SELECT DISTINCT kpi_name FROM exceldata";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                isimler.add(rs.getString("kpi_name"));
            }

        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: " + e.getMessage());
        }

        return isimler;
    }

    private List<String> fetchUniqueSehirlerFromDatabase() {
        List<String> sehirler = new ArrayList<>();
        String sql = "SELECT DISTINCT city FROM exceldata";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sehirler.add(rs.getString("city"));
            }

        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: " + e.getMessage());
        }

        return sehirler;
    }

    private List<Date> fetchUniqueDatesFromDatabase() {
        List<Date> tarihler = new ArrayList<>();
        String sql = "SELECT DISTINCT date FROM exceldata";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tarihler.add(rs.getDate("date"));
            }

        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: " + e.getMessage());
        }

        return tarihler;
    }

    private String[] getSortedUniqueArray(List<String> list) {
        return list.stream()
                .distinct()
                .sorted()
                .toArray(String[]::new);
    }

    public List<String> getVeriler(List<String> seciliKPI, String seciliSehir, String baslangicTarihiStr, String bitisTarihiStr) {
        Date baslangicTarihi = parseDate(baslangicTarihiStr);
        Date bitisTarihi = parseDate(bitisTarihiStr);

        List<String> dataList = new ArrayList<>();

        String sql = "SELECT * FROM exceldata WHERE kpi_name IN (" +
                String.join(",", Collections.nCopies(seciliKPI.size(), "?")) +
                ") AND city = ? AND date BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (String kpi : seciliKPI) {
                stmt.setString(index++, kpi);
            }
            stmt.setString(index++, seciliSehir);
            stmt.setDate(index++, new java.sql.Date(baslangicTarihi.getTime()));
            stmt.setDate(index++, new java.sql.Date(bitisTarihi.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double turkcellValue = rs.getDouble("turkcellValue");
                    double vodafoneValue = rs.getDouble("vodafoneValue");
                    double turktelekomValue = rs.getDouble("turktelekomValue");

                    dataList.add("Turkcell: " + turkcellValue + ", Vodafone: " + vodafoneValue + ", TurkTelekom: " + turktelekomValue);
                }
            }

        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: " + e.getMessage());
        }

        return dataList;
    }

    public Map<String, Double> getOperatorAverages(List<String> seciliKPI, String seciliSehir, String baslangicTarihiStr, String bitisTarihiStr) {
        Map<String, Integer> operatorCounts = new HashMap<>();
        operatorCounts.put("Turkcell", 0);
        operatorCounts.put("Vodafone", 0);
        operatorCounts.put("TurkTelekom", 0);

        Map<String, Double> operatorTotals = new HashMap<>();
        operatorTotals.put("Turkcell", 0.0);
        operatorTotals.put("Vodafone", 0.0);
        operatorTotals.put("TurkTelekom", 0.0);

        Date baslangicTarihi = parseDate(baslangicTarihiStr);
        Date bitisTarihi = parseDate(bitisTarihiStr);

        String sql = "SELECT * FROM exceldata WHERE kpi_name IN (" +
                String.join(",", Collections.nCopies(seciliKPI.size(), "?")) +
                ") AND city = ? AND date BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (String kpi : seciliKPI) {
                stmt.setString(index++, kpi);
            }
            stmt.setString(index++, seciliSehir);
            stmt.setDate(index++, new java.sql.Date(baslangicTarihi.getTime()));
            stmt.setDate(index++, new java.sql.Date(bitisTarihi.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double turkcellValue = rs.getDouble("turkcellValue");
                    double vodafoneValue = rs.getDouble("vodafoneValue");
                    double turktelekomValue = rs.getDouble("turktelekomValue");

                    operatorCounts.put("Turkcell", operatorCounts.get("Turkcell") + 1);
                    operatorTotals.put("Turkcell", operatorTotals.get("Turkcell") + turkcellValue);

                    operatorCounts.put("Vodafone", operatorCounts.get("Vodafone") + 1);
                    operatorTotals.put("Vodafone", operatorTotals.get("Vodafone") + vodafoneValue);

                    operatorCounts.put("TurkTelekom", operatorCounts.get("TurkTelekom") + 1);
                    operatorTotals.put("TurkTelekom", operatorTotals.get("TurkTelekom") + turktelekomValue);
                }
            }

        } catch (SQLException e) {
            System.err.println("Veritabanı hatası: " + e.getMessage());
        }

        return operatorTotals.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            int count = operatorCounts.get(entry.getKey());
                            return count > 0 ? entry.getValue() / count : Double.NaN;
                        }
                ));
    }

    private Date parseDate(String tarihStr) {
        try {
            return sdf.parse(tarihStr);
        } catch (ParseException e) {
            System.err.println("Tarih formatı hatalı: " + tarihStr);
            return new Date();
        }
    }
}

