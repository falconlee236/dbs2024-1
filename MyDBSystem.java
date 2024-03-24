import java.sql.*;
import java.util.Map;
import java.util.Scanner;

import static java.lang.System.getenv;

public class MyDBSystem {
    public static void main(String[] argv)  {
        System.out.println("My DB System: 2024-1 database system");
        while (true){
            System.out.println("<Menu>");
            System.out.println("1. DB Create");
            System.out.println("2. DB Insert");
            System.out.println("3. DB Delete");
            System.out.println("4. DB Select all");
            System.out.println("5. DB Select one");
            System.out.println("6. Exit");
            System.out.print("Select the number: ");
            Scanner sc = new Scanner(System.in);
            String typeStr = sc.nextLine();

            if (typeStr.equals("1")){
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
                    if (e.getClass().getCanonicalName()
                            .equals("java.sql.SQLIntegrityConstraintViolationException")){
                        System.out.println("duplicate primary keys");
                    }
                }
            } else if (typeStr.equals("6")){
                break;
            }
        }
    }
}
