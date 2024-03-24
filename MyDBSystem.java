import java.sql.*;
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
            PreparedStatement pstmt = connection.prepareStatement(
                    "insert into relation values (?, ?)"
            );
            pstmt.setString(1, "clothes");
            pstmt.setInt(2, 4);
            pstmt.execute();

            Statement stmt = connection.createStatement();
            ResultSet res = stmt.executeQuery("select * from relation");
            while (res.next()){
                System.out.printf("%s %d\n", res.getString(1), res.getInt(2));
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
