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

    private String setMetaData(JdbcConnection conn){
        Scanner sc = new Scanner(System.in);
        String relationName;
        int attributeNum;
        System.out.print("name: ");
        relationName = sc.nextLine();
        System.out.print("attribute number: ");
        attributeNum = Integer.parseInt(sc.nextLine());

        if (relationName.length() > 16){
            System.err.println("max relation name length is 16");
            return null;
        } else if (conn.checkDuplicate(relationName)){
            System.err.println("duplicate relation Name");
            return null;
        } else if (attributeNum < 1 || attributeNum > 4){
            System.err.println("max attribute number is 4");
            return null;
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
                return relationName;
            } else {
                System.err.println("connection failed");
                return null;
            }
        } catch (NumberFormatException e){
            System.err.println("please input Integer");
            return null;
        }
    }

    public void createDB(JdbcConnection conn){
        System.out.println("<Create table>");
//        String relationName = setMetaData(conn);
//        Attribute[] attributeArr = conn.getJDBCAttribute(relationName);
        Attribute[] attributeArr = conn.getJDBCAttribute("clothes");
        if (attributeArr == null){
            System.err.println("duplicate primary keys");
            System.exit(1);
        }
        for(Attribute x : attributeArr){
            System.out.println(x.relation_name);
            System.out.println(x.attribute_name);
            System.out.println(x.length);
            System.out.println("----");
        }
        MyFileIOSystem fileIo = new MyFileIOSystem();
//        fileIo.createDBFile("clothes", attributeArr);
        fileIo.readDBFile("clothes", attributeArr);
    }
}
