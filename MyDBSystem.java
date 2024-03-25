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

            } else if (typeStr.equals("6")){
                break;
            }
        }
    }
}
