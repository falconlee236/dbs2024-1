package caudbs2024;

import java.util.Scanner;

public class MySystemUI {
    public void printMenu(){
        System.out.println("<Menu>");
        System.out.println("1. DB Create");
        System.out.println("2. DB Insert");
        System.out.println("3. DB Delete");
        System.out.println("4. DB Select all");
        System.out.println("5. DB Select one");
        System.out.println("6. Exit");
        System.out.print("Select the number: ");
    }

    public void createDB(JdbcConnection conn){
        Scanner sc = new Scanner(System.in);
        String relationName;
        int attributeNum;

        System.out.println("<Create table>");
        System.out.print("name: ");
        relationName = sc.nextLine();
        System.out.print("attribute number: ");
        attributeNum = Integer.parseInt(sc.nextLine());

        if (relationName.length() > 20){
            System.out.println("max relation name length is 20");
            return;
        } else if (conn.checkDuplicate(relationName)){
            System.out.println("duplicate relation Name");
            return;
        } else if (attributeNum < 0 || attributeNum > 4){
            System.out.println("max attribute number is 4");
            return;
        }

        if (!conn.insertJDBCRelation(relationName, attributeNum)){
            System.out.println("connection failed");
            return;
        }

        for(int i = 0;i < attributeNum; i++){
            String idxStr = Integer.toString(i + 1);
            System.out.print(idxStr + "- attribute name: ");
            String attributeName = sc.nextLine();
            System.out.print(idxStr + "- attribute length");
            int length = Integer.parseInt(sc.nextLine());
            if (!conn.insertJDBCAttribute(relationName, attributeName, length)){
                System.out.println("connection failed");
                return;
            }
        }
//        for(int i = 0; i < 10; i++){
//            String id = Integer.toString(1000 + i);
//        }
    }
}
