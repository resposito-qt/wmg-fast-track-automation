package framework.platform.utilities;


import framework.platform.ConfigProvider;

import java.sql.Connection;
import java.sql.DriverManager;

@SuppressWarnings("JpaQueryApiInspection")
public class DataBaseUtils {

    private String user = ConfigProvider.dbUser;
    private String pass = ConfigProvider.dbPass;
    private String dbUrl = ConfigProvider.dbUrl;

    private static DataBaseUtils dataBaseUtils = new DataBaseUtils();

    public static DataBaseUtils getInstance() {
        return dataBaseUtils;
    }

    private DataBaseUtils() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find the postgresql driver", e);
        }
    }

    public Connection openConnection() {
        try {
            return DriverManager.getConnection(dbUrl, user, pass);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't connect to DB", e);
        }
    }
}
