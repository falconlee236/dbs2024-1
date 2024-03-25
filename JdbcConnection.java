import javax.xml.transform.Result;
import java.sql.*;
import java.util.Map;

import static java.lang.System.getenv;

public class JdbcConnection {
    final private String _dbUser;
    final private String _dbPwd;
    final private String _url;
    private Connection _connection;

    JdbcConnection(){
        Map<String, String> env = getenv();
        String dbHost = env.get("DB_HOST");
        _dbUser = env.get("DB_USER");
        _dbPwd = env.get("DB_PWD");
        _url = String.format("jdbc:mysql://%s:3306/test", dbHost);
    }

    public boolean getConnection(){
        try {
            _connection = DriverManager.getConnection(_url, _dbUser, _dbPwd);

            Statement stmt = _connection.createStatement();
            ResultSet res = stmt.executeQuery("select * from relation");
            while (res.next()){
                System.out.printf("%s %d\n", res.getString(1), res.getInt(2));
            }
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertRelation(String relationName, int numAttribute){
        try{
            PreparedStatement stmt = _connection.prepareStatement(
                    "insert into relation values (?, ?)"
            );
            stmt.setString(1, relationName);
            stmt.setInt(2, numAttribute);
            return stmt.execute();
        } catch (SQLException e){
            e.printStackTrace();
            if (e.getClass().getCanonicalName()
                    .equals("java.sql.SQLIntegrityConstraintViolationException")){
                System.out.println("duplicate primary keys");
            }
            return false;
        }
    }

    public boolean insertAttribue(String relationName, String attributeName, int length){
        try{
            PreparedStatement stmt = _connection.prepareStatement(
                    "insert into attribute values (?, ?, ?)"
            );
            stmt.setString(1, relationName);
            stmt.setString(2, attributeName);
            stmt.setInt(3, length);
            return stmt.execute();
        } catch (SQLException e){
            e.printStackTrace();
            if (e.getClass().getCanonicalName()
                    .equals("java.sql.SQLIntegrityConstraintViolationException")){
                System.out.println("duplicate primary keys");
            }
            return false;
        }
    }
}
