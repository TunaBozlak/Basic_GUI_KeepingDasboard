package org.example;

import javax.swing.*;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "Lütfen Excel dosyanızı seçin. "+
                            "\nSeçtiğiniz dosyada,\nKPI adları(1.sütun),\nŞehirler(7.sütun),\nTarihler(dd.MM.yyyy ve 8.sütun)," +
                            "\nTurkcell(10.sütun),Vodafone(11.sütun),TurkTelekom(12.sütun) değerleri bulunmalı!",
                    "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Excel Dosyasını Seçin");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Dosyaları (*.xlsx, *.xls)", "xlsx", "xls"));
            int userSelection = fileChooser.showOpenDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String exceldosyaYolu = file.getAbsolutePath();
                try (Connection connection = DatabaseConnector.getConnection()) {
                    Data data = new Data(exceldosyaYolu);
                    data.verileriOku();
                    SwingUtilities.invokeLater(() -> {
                        new GUI(exceldosyaYolu);
                        JOptionPane.showMessageDialog(null, "Dosya başarıyla yüklendi ve veritabanına aktarıldı: " + exceldosyaYolu,
                                "Başarı", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Veritabanı bağlantı hatası: " + e.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Dosya yüklenirken bir hata oluştu: " + e.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Dosya seçilmedi. Uygulama kapatılıyor.",
                        "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
