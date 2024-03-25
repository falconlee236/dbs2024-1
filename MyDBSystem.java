import java.sql.*;
import java.util.Map;
import java.util.Scanner;

import static java.lang.System.getenv;

public class MyDBSystem {
    public static void main(String[] argv)  {
        System.out.println("My DB System: 2024-1 database system");
        MySystemUI systemUI = new MySystemUI();
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
