package caudbs2024;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
            for(int k = 0; k < 1; k++){
                byte[] buffer = new byte[RECORD_SIZE];
                if (k == 0){
                    byte[] nextNode = Integer.toString(INIT_RECORDS + 1).getBytes();
                    System.arraycopy(nextNode, 0, buffer, NEXT_NODE_IDX, nextNode.length);
                } else {
                    System.out.printf("%d - record\n", k);
                    for(int i = 1; i <= attribute_num; i++){
                        Attribute node = attributes[i - 1];
                        String inputStr;
                        byte[] idxStr = Integer.toString(k  + 1000).getBytes();
                        System.arraycopy(idxStr, 0, buffer, 0, idxStr.length);
                        while (true){
                            System.out.printf("input %s: ", node.attribute_name);
                            inputStr = sc.nextLine();
                            if (inputStr.length() <= node.length) break;
                            System.out.println("Length overflow, Please re input");
                        }

                        byte[] tmpArr = inputStr.getBytes();
                        System.arraycopy(tmpArr, 0, buffer, i * COLUMN_SIZE, inputStr.length());
                    }
                }
                buffer[LAST_IDX] = '\n';
                System.arraycopy(buffer, 0, block, (k % 3) * RECORD_SIZE, RECORD_SIZE);
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

    public ArrayList<ArrayList<String>> readDBFile(String relationName, Attribute[] attributes){
        final int attribute_num = attributes.length;
        byte[] block = new byte[BLOCK_SIZE];
        ArrayList<ArrayList<String>> res = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(relationName + ".txt")){
            while (fis.read(block) > 0){
                for(int i = 0; i < BLOCK_SIZE / RECORD_SIZE; i++){
                    ArrayList<String> recordArr = getRecordArr(block, i, attribute_num);
                    res.add(recordArr);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private ArrayList<String> getRecordArr(byte[] block, int i, int attribute_num) {
        ArrayList<String> recordArr = new ArrayList<>();
        byte[] recordBytes = Arrays.copyOfRange(
                block, i * (RECORD_SIZE), (i + 1) * RECORD_SIZE);
        for(int j = 0; j < attribute_num + 1; j++){
            String str = new String(
                    Arrays.copyOfRange(recordBytes, j * (COLUMN_SIZE), (j + 1) * COLUMN_SIZE))
                    .replace((char)0, ' ')
                    .trim();
            recordArr.add(str);
        }
        String nextIdx = new String(
                Arrays.copyOfRange(recordBytes, NEXT_NODE_IDX, NEXT_NODE_IDX + 4))
                .replace((char)0, ' ')
                .trim();
        recordArr.add(nextIdx);
        return recordArr;
    }

    public ArrayList<String> readDBFileOne(String relationName, Attribute[] attributes, String id){
        final int attribute_num = attributes.length;
        byte[] block = new byte[BLOCK_SIZE];

        try (FileInputStream fis = new FileInputStream(relationName + ".txt")){
            while (fis.read(block) > 0){
                for(int i = 0; i < BLOCK_SIZE / RECORD_SIZE; i++){
                    ArrayList<String> recordArr = getRecordArr(block, i, attribute_num);
                    if (id.equals(recordArr.get(0))){
                        return recordArr;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void insertDBFileRecord(String relationName, Attribute[] attributes, ArrayList<String> record){

    }
}