package caudbs2024;

import java.util.ArrayList;
import java.util.HashSet;
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
                System.out.print(idxStr + "- attribute length: ");
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

    public void createDB(JdbcConnection conn, MyFileIOSystem fileIo){
        System.out.println("<Create table>");
        String relationName = setMetaData(conn);
        Attribute[] attributes = conn.getJDBCAttribute(relationName);
        if (attributes == null){
            System.err.println("duplicate primary keys");
            System.exit(1);
        }
        fileIo.createDBFile(relationName, attributes);
        System.out.println("create successful!");
    }

    public void searchDB(JdbcConnection conn, MyFileIOSystem fileIo){
        HashSet<String> curRelationList = conn.getRelationNameArr();
        Scanner sc = new Scanner(System.in);
        int i = 0;
        if (curRelationList.isEmpty()){
            System.out.println("No relation table in system");
            return;
        }
        System.out.println("select the relation table");
        for(String str : curRelationList){
            System.out.printf("%d - %s\n", i, str);
            i++;
        }
        System.out.print("input relation name: ");
        String relationName = sc.nextLine();
        if (!curRelationList.contains(relationName)){
            System.err.println("this Name doesn't contain Table List");
            return;
        }
        Attribute[] attributes = conn.getJDBCAttribute(relationName);
        if (attributes == null){
            System.err.println("duplicate primary keys");
            System.exit(1);
        }
        ArrayList<ArrayList<String>> table = fileIo.readDBFile(relationName, attributes);
        printDBTable(attributes, table);
    }

    private void printDBTable(Attribute[] attributes, ArrayList<ArrayList<String>> table){
        System.out.println("<result table>");
        System.out.printf("|%-16s", "id");
        for(Attribute node : attributes){
            System.out.printf("|%-16s", node.attribute_name);
        }
        System.out.println("|");
        for(ArrayList<String> node : table){
            if (node.get(0).isEmpty()){
                continue;
            }
            for(int i = 0; i < node.size() - 1; i++){
                System.out.printf("|%-16s", node.get(i));
            }
            System.out.println("|");
        }
    }
}
