package org.example;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnector {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConnector.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Üzgünüm, config.properties dosyası bulunamadı");
                throw new RuntimeException("Yapılandırma dosyası bulunamadı");
            }
            properties.load(input);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Yapılandırma dosyası yüklenirken hata oluştu", ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
