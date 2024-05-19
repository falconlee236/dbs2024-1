package caudbs2024;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
            for(int k = 0; k < INIT_RECORDS + 1; k++){
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
                if (k % BLOCK_FACTOR == BLOCK_FACTOR - 1) {
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

                        // 새로운 record만들기
                        byte[] recordBuffer = new byte[100];
                        recordBuffer[LAST_IDX] = '\n';
                        byte[] insertPosBytes = Integer.toString(insertPos + 1000).getBytes();
                        System.arraycopy(insertPosBytes, 0, recordBuffer, 0, insertPosBytes.length);
                        for (int j = 0; j < record.size(); j++) {
                            byte[] strBytes = record.get(j).getBytes();
                            System.arraycopy(strBytes, 0, recordBuffer,
                                    (j + 1) * COLUMN_SIZE, strBytes.length);
                        }

                        // 삽입 index 위치 이동
                        raf.seek((long) insertPos / BLOCK_FACTOR * BLOCK_SIZE);
                        byte[] tmpBlockBuffer = new byte[300];
                        raf.read(tmpBlockBuffer);
                        // 해당 index 위치에 있는 다음 node를 정보 가져오기
                        // 해당 node에 다음 node 정보가 있을 경우
                        int insertIdx = (insertPos % BLOCK_FACTOR) * RECORD_SIZE;
                        if (tmpBlockBuffer[insertIdx + NEXT_NODE_IDX] != 0){
                            System.arraycopy(tmpBlockBuffer, insertIdx + NEXT_NODE_IDX,
                                    block, NEXT_NODE_IDX, RECORD_SIZE - NEXT_NODE_IDX);
                        } else { // 정보가 없을 경우
                            byte[] tmpBuffer = new byte[4];
                            byte[] nextPos = Integer.toString(insertPos + 1).getBytes();
                            System.arraycopy(nextPos, 0,
                                    tmpBuffer, 0, nextPos.length);
                            System.arraycopy(tmpBuffer, 0,
                                    block, NEXT_NODE_IDX, tmpBuffer.length);
                        }
                        // 갱신된 insert정보를 head에 쓰기
                        raf.seek(0);
                        raf.write(block);

                        // 삽입 index 위치에 record 쓰기
                        raf.seek((long) insertPos / BLOCK_FACTOR * BLOCK_SIZE);
                        raf.read(tmpBlockBuffer);
                        System.arraycopy(recordBuffer, 0,
                                tmpBlockBuffer, insertIdx, recordBuffer.length);
                        tmpBlockBuffer[LAST_IDX] = '\n';
                        tmpBlockBuffer[RECORD_SIZE + LAST_IDX] = '\n';
                        tmpBlockBuffer[2 * RECORD_SIZE + LAST_IDX] = '\n';
                        raf.seek((long) insertPos / BLOCK_FACTOR * BLOCK_SIZE);
                        raf.write(tmpBlockBuffer);

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

    public void partitionDBFile(String relationName, int attribute_num){
        byte[] block = new byte[BLOCK_SIZE];
        byte[] writeBlock1 = new byte[BLOCK_SIZE];
        byte[] writeBlock2 = new byte[BLOCK_SIZE];
        int cnt1 = 0, cnt2 = 0;

        try (
                FileInputStream fis = new FileInputStream(relationName + ".txt");
                FileOutputStream fos1 = new FileOutputStream(relationName+"-part1.txt");
                FileOutputStream fos2 = new FileOutputStream(relationName+"-part2.txt")
        ){
            while (fis.read(block) > 0){
                for(int i = 0; i < BLOCK_FACTOR; i++){
                    ArrayList<String> recordArr = getRecordArr(block, i, attribute_num);
                    if (recordArr.get(0).isEmpty())
                        continue;
                    if (Integer.parseInt(recordArr.get(0)) % 2 == 0){
                        System.arraycopy(block, i * RECORD_SIZE, writeBlock1, cnt1 * RECORD_SIZE, RECORD_SIZE);
                        cnt1++;
                        if (cnt1 == 3){
                            fos1.write(writeBlock1);
                            Arrays.fill(writeBlock1, (byte) 0);
                            cnt1 = 0;
                        }
                    } else {
                        System.arraycopy(block, i * RECORD_SIZE, writeBlock2, cnt2 * RECORD_SIZE, RECORD_SIZE);
                        cnt2++;
                        if (cnt2 == 3){
                            fos2.write(writeBlock2);
                            cnt2 = 0;
                            Arrays.fill(writeBlock2, (byte) 0);
                        }
                    }
                }
            }
            if (cnt1 != 0)
                fos1.write(writeBlock1);
            if (cnt2 != 0)
                fos2.write(writeBlock2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void hashJoinDB(String relationName1, int attribute_num1, String relationName2, int attribute_num2){
        for(int i = 0; i < 2; i++){
            String curBuildFileName = String.format("%s-part%d.txt", relationName1, i + 1);
            String curProbFileName = String.format("%s-part%d.txt", relationName2, i + 1);
            try(
                    FileInputStream buildFileIs = new FileInputStream(curBuildFileName);
                    FileInputStream probFileIs = new FileInputStream(curProbFileName);
                    FileOutputStream mergeFileOs = new FileOutputStream(relationName1 + "-" + relationName2 + "-merged.txt")
            ) {
                HashMap<Integer, ArrayList<ArrayList<String>>> index = getHashIndex(buildFileIs, attribute_num1);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private HashMap<Integer, ArrayList<ArrayList<String>>> getHashIndex(FileInputStream fis, int attribute_num) throws IOException {
        HashMap<Integer, ArrayList<ArrayList<String>>> index = new HashMap<>(5);
        byte[] block = new byte[BLOCK_SIZE];

        @SuppressWarnings("unchecked")
        ArrayList<ArrayList<String>>[] tmpList = new ArrayList[5];
        for(int i = 0; i < tmpList.length; i++){
            tmpList[i] = new ArrayList<>();
        }

        while (fis.read(block) > 0){
            for(int i = 0; i < BLOCK_FACTOR; i++) {
                ArrayList<String> recordArr = getRecordArr(block, i, attribute_num);
                if (recordArr.get(0).isEmpty())
                    continue;
                int pos =  Integer.parseInt(recordArr.get(0));
                tmpList[pos % 5].add(recordArr);
            }
        }
        for(int i = 0; i < tmpList.length; i++){
            index.put(i, tmpList[i]);
        }
        return index;
    }
}