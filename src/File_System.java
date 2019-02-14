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
            BitSet bitArray = new BitSet(64);
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

    public void create(String filename) // the problem is the directory nav
    {
        PackableMemory filespace = new PackableMemory(64);

        if (this.bitmap.cardinality() == 64) // if full
        {
            System.out.println("error");
            return;
        }

        int index = this.bitmap.nextClearBit(8);
        this.bitmap.set(index); // should be placed at end to indicate success
        int[] desc = _find_descriptor();

        if (desc[0] == -1 || desc[1] == -1)
        {
            System.out.println("error");
            return;
        }

        for (byte b : filespace.mem = disk.read_block(desc[0])) {}
        filespace.pack(0, desc[1]);             // file size
        filespace.pack(index, desc[1]+4);       // index in ldisk of first file
        disk.write_block(desc[0], filespace.mem);   // write to descriptor index

        // add to directory
        char[] name = filename.toCharArray();
        for (int i = 0; i < name.length; i++)
            write(0, filename.charAt(i), 1);

        write(0, Integer.toString(desc[2]).charAt(0), 1);

        System.out.println(filename + " created");
    }

    public void destroy(String filename)
    {

        System.out.println(filename + " destroyed");
    }

    public int open(String filename)
    {
        return 0; //temp handler
    }

    public void close(int index) //incomplete
    {
        // Write buffer to disk
        // This probably needs to be fixed to get the right index of the file
        this.disk.write_block(this.oftable.get_index(index), this.oftable.get_buf(index).toByteArray()); // ???

        // Update file length in descriptor

        // Free OFT entry
        this.oftable.deleteEntry(index);

        // return status
        System.out.println(index + ". " + "closed");
    }

    public int read(int index, int count) // return bytes read
    {
        // ISSUE : extra letter at the beginning when printing out files... hmm
        // ISSUE : When rd 0 192 is called 3 different versions of the same thing print

        // Compute position in r/w buffer
        int pos = this.oftable.get_pos(index);
        String read_bytes = new String();
        int bytes = 0;

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
            if (this.oftable.get_pos(index) % 64 == 0)
            {

            }

            if (memory.mem[this.oftable.get_pos(index)%64] != 0x00)
                read_bytes += (char)memory.mem[this.oftable.get_pos(index)%64];
            else
                read_bytes += " ";
            count--;
            bytes++;
            this.oftable.set_pos(index, pos++);
        }

        // return status
        System.out.println(read_bytes);
        return bytes-1;
    }

    public int write(int index, char symbol, int count) // return #bytes written
    {
        // ISSUE : WRITES ONE LESS FOR SOME REASON
        //      * writing i 1 time after seeking worked for some reason
        //      * without modifying return, an extra byte is written, extra loop?

        // Compute position in r/w buffer
        int pos = this.oftable.get_pos(index);
        int bytes = 0;

        PackableMemory memory = new PackableMemory(64);
        for (int i = 0; i < memory.mem.length; i++) // nice dude
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
            if (this.oftable.get_pos(index) % 64 == 0)
            {

            }

            memory.mem[this.oftable.get_pos(index)%64] = (byte)symbol;
            count--;
            bytes++;
            this.oftable.set_pos(index, pos++);
            this.oftable.set_buf(BitSet.valueOf(memory.mem), index);
        }

        // Update file length in descriptor <- do this
        return bytes-1;
    }

    public int lseek(int index, int pos) // pos cannot be >= 192 (max 3 blocks per file each 64)
    {
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
            {
                System.out.println("error");
                return -1;
            }
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
                            System.out.println("error");
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

    public void directory() // Done but need to ignore numbers
    {
        int old_pos = this.oftable.get_pos(0); // Store old position
        lseek(0,0); // Go to beginning of directory.
        read(0, old_pos); // Read the directory up to its current position.
    }

    public void init(String filename) // if no filename, new file -> fix this error
    {
        /*
            - Have to re-init bitmap and other variables when class recreated & ldisk
         */

        File d_file = new File(System.getProperty("user.dir") + "\\" + filename);

        if (d_file.isFile()) // Checks if file exists & isn't dir and restores disk
        {
            boolean check =_restore(d_file);

            if (check)
            {
                this.init = true;
                System.out.println("disk restored");
            }
            else
            {
                System.out.println("error");
                return;
            }
        }

        else // Initializes new disk
        {
            this.bitmap.set(0,8); // 7 for directory / 0-6 are for bitmap & descriptors / 8 is first dir file
            this.disk.write_block(0, this.bitmap.toByteArray());
            _init_descriptor();
            System.out.println("disk initialized");
        }

        // This implementation automatically reserves the first block of ldisk for the first dir file.
        OFTEntry directory = new OFTEntry(BitSet.valueOf(this.disk.read_block(7)), 0, 0); // ???
        this.oftable = new OFT(directory);
    }

    public void save(String filename) // Add functionality to close all files
    {
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
            System.out.println("disk saved");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("error");
        }
    }
}
