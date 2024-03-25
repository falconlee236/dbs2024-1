package caudbs2024;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        System.out.println("My DB System: 2024-1 database system");
        MySystemUI systemUI = new MySystemUI();
        JdbcConnection myConn =  new JdbcConnection();
        while (true){
            systemUI.printMenu();
            Scanner sc = new Scanner(System.in);
            String typeStr = sc.nextLine();
            if (typeStr.equals("1")){
                System.out.println("Start Create DB....");
                systemUI.createDB();
            } else if (typeStr.equals("6")){
                break;
            }
        }
    }
}