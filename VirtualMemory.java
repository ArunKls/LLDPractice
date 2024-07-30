package practice;

import java.util.*;

class Memory {
    public int levels;
    public int pages = 64;
    private ArrayList<Object> physicalMemory;
    private int pte_size = 4;
    private HashSet<Integer> free_list;

    public Memory(int levels){
        this.levels = levels;
        this.physicalMemory = new ArrayList<>(64);
        this.pages = 64;
        this.initialize();
    }
    public Memory(int levels, int pages){
        this.levels = levels;
        this.physicalMemory = new ArrayList<>(pages);
        this.pages = pages;
        this.initialize();
    }
    public int get_free_page(){
        int page = 4 + (int)(Math.random()*60);
        while (this.free_list.contains(page)){
            page = 4 + (int)(Math.random()*60);
        }
        return page;
    }
    public void initialize(){
        this.physicalMemory.set(0, "OS");
        this.physicalMemory.set(1, "OS");
        this.physicalMemory.set(2, "OS");
        this.physicalMemory.set(3, "OS");
        int idx = 4;
        this.physicalMemory.set(idx, new int[this.pte_size]);
        int[] page_tables = new int[this.levels];
        page_tables[0] = 4;
        while (idx < this.levels+idx-1){
            this.physicalMemory.set(idx, new int[this.pte_size]);
            int[] page_table = (int[])this.physicalMemory.get(idx);
            page_table[0] = idx-1;
            page_tables[idx-4] = idx;
            idx ++;
        }
        for (int i=idx; i<this.pages; i++){
            this.free_list.add(i);
        }
        int page;
        String s;
        for (int i=0; i<this.pages/5; i++){
            page = get_free_page();
            s = "";
            for (int j = 0; j < 5; j++) {
                int c = 65 + (int) (Math.random() * 26);
                s += (char) c;
            }
            System.out.println(page+s);
            // this.physicalMemory.set(page, s);
            // this.free_list.remove(page);
            // if (((int[])this.physicalMemory.get(page_tables[0])).length == 4)
        }
    }
}

class VirtualMemory {
    @SuppressWarnings("unchecked")
    public static char translate(String addr, int offset, int pte_size, int page_size){
        int binary = Integer.parseInt(addr, 2);
        int vpn = binary >> offset;
        double num_of_entries = page_size/pte_size;
        int bits_for_level = (int)(Math.log(num_of_entries)/Math.log(2));
        int levels = (int)Math.ceil((addr.length() - offset)/bits_for_level);
        int entry = 0;
        ArrayList<Object> memory = new ArrayList<>();
        for (int i=levels-1; i>=0; i--){
            entry = vpn >> (i*bits_for_level);
            memory = (ArrayList<Object>)memory.get(entry);
        }
        int offset_val = binary & (int)(Math.pow(2, offset)-1);
        return ((String)memory.get(entry)).charAt(offset_val);
    }
}
