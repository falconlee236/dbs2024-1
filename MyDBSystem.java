import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import static java.lang.System.getenv;

public class MyDBSystem {
    public static void main(String[] argv)  {
        Map<String, String> env = getenv();
        String dbHost = env.get("DB_HOST");
        String dbUser = env.get("DB_USER");
        String dbPwd = env.get("DB_PWD");
        String url = String.format("jdbc:mysql://%s:3306/test", dbHost);

        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPwd)){
            System.out.println(connection);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
