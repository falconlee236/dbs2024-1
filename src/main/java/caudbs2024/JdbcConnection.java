package caudbs2024;

import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

import static java.lang.System.getenv;

public class JdbcConnection {
    final private String _dbUser;
    final private String _dbPwd;
    final private String _url;
    private Connection _connection;
    private final ArrayList<String> _tableNameArr;

    JdbcConnection(){
        Map<String, String> env = getenv();
        String dbHost = env.get("DB_HOST");
        _dbUser = env.get("DB_USER");
        _dbPwd = env.get("DB_PWD");
        _url = String.format("jdbc:mysql://%s:3306/test", dbHost);
        _tableNameArr = new ArrayList<>(10);
    }

    public boolean getJDBCConnection(){
        try {
            _connection = DriverManager.getConnection(_url, _dbUser, _dbPwd);
            this.storeRelationName();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertJDBCRelation(String relationName, int numAttribute){
        try{
            PreparedStatement stmt = _connection.prepareStatement(
                    "insert into relation values (?, ?)"
            );
            stmt.setString(1, relationName);
            stmt.setInt(2, numAttribute);
            stmt.execute();
            stmt.close();
            System.out.println("Insert successful!");
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            if (e.getClass().getCanonicalName()
                    .equals("java.sql.SQLIntegrityConstraintViolationException")){
                System.out.println("duplicate primary keys");
            }
            return false;
        }
    }

    private void storeRelationName(){
        try{
            ResultSet rst;
            Statement stmt = _connection.createStatement();
            rst = stmt.executeQuery("select * from relation");

            for (int i = 0; rst.next(); i++){
                _tableNameArr.add(rst.getString(1));
            }
            stmt.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public boolean insertJDBCAttribute(String relationName, String attributeName, int length){
        try{
            PreparedStatement stmt = _connection.prepareStatement(
                    "insert into attribute values (?, ?, ?)"
            );
            stmt.setString(1, relationName);
            stmt.setString(2, attributeName);
            stmt.setInt(3, length);
            stmt.execute();
            stmt.close();
            System.out.println("Insert successful!");
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            if (e.getClass().getCanonicalName()
                    .equals("java.sql.SQLIntegrityConstraintViolationException")){
                System.out.println("duplicate primary keys");
            }
            return false;
        }
    }

    public boolean checkDuplicate(String relationName){
        return _tableNameArr.contains(relationName);
    }
}
