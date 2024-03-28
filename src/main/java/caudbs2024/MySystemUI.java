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

        try{
            String[] attributeNameArr = new String[attributeNum];
            int[] attributeLengthArr = new int[attributeNum];
            //도중에 터지면 relation만 생기는 문제 해결
            for(int i = 0; i < attributeNum; i++){
                String idxStr = Integer.toString(i + 1);
                System.out.print(idxStr + "- attribute name: ");
                attributeNameArr[i] = sc.nextLine();
                System.out.print(idxStr + "- attribute length");
                attributeLengthArr[i] = Integer.parseInt(sc.nextLine());
            }
            if (conn.insertJDBCRelation(relationName, attributeNum)){
                for(int i = 0; i < attributeNum; i++){
                    conn.insertJDBCAttribute(
                            relationName, attributeNameArr[i], attributeLengthArr[i]);
                }
            } else {
                System.out.println("connection failed");
                return;
            }
        } catch (NumberFormatException e){
            System.out.println("please input Integer");
        }

    }
}
