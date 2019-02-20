import java.io.*;
import java.util.BitSet;
import java.util.Arrays;

public class File_System
{
    private IO_System disk;
    private BitSet bitmap;
    private boolean init;
    private OFT oftable;

    public File_System()
    {
        this.disk = new IO_System();        // primary ldisk
        this.bitmap = new BitSet(64); // ldisk[0] bitmap
        this.init = false;                  // Check if ldisk
    }

    private boolean _restore(File file) // Optimize this helper function for init
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BitSet bitArray = new BitSet(512);
            int[] intArray;
            String line;

            for (int i = 0; (line = reader.readLine()) != null; i++)
            {
                bitArray.clear();
                line = line.replace("{", "")
                        .replace("}", "");

                if (!line.equals(""))
                {
                    intArray = Arrays.stream(line.split(", "))
                            .mapToInt(Integer::parseInt)
                            .toArray();

                    for (int j = 0; j < intArray.length; j++)
                        bitArray.set(intArray[j]);

                    this.disk.write_block(i, bitArray.toByteArray());
                }
                else
                    continue;
            }

            reader.close();
        }
        catch (IOException e)
        {
            return false;
        }

        return true;
    }

    private void _init_descriptor() // init helper function
    {
        PackableMemory dirmem = new PackableMemory(64);
        PackableMemory memory = new PackableMemory(64);

        // DIRECTORY DESCRIPTOR AND SUBSEQUENT 3 DESCRIPTORS
        dirmem.pack(0, 0); // File size
        dirmem.pack(7, 4); // First directory file
        for (int i = 8; i < memory.size; i+=4)
            dirmem.pack(-1, i);
        disk.write_block(1, dirmem.mem);

        // EVERYTHING AFTER THE BLOCK WITH THE DIRECTORY
        for (int i = 0; i < memory.size; i+=4)
            memory.pack(-1, i);
        for (int i = 2; i < 7; i++)
            disk.write_block(i, memory.mem);
    }

    private int[] _find_descriptor() // can be improved | helper for create()
    {
        PackableMemory memory = new PackableMemory(64);

        int k = 0; //descriptor index

        for (int i = 1; i < 7; i++)
        {
            for (byte b : memory.mem = disk.read_block(i)) {}

            for (int j = 0; j < memory.size; j+=16)
            {
                if (memory.unpack(j) == -1)
                {
                    int[] result = new int[] {i,j,k};
                    return result;
                }
                k++;
            }
        }

        return new int[] {-1,-1,-1};
    }

    public int create(String filename) // the problem is the directory nav
    {
        if (!this.init)
            return -1;

        int old_pos = this.oftable.get_pos(0);

        lseek(0,0);
        String directory = read(0,old_pos).substring(1); // remove extra char
        int dex = directory.indexOf(filename);
        if (dex != -1)
            return -1;

        PackableMemory filespace = new PackableMemory(64);

        if (this.bitmap.cardinality() == 64) // if full
            return -1;

        int index = this.bitmap.nextClearBit(8);
        this.bitmap.set(index); // should be placed at end to indicate success
        this.disk.write_block(0, this.bitmap.toByteArray()); // update bitmap
        int[] desc = _find_descriptor();

        if (desc[0] == -1 || desc[1] == -1)
            return -1;

        for (byte b : filespace.mem = this.disk.read_block(desc[0])) {}
        filespace.pack(0, desc[1]);             // file size
        filespace.pack(index, desc[1]+4);       // index in ldisk of first file
        this.disk.write_block(desc[0], filespace.mem);   // write to descriptor index

        // add to directory
        char[] name = filename.toCharArray();
        for (int i = 0; i < name.length; i++)
            write(0, filename.charAt(i), 1);

        write(0, Integer.toString(desc[2]).charAt(0), 1);

        return 0;
    }

    public int destroy(String filename)
    {
        if (!this.init)
            return -1;

        int old_pos = this.oftable.get_pos(0);
        int index = -1;

        // Search directory to find file descriptor
        lseek(0,0);
        String directory = read(0,old_pos).substring(1); // remove extra char
        int dex = directory.indexOf(filename);
        if (dex < 0)
            return -1;
        index = directory.toCharArray()[dex+filename.length()] - '0'; // idx of fd

        // Remove directory entry
        String newDir = directory.replace(filename+index, "");
        lseek(0,0);
        for (int i = 0; i < newDir.length(); i++)
            write(0, newDir.charAt(i),1);

        // Update bitmap if file wasn't empty
        PackableMemory memory = new PackableMemory(64);
        int k = 0; // at k = 1 it overwrote directory
        int bm_index = -1;

        for (int i = 1; i < 7; i++)
        {
            for (byte b : memory.mem = disk.read_block(i));

            for (int j = 0; j < memory.size; j+=16)
            {
                if (index == k)
                {
                    // Update bitmap
                    bm_index = memory.unpack(j+4);
                    this.bitmap.clear(bm_index);
                    if (memory.unpack(j+8) != -1)
                    {
                        bm_index = memory.unpack(j+8);
                        this.bitmap.clear(bm_index);
                    }
                    if (memory.unpack(j+12) != -1)
                    {
                        bm_index = memory.unpack(j+12);
                        this.bitmap.clear(bm_index);
                    }
                    this.disk.write_block(0, this.bitmap.toByteArray());

                    // Free file descriptor
                    memory.pack(-1, j); // Clear fd file length
                    memory.pack(-1,j+4); // Clear first file index
                    memory.pack(-1,j+8); // Clear second file index
                    memory.pack(-1,j+12); // clear third file index
                    this.disk.write_block(i, memory.mem);
                    return 0; // return status
                }
                k++;
            }
        }

        return -1;
    }

    public int open(String filename)
    {
        // If the table is full, do not open anymore files
        if (this.oftable.getFilecount() == 3 || !this.init)
            return -1;

        int old_pos = this.oftable.get_pos(0);
        int index = -1;

        // Search directory to find index of file descriptor
        lseek(0,0);
        String directory = read(0,old_pos).substring(1); // remove extra char
        int dex = directory.indexOf(filename);
        if (dex < 0)
            return -1;
        index = directory.toCharArray()[dex+filename.length()] - '0'; // convert fd index to int

        // allocate free OFT entry & fill in current position & fdi
        int oft_index = -1;
        if (index != -1)
            oft_index = this.oftable.addEntry(new OFTEntry(new BitSet(512), 0, index)); // add index
        else
            return -1;

        // read block 0 of file into the r/w buffer
        PackableMemory memory = new PackableMemory(64);
        int k = 0; // at k = 1 it overwrote directory
        int loc = 0;

        for (int i = 1; i < 7; i++)
        {
            for (byte b : memory.mem = disk.read_block(i));

            for (int j = 0; j < memory.size; j+=16)
            {
                if (this.oftable.get_index(oft_index) == k)
                {
                    loc = memory.unpack(j+4);
                    this.oftable.set_buf(BitSet.valueOf(this.disk.read_block(loc)), oft_index);
                    return oft_index;
                }
                k++;
            }
        }
        // return error.
        return -1;
    }

    public int close(int index)
    {
        if (this.oftable.get_index(index) == -1 || !this.init)
            return -1;
        // Write buffer to disk
        PackableMemory memory = new PackableMemory(64);
        int k = 0;
        int loc = 0;

        for (int i = 1; i < 7; i++)
        {
            for (byte b : memory.mem = disk.read_block(i));

            for (int j = 0; j < memory.size; j+=16)
            {
                if (this.oftable.get_index(index) == k)
                {
                    loc = memory.unpack(j+this.oftable.get_inc(index));
                    // write buffer to disk
                    this.disk.write_block(loc, this.oftable.get_buf(index).toByteArray());
                    // Update file length in descriptor
                    memory.pack(this.oftable.get_pos(index),j);
                    this.disk.write_block(i, memory.mem);
                    // Free OFT entry
                    this.oftable.deleteEntry(index);
                    // return status
                    return index;
                }
                k++;
            }
        }

        return -1;
    }

    public String read(int index, int count) // return bytes read
    {
        // ISSUE : extra letter at the beginning when printing out files... hmm
        if (this.oftable.get_index(index) == -1 || !this.init)
            return "error";

        // Compute position in r/w buffer
        int pos = this.oftable.get_pos(index);
        String read_bytes = new String();

        PackableMemory memory = new PackableMemory(64);
        for (int i = 0; i < memory.mem.length; i++)
        {
            if (i<this.oftable.get_buf(index).toByteArray().length)
                memory.mem[i] = this.oftable.get_buf(index).toByteArray()[i];
            else
                memory.mem[i] = 0x00;
        }

        // desired count or end of file reached
        while (count >= 0 && this.oftable.get_pos(index) != 192)
        {
            // 2. end of buffer is reached
            if (this.oftable.get_pos(index) % 64 == 0 && this.oftable.get_pos(index) != 0)
            {
                // MAYBE USE FILE LENGTH?
                // write buffer to disk
                    // using index -> write to current file
                    // using index -> read next block
                // read the next block
                // continue copying
            }

            if (memory.mem[this.oftable.get_pos(index)%64] != 0x00)
                read_bytes += (char)memory.mem[this.oftable.get_pos(index)%64];
            else
                read_bytes += " ";
            count--;
            this.oftable.set_pos(index, pos++);
        }

        // return status
        return read_bytes;
    }

    public int write(int index, char symbol, int count) // return #bytes written
    {
        if (this.oftable.get_index(index) == -1 || !this.init)
            return -1;

        // Compute position in r/w buffer
        int pos = this.oftable.get_pos(index);
        int bytes = 0;

        PackableMemory memory = new PackableMemory(64);
        for (int i = 0; i < memory.mem.length; i++)
        {
            if (i<this.oftable.get_buf(index).toByteArray().length)
                memory.mem[i] = this.oftable.get_buf(index).toByteArray()[i];
            else
                memory.mem[i] = 0x00;
        }

        // copy from memory into buffer until
        // 1. desired count or end of file is reached:
        while (count >= 0 && this.oftable.get_pos(index) != 192)
        {
            // 2. end of buffer is reached
            if (this.oftable.get_pos(index) % 64 == 0 && this.oftable.get_pos(index) != 0)
            {

            }

            memory.mem[this.oftable.get_pos(index)%64] = (byte)symbol;
            count--;
            bytes++;
            this.oftable.set_pos(index, pos++);
            this.oftable.set_buf(BitSet.valueOf(memory.mem), index);
        }

        // Update file length in descriptor
        return bytes-1;
    }

    public int lseek(int index, int pos) // pos cannot be >= 192 (max 3 blocks per file each 64)
    {
        if (!this.init)
            return -1;

        int inc = 4; // used to determine block
        int loc = 0; // used to determine file location
        int k = 0;   // used to determine fd index

        // if new pos not in current block
        if (pos >= 64)
        {
            inc = 8;

            if (pos >= 128)
                inc = 12;

            if (pos >= 192)
                return -1;
        }

        if (this.oftable.get_inc(index) != inc)
        {
            // write the buffer to disk
            PackableMemory memory = new PackableMemory(64);

            for (int i = 1; i < 7; i++) // for each directory index
            {
                for (byte b : memory.mem = disk.read_block(i)) {}

                for (int j = 0; j < memory.size; j+=16)
                {
                    if (this.oftable.get_index(index) == k)
                    {
                        // j = length | j+4 = i1 | j+8 = i2 | j+12 = i3
                        // this assumes that the other blocks do exist
                        loc = memory.mem[j+inc];

                        // write current block to file
                        this.disk.write_block(this.oftable.get_inc(index),
                                this.oftable.get_buf(index).toByteArray());

                        // read the new block in
                        try
                        {
                            this.oftable.set_buf(BitSet.valueOf(
                                    this.disk.read_block(loc)), index);
                        }
                        catch (ArrayIndexOutOfBoundsException a)
                        {
                            return -1;
                        }

                        // set the new file block in OFT
                        this.oftable.set_inc(index, inc);
                    }
                    k++;
                }
            }
        }

        // set the current position to the new position
        this.oftable.set_pos(index, pos);

        // return status
        return pos;
    }

    public String directory()
    {
        if (!this.init)
            return "error";
        int old_pos = this.oftable.get_pos(0); // Store old position
        lseek(0,0); // Go to beginning of directory.
        // Read the directory up to its current position.
         return read(0, old_pos).substring(1).replaceAll("[0-9]"," ");
    }

    public int init()
    {
        this.bitmap.set(0,8); // 7 for directory / 0-6 are for bitmap & descriptors / 8 is first dir file
        this.disk.write_block(0, this.bitmap.toByteArray());
        _init_descriptor();

        OFTEntry directory = new OFTEntry(BitSet.valueOf(this.disk.read_block(7)), 0, 0);
        this.oftable = new OFT(directory);
        this.init = true;

        return 0;
    }

    public int init(String filename)
    {
        File d_file = new File(System.getProperty("user.dir") + "\\" + filename);

        if (d_file.isFile()) // Checks if file exists & isn't dir and restores disk
        {
            boolean check =_restore(d_file);

            if (check)
            {
                this.init = true;
                this.bitmap = BitSet.valueOf(this.disk.read_block(0));
                OFTEntry directory = new OFTEntry(BitSet.valueOf(this.disk.read_block(7)), 0, 0);
                this.oftable = new OFT(directory);

                return 1;
            }
            else
                return -1;
        }

        else // Initializes new disk
            return init();
    }

    public int save(String filename)
    {
        // Can't seem to load up save properly, may be an issue with other functions?
        for (int i = 0; i < 4; i++)
            close(i);

        try
        {
            PrintWriter out = new PrintWriter(filename);

            BitSet liner;
            for (int i = 0; i < 64; i++)
            {
                liner = BitSet.valueOf(disk.read_block(i));
                out.println(liner.toString());
            }

            out.close();
            this.init = false;

            return 0;
        }
        catch (FileNotFoundException e)
        {
            return -1;
        }
    }
}
