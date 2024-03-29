package caudbs2024;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class MyFileIOSystem {
    final int COLUMN_SIZE = 16;
    final int BLOCK_SIZE = 300;
    final int RECORD_SIZE = 100;
    final int INIT_RECORDS = 10;
    final int NEXT_NODE_IDX = 80;
    final int LAST_IDX = RECORD_SIZE - 1;
    public void createDBFile(String relationName, Attribute[] attributes){
        Scanner sc = new Scanner(System.in);
        final int attribute_num = attributes.length;
        byte[] block = new byte[BLOCK_SIZE];

        /*
         id = 16byte (0~15),
         first = 16~31
         second = 32~47
         third = 48~63
         fourth = 64~79
         each column 16 bytes -> 최대 4개 이므로 80bytes, 0~79 byte
         80, 81, 82, 83 byte는 다음 free node idx, 99byte는 개행
         */
        try(FileOutputStream fos = new FileOutputStream(relationName + ".txt")){
            for(int k = 0; k < INIT_RECORDS + 1; k++){
//            for(int k = 0; k < 2; k++){
                byte[] buffer = new byte[RECORD_SIZE];
                if (k == 0){
                    byte[] nextNode = Integer.toString(INIT_RECORDS + 1).getBytes();
                    for(int i = 0; i < nextNode.length; i++){
                        buffer[NEXT_NODE_IDX + i] = nextNode[i];
                    }
                    buffer[LAST_IDX] = '\n';
                } else {
                    System.out.printf("%d - record\n", k);
                    for(int i = 1; i <= attribute_num; i++){
                        Attribute node = attributes[i - 1];
                        String inputStr;
                        byte[] idxStr = Integer.toString(k  + 1000).getBytes();
                        for(int j = 0; j < idxStr.length; j++){
                            buffer[j] = idxStr[j];
                        }
                        while (true){
                            System.out.printf("input %s: ", node.attribute_name);
                            inputStr = sc.nextLine();
                            if (inputStr.length() <= node.length) break;
                            System.out.println("Length overflow, Please re input");
                        }

                        byte[] tmpArr = inputStr.getBytes();
                        for(int j = 0; j < inputStr.length(); j++){
                            buffer[i * COLUMN_SIZE + j] = tmpArr[j];
                        }
                    }
                    buffer[LAST_IDX] = '\n';
                }
                for(int i = 0; i < RECORD_SIZE; i++){
                    block[(k % 3) * RECORD_SIZE + i] = buffer[i];
                }
                if (k % 3 == 2) {
                    fos.write(block);
                    block = new byte[BLOCK_SIZE];
                }
            }
            fos.write(block);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readDBFile(String relationName, Attribute[] attributes){
        final int attribute_num = attributes.length;

        try (FileInputStream fis = new FileInputStream(relationName + "txt")){

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}