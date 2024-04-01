package caudbs2024;

import java.sql.*;
import java.util.HashSet;
import java.util.Map;

import static java.lang.System.getenv;

public class JdbcConnection {
    final private String _dbUser;
    final private String _dbPwd;
    final private String _url;
    private Connection _connection;
    private final HashSet<String> _tableNameArr;

    JdbcConnection(){
        Map<String, String> env = getenv();
        String dbHost = env.get("DB_HOST");
        _dbUser = env.get("DB_USER");
        _dbPwd = env.get("DB_PWD");
        _url = String.format("jdbc:mysql://%s:3306/test", dbHost);
        _tableNameArr = new HashSet<>();
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

    private void storeRelationName(){
        try{
            ResultSet rst;
            Statement stmt = _connection.createStatement();
            rst = stmt.executeQuery("select * from relation");

            while (rst.next()){
                _tableNameArr.add(rst.getString(1));
            }
            stmt.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private Relation getJDBCRelation(String relationName){
        try{
            PreparedStatement stmt = _connection.prepareStatement(
                    "select * from relation where relation_name = ?"
            );
            stmt.setString(1, relationName);
            ResultSet rst = stmt.executeQuery();
            rst.next();
            return new Relation(
                    rst.getString(1), rst.getInt(2));
        } catch (SQLException e){
            e.printStackTrace();
            return null;
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
            storeRelationName();
            return true;
        } catch (SQLException e){
            e.printStackTrace();
            if (e.getClass().getCanonicalName()
                    .equals("java.sql.SQLIntegrityConstraintViolationException")){
                System.err.println("duplicate primary keys");
            }
            return false;
        }
    }

    public Attribute[] getJDBCAttribute(String relationName){
        try{
            Relation table = getJDBCRelation(relationName);
            if (table == null){
                return null;
            }
            PreparedStatement stmt = _connection.prepareStatement(
                    "select * from attribute where relation_name = ?"
            );
            stmt.setString(1, relationName);
            ResultSet rst = stmt.executeQuery();
            Attribute[] attributeArr = new Attribute[table.attribute_num];
            for (int i = 0; rst.next(); i++){
                attributeArr[i] = new Attribute(
                        rst.getString(1),
                        rst.getString(2),
                        rst.getInt(3)
                );
            }
            return attributeArr;
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    public void insertJDBCAttribute(String relationName, String attributeName, int length){
        try{
            PreparedStatement stmt = _connection.prepareStatement(
                    "insert into attribute values (?, ?, ?)"
            );
            stmt.setString(1, relationName);
            stmt.setString(2, attributeName);
            stmt.setInt(3, length);
            stmt.execute();
            stmt.close();
        } catch (SQLException e){
            e.printStackTrace();
            if (e.getClass().getCanonicalName()
                    .equals("java.sql.SQLIntegrityConstraintViolationException")){
                System.err.println("duplicate primary keys");
            }
        }
    }

    public boolean checkDuplicate(String relationName){
        return _tableNameArr.contains(relationName);
    }
    public HashSet<String> getRelationNameArr(){
        return this._tableNameArr;
    }
}
