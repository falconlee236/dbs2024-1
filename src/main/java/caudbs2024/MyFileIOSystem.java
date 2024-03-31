package caudbs2024;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class MyFileIOSystem {
    final int COLUMN_SIZE = 16;
    final int BLOCK_SIZE = 300;
    final int RECORD_SIZE = 100;
    final int BLOCK_FACTOR = BLOCK_SIZE / RECORD_SIZE;
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
                for(int i = 0; i < BLOCK_FACTOR; i++){
                    ArrayList<String> recordArr = getRecordArr(block, i, attribute_num);
                    res.add(recordArr);
                }
                Arrays.fill(block, (byte)0);
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
                for(int i = 0; i < BLOCK_FACTOR; i++){
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

    public void insertDBFileRecord(String relationName, Attribute[] attributes, ArrayList<String> record) {
        final int attribute_num = attributes.length;
        byte[] block = new byte[BLOCK_SIZE];

        try (RandomAccessFile raf = new RandomAccessFile(relationName + ".txt", "rw")) {
            while (raf.read(block) > 0) {
                for (int i = 0; i < BLOCK_FACTOR; i++) {
                    ArrayList<String> recordArr = getRecordArr(block, i, attribute_num);
                    //head 찾기, 반드시 insert는 head가 가리키는 곳에만 넣으면 된다.
                    if (recordArr.get(0).isEmpty() && !recordArr.get(attribute_num + 1).isEmpty()) {
                        // 삽입 index 찾기
                        int insertPos = Integer.parseInt(recordArr.get(attribute_num + 1));
                        // 삽입 index가 있는 block으로 이동
                        raf.seek(insertPos / BLOCK_FACTOR);
                        // 새로운 record만들기
                        byte[] blockBuffer = new byte[300];
                        byte[] insertPosBytes = Integer.toString(insertPos + 1000).getBytes();
                        System.arraycopy(insertPosBytes, 0, blockBuffer, 0, insertPosBytes.length);
                        for (int j = 0; j < record.size(); j++) {
                            byte[] strBytes = record.get(j).getBytes();
                            System.arraycopy(strBytes, 0, blockBuffer,
                                    (j + 1) * COLUMN_SIZE, strBytes.length);
                        }

                        byte[] tmpBlockBuffer = new byte[300];
                        raf.seek((long) insertPos * RECORD_SIZE);
                        raf.read(tmpBlockBuffer);
                        System.arraycopy(tmpBlockBuffer, RECORD_SIZE,
                                blockBuffer, RECORD_SIZE, BLOCK_SIZE - RECORD_SIZE);
                        blockBuffer[LAST_IDX] = '\n';
                        blockBuffer[RECORD_SIZE + LAST_IDX] = '\n';
                        blockBuffer[2 * RECORD_SIZE + LAST_IDX] = '\n';
                        raf.seek((long) insertPos * RECORD_SIZE);
                        raf.write(blockBuffer);

                        if (tmpBlockBuffer[NEXT_NODE_IDX] != 0){
                            System.arraycopy(tmpBlockBuffer, NEXT_NODE_IDX,
                                    block, NEXT_NODE_IDX, RECORD_SIZE - NEXT_NODE_IDX);
                        } else {
                            byte[] tmpBuffer = new byte[4];
                            byte[] nextPos = Integer.toString(insertPos + 1).getBytes();
                            System.arraycopy(nextPos, 0,
                                    tmpBuffer, 0, nextPos.length);
                            System.arraycopy(tmpBuffer, 0,
                                    block, NEXT_NODE_IDX, tmpBuffer.length);
                        }
                        raf.seek(0);
                        raf.write(block);
                        System.out.println("Insert Successful!");
                        return;
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void deleteDBFileRecord(String relationName, Attribute[] attributes, String id){
        final int attribute_num = attributes.length;
        byte[] block = new byte[BLOCK_SIZE];

        try (RandomAccessFile raf = new RandomAccessFile(relationName + ".txt", "rw")){
            // 1. 삭제 index 찾기
            int deleteIdx = -1;
            for (int k = 0; raf.read(block) > 0; k++){
                for(int i = 0; i < BLOCK_FACTOR; i++){
                    ArrayList<String> recordArr = getRecordArr(block, i, attribute_num);
                    if (id.equals(recordArr.get(0))){
                        deleteIdx = k * BLOCK_FACTOR + i;
                        break;
                    }
                }
                if (deleteIdx != -1) break;
            }
            if (deleteIdx == -1){
                System.err.println("That id doesn't exist in table");
                return;
            }
            // 2. 해당 index 이전 빈 block 찾기
            Arrays.fill(block, (byte) 0);
            for(int k = (deleteIdx / BLOCK_FACTOR) * BLOCK_SIZE; k >= 0; k -= BLOCK_SIZE){
                raf.seek(k);
                raf.read(block);
                for(int i = 0; i < BLOCK_FACTOR; i++){
                    ArrayList<String> recordArr = getRecordArr(block, i, attribute_num);
                    if (!recordArr.get(attribute_num + 1).isEmpty()){
                        // 3. 이전 block이 가리키고 있는 index 저장
                        int prevIdx = Integer.parseInt(recordArr.get(attribute_num + 1));


                        // 4. 이전 block에 삭제 index 넣기
                        byte[] deleteIdxStrBytes = Integer.toString(deleteIdx).getBytes();
                        byte[] buffer = new byte[100];
                        buffer[LAST_IDX] = '\n';
                        System.arraycopy(deleteIdxStrBytes, 0,
                                buffer, NEXT_NODE_IDX, deleteIdxStrBytes.length);
                        System.arraycopy(buffer, 0,
                                block, i * RECORD_SIZE, buffer.length);
                        raf.seek(k);
                        raf.write(block);
                        Arrays.fill(block, (byte) 0);
                        Arrays.fill(buffer, (byte) 0);

                        // 5. 삭제 index에 이전 block이 가리키는 index 넣기
                        raf.seek((long) (deleteIdx / BLOCK_FACTOR) * BLOCK_SIZE);
                        raf.read(block);
                        byte[] prevIdxStrBytes = Integer.toString(prevIdx).getBytes();
                        buffer[LAST_IDX] = '\n';
                        System.arraycopy(prevIdxStrBytes, 0,
                                buffer, NEXT_NODE_IDX, prevIdxStrBytes.length);
                        System.arraycopy(buffer, 0,
                                block, (deleteIdx % BLOCK_FACTOR) * RECORD_SIZE, buffer.length);
                        raf.seek((long) (deleteIdx / BLOCK_FACTOR) * BLOCK_SIZE);
                        raf.write(block);
                        System.out.println("delete Successful!");
                        return;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}