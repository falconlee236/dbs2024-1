package caudbs2024;

import java.util.Scanner;

public class MyFileIOSystem {
    public void createDBFile(String relationName, Attribute[] attributes){
        Scanner sc = new Scanner(System.in);
        final int attribute_num = attributes.length;
        final int columnByteSize = 99 / attribute_num;
        byte[] block = new byte[300];
        byte[] buffer = new byte[100];

        for(int i = 0; i < attribute_num; i++){
            Attribute node = attributes[i];
            String inputStr;
            String idxStr = Integer.toString(i  + 1000);

            while (true){
                System.out.printf("input %s: ", node.attribute_name);
                inputStr = sc.nextLine();
                if (inputStr.length() <= node.length) break;
                System.out.println("Length overflow, Please re input");
            }

            byte[] tmpArr = inputStr.getBytes();
            System.out.println(tmpArr.length);
            for(int j = 0; j < inputStr.length(); j++){
                buffer[i * columnByteSize + j] = tmpArr[j];
            }
        }
    }
}