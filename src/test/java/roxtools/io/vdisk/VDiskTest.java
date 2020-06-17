package roxtools.io.vdisk;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class VDiskTest {

    @Test
    @Disabled
    public void test() throws IOException {

        File vDiskDir = new File("/tmp/vdisk-test");

        vDiskDir.mkdirs();

        VDisk vDisk = new VDisk(vDiskDir, 1024, 1024 * 10, 0);

        for (int sectI = 0 ; sectI < vDisk.getTotalSectors() ; sectI++) {
            VDSector sector = vDisk.getSector(sectI);

            for (var i = 0 ; i < sector.getTotalBlocks() ; i++) {
                VDBlock block = sector.getBlock(i);

                System.out.println(sectI + ">" + i + ">> " + block);
            }

        }


        System.out.println("--------------------------------------------");

        VDSector sector = vDisk.getSector(0);

        VDBlock blk0 = sector.createBlock(0);

        System.out.println(blk0);

        System.out.println(Arrays.toString(blk0.readFullBlock()));

        blk0.write(new byte[] {1, 2, 3});

        blk0.write(new byte[] {1, 2, 3, 4, 5, 6, 7});

        System.out.println(Arrays.toString(blk0.readFullBlock()));

        System.out.println(blk0);

        System.out.println("-------");

        VDBlock blk1 = sector.createBlock(1);

        System.out.println(blk1);

        System.out.println(Arrays.toString(blk1.readFullBlock()));

        blk1.write(new byte[] {1, 2, 3, 4, 5});

        System.out.println(Arrays.toString(blk1.readFullBlock()));

        System.out.println(blk1);

        System.out.println("-------");

        VDBlock blk2 = sector.createBlock(2);

        System.out.println(blk2);

        if (!blk1.isFull()) {

            int size = blk1.size();
            int available = blk1.remainingToWrite();

            byte[] fill = new byte[available];

            for (var i = 0 ; i < available ; i++) {
                fill[i] = (byte) (size + i + 1);
            }

            blk1.write(fill);
        }

        blk1.setNextBlock(blk2);

        System.out.println(blk1);

        blk2.write(new byte[] {11, 12, 13});

        System.out.println(blk2);

        System.out.println(Arrays.toString(blk1.readFullBlock()));
        System.out.println(Arrays.toString(blk2.readFullBlock()));

        System.out.println("-----------------------------------------------------");

        VDFile vdFile = new VDFile(vDisk, blk1);

        System.out.println(vdFile);

        //vdFile.delete() ;

        System.out.println(vdFile);

        System.out.println("-----------------------------------------------------");

        ArrayList<String> filesIDs = vDisk.getFilesIDs();

        for (String fileId : filesIDs) {
            VDFile vdFile2 = vDisk.getFileByID(fileId);

            System.out.println(fileId + ">> " + vdFile2);

            System.out.println("    metaData: " + vdFile2.getMetaData());

            byte[] data = vdFile2.readFullData();
            System.out.print("    size1: " + data.length);

            if (false && vdFile2.size() > 1024 * 3) {
                vdFile2.setSize(Math.max(vdFile2.size() / 2, 1));
                System.out.print("    size2: " + data.length);
            }

            System.out.println();
            //System.out.println( Arrays.toString(data) );
        }

        System.out.println("-----------------------------------------------------");

        Iterator<String> iterateRootBlocksIdents = vDisk.iterateFileIDs();

        while (iterateRootBlocksIdents.hasNext()) {
            String id = (String) iterateRootBlocksIdents.next();

            System.out.println("> " + id);
        }

        System.out.println("-----------------------------------------------------------------------------------");

        //if (true) return ;

        VDFile vdFile2 = vDisk.createFile();

        OutputStream outputStream = vdFile2.getOutputStream();

        System.out.println(vdFile2);

        for (var i = 0 ; i < 10005 ; i++) {

            byte[] buff = new byte[15];

            for (int j = 0 ; j < buff.length ; j++) {
                buff[j] = (byte) (i * j);
            }

            outputStream.write(buff);

            if (i % 100 == 0) {
                System.out.println(vdFile2);
            }

        }

        System.out.println(vdFile2);

        System.out.println("-----------------------------------------------------------");

        vdFile2.setMetaData(new VDMetaData("MD:" + vdFile2.getID(), new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}));

        System.out.println(vdFile2);
        System.out.println("metaData: " + vdFile2.getMetaData());

        System.out.println("-----------------------------------------------------------");

        String key = vdFile2.getMetaData().getKey();

        VDFile[] files = vDisk.getFileByMetaDataKey(key);

        for (VDFile vdFile3 : files) {
            System.out.println("get by key: " + key + "> " + vdFile3);
        }

        System.out.println("===============================================================");

        String[] fileKeys = vDisk.getFilesMetaDataKeysArray();

        for (String fileKey : fileKeys) {
            VDFile file = vDisk.getFirstFileByMetaDataKey(fileKey);
            System.out.println(fileKey + ">>> " + file + " > " + file.getMetaDataKey());
        }

        //Thread.sleep(10000000) ;

        //vDisk.clear() ;

        System.out.println("By!");

    }

}