package practice;

/* 
You need to implement a class that provides byte-based I/O operations using an underlying
 block-based interface. The provided class should support the following operations:

int write(byte[] dataPtr, int len);
int read(byte[] dataPtr, int len);
int seek(int location);
The underlying block-based interface provides the following methods:

int bSeek(int blockNum);
int bWrite(byte[] blockPtr);
int bRead(byte[] blockPtr);
int bBlockSize();
Your goal is to implement the byte-based I/O operations using the block-based methods provided.

Example Input
seek(12): Seeks to the 12th byte.
write(data1, 15): Writes 15 bytes from data1 starting at the current seek position.
write(data2, 16): Writes 16 bytes from data2 starting at the current seek position.
seek(17): Seeks to the 17th byte.
read(data3, 2): Reads 2 bytes into data3 starting at the current seek position.
*/
import java.util.ArrayList;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;

class ByteBlockInterface {

    private Device device;

    public ByteBlockInterface() {
        this.device = new Device(4);
    }

    public ByteBlockInterface(int block_size) {
        this.device = new Device(block_size);
    }

    public ByteBlockInterface(ArrayList<String> data, int start_block, int start_byte) {
        this.device = new Device(data, start_block, start_byte);
    }

    public ByteBlockInterface(ArrayList<String> data, int block_size) {
        this.device = new Device(data, block_size);
    }

    public ByteBlockInterface(ArrayList<String> data) {
        this.device = new Device(data);
    }

    int write(byte[] data, int len) {
        return this.device.bWrite(data);
    }

    int read(int len) {
        byte[] bytes = this.device.bRead(len);
        System.out.println(new String(bytes, StandardCharsets.UTF_8));
        return 0;
    }

    int seek(int location) {
        return this.device.bSeek(location);
    }

}

class Device {

    private int start_block;
    private int start_byte;
    private ArrayList<String> data;
    private int block_size = 4;

    public Device() {
        this.data = new ArrayList<>();
        this.data.add(new String());
        this.start_block = 0;
        this.start_byte = 0;
    }

    public Device(ArrayList<String> data) {
        this.initializeData(data);
    }

    public Device(ArrayList<String> data, int block_size) {
        this.block_size = block_size;
        this.initializeData(data);
    }

    public Device(int start_block, int start_byte) {
        this.data = new ArrayList<>();
        this.bSeek(start_block);
        this.start_block = start_block;
        String curr_block = this.data.get(this.start_block);
        int i = 0;
        while (i < start_byte) {
            curr_block = new String(new StringBuilder(curr_block).append(" "));
            i += 1;
        }
        this.data.set(this.start_block, curr_block);
        this.start_byte = start_byte;
    }

    public Device(ArrayList<String> data, int start_block, int start_byte) {
        this.initializeData(data);
        this.bSeek(start_block);
        this.start_block = start_block;
        this.start_byte = start_byte;
    }

    public Device(int block_size) {
        this.block_size = block_size;
        this.data = new ArrayList<>();
        this.data.add(new String());
        this.start_block = 0;
        this.start_byte = 0;
    }

    public void initializeData(ArrayList<String> data) {
        this.data = data;
        this.start_block = data.size();
        int last_block_data_filled = this.data.get(this.data.size() - 1).length();
        if (last_block_data_filled < this.block_size) {
            this.start_block -= 1;
        }
        this.start_byte = last_block_data_filled;

    }

    public int bBlockSize() {
        return this.block_size;
    }

    public int bSeek(int blockNum) {
        if (blockNum < this.data.size()) {
            this.start_block = blockNum;
            return 0;
        }
        this.start_block = blockNum;
        while (this.data.size() <= this.start_block) {
            this.data.add(new String());
        }
        this.start_block = blockNum;
        this.start_byte = 0;
        return 0;
    }

    public int bWrite(byte[] blockPtr) {
        try {
            String str = new String(blockPtr, StandardCharsets.UTF_8);
            System.out.println(this.start_block + " " + this.start_byte);
            int left_space = this.block_size - this.start_byte;
            StringBuilder b = new StringBuilder(str);
            String fill = b.substring(0, Math.min(left_space, b.length()));
            str = b.substring(Math.min(left_space, str.length()));
            StringBuilder filler = new StringBuilder(this.data.get(this.data.size() - 1));
            filler.append(fill);
            this.data.set(this.start_block, new String(filler));
            this.start_byte = 0;
            this.start_block += 1;
            this.data.add(new String());
            while (str.length() > 0) {
                left_space = this.block_size - this.start_byte;
                b = new StringBuilder(str);
                fill = b.substring(0, Math.min(left_space, b.length()));
                str = b.substring(Math.min(left_space, str.length()));
                filler = new StringBuilder(this.data.get(this.data.size() - 1));
                filler.append(fill);
                this.data.set(this.start_block, new String(filler));
                this.start_byte += fill.length();
                if (this.start_byte == this.block_size) {
                    this.start_block += 1;
                    this.start_byte = 0;
                    this.data.add(new String());
                }
            }
            if (this.start_byte == this.block_size) {
                this.start_block += 1;
                this.start_byte = 0;
                this.data.add(new String());
            }
            System.out.println(this.data);
            return 0;
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    public byte[] bRead(int size) {
        StringBuilder rd = new StringBuilder();
        int currBlock = this.start_block;
        int currByte = this.start_byte;
        while (size > 0) {
            int left = this.data.get(currBlock).length() - currByte;
            if (left > 0) {
                rd.append(new StringBuilder(this.data.get(currBlock)).substring(currByte, this.block_size));
            }
            currByte += left;
            if (currByte == this.block_size) {
                currByte = 0;
                currBlock += 1;
            }
            size -= left;
            if (size > 0 && currBlock == this.data.size()) {
                return new byte[0];
            }
        }
        String readData = new String(rd);
        return readData.getBytes();
        // return 0;
    }
}

class ByteBlock {

    public static ByteBlockInterface bbi;

    public static void main(String args[]) {
        bbi = new ByteBlockInterface();
        String a = "sadawejfghaewfhslefbslekfnserfsergfsd";
        bbi.write(a.getBytes(), a.length());
        bbi.seek(6);
        bbi.write(a.getBytes(), a.length());

        String[] data_array = { "sada", "wejf", "ghae", "wfhs", "lefb", "slek", "fnse", "rfse", "rgfs", "d" };
        ArrayList<String> data = new ArrayList<>(Arrays.asList(data_array));
        a = "test";
        bbi = new ByteBlockInterface(data, 3, 2);
        bbi.seek(6);
        bbi.read(5);
        bbi.write(a.getBytes(), a.length());
    }
}
