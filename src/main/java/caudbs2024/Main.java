package caudbs2024;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("My DB System: 2024-1 database system");
        MySystemUI systemUI = new MySystemUI();
        JdbcConnection myConn =  new JdbcConnection();
        MyFileIOSystem fileIo = new MyFileIOSystem();
        if (!myConn.getJDBCConnection()){
            System.out.println("Cannot connect mySQL database, check DB server");
            return;
        }
        while (true){
            systemUI.printMenu();
            Scanner sc = new Scanner(System.in);
            String typeStr = sc.nextLine();
            if (typeStr.equals("1")){
                System.out.println("Start Create DB....");
                systemUI.createDB(myConn, fileIo);
            } else if (typeStr.equals("4")){
                System.out.println("4. DB Select all...");
                systemUI.searchDB(myConn, fileIo);
            } else if (typeStr.equals("5")) {
                System.out.println("5. DB Select one...");
            } else if (typeStr.equals("6")){
                break;
            }
        }
    }
}