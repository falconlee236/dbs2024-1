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
        label:
        while (true){
            systemUI.printMenu();
            Scanner sc = new Scanner(System.in);
            String typeStr = sc.nextLine();
            switch (typeStr) {
                case "1":
                    System.out.println("Start Create DB....");
                    systemUI.createDB(myConn, fileIo);
                    break;
                case "2":
                    System.out.println("Start Insert DB record....");
                    systemUI.insertDB(myConn, fileIo);
                    break;
                case "3":
                    System.out.println("Start Delete DB record....");
                    systemUI.deleteDB(myConn, fileIo);
                    break;
                case "4":
                    System.out.println("Start Select DB all....");
                    systemUI.searchDB(myConn, fileIo, false);
                    break;
                case "5":
                    System.out.println("Start Select DB one....");
                    systemUI.searchDB(myConn, fileIo, true);
                    break;
                case "6":
                    System.out.println("Start Join two DB....");
                    systemUI.joinDB(myConn, fileIo);
                    break;
                case "7":
                    break label;
            }
        }
    }
}