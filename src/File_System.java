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
        this.init = false;                  // Check if ldisk initialized
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
        dirmem.pack(0, 0);
        dirmem.pack(7, 4);
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

        for (int i = 1; i < 7; i++)
        {
            for (byte b : memory.mem = disk.read_block(i)) {}

            for (int j = 0; j < memory.size; j+=16)
            {
                if (memory.unpack(j) == -1)
                {
                    int[] result = new int[] {i,j};
                    return result;
                }
            }
        }

        System.out.println("error");
        return new int[] {-1,-1};
    }

    public void create(String filename)
    {
        PackableMemory filespace = new PackableMemory(64);

        if (this.bitmap.cardinality() == 64) // if full
        {
            System.out.println("error");
            return;
        }

        int index = this.bitmap.nextClearBit(8);
        this.bitmap.set(index);
        int[] desc = _find_descriptor();

        if (desc[0] == -1 || desc[1] == -1)
        {
            System.out.println("error");
            return;
        }
        System.out.println(desc[0]);
        System.out.println(desc[1]);

        // Need to add to directory
        for (byte b : filespace.mem = disk.read_block(desc[0])) {}
        filespace.pack(0, desc[1]);
        filespace.pack(index, desc[1]+4);
        disk.write_block(desc[0], filespace.mem);
    }

    public void destroy(String filename)
    {

    }

    public int open(String filename)
    {
        return 0; //temp handler
    }

    public void close(int index)
    {

    }

    // Add error checking for unopened/uninit OFT entries.
    public int read(int index, int count) // return bytes read
    {
        int pos = this.oftable.get_pos(index);
        PackableMemory memory = new PackableMemory(64);

        for (byte b : memory.mem = this.oftable.get_buf(index).toByteArray()) {}

        if (BitSet.valueOf(memory.mem).isEmpty())
            System.out.println("Read empty");

        return -1; // placeholder
    }

    // Add error checking for unopened/uninit OFT entries.
    public int write(int index, byte[] chars, int count) // return #bytes written
    {
        int pos = this.oftable.get_pos(index);
        PackableMemory memory = new PackableMemory(64);

        for (byte b : memory.mem = chars) {}


        return 0;
    }

    public void lseek(int index, int pos) // pos cannot be >= 192 (max 3 blocks per file)
    {
        // Use descriptors to figure out how many files/indexes there are

        if (pos >= 64) // if new pos not in current block
        {
            pos = pos % 64; // Position in new file

        }

        this.oftable.set_pos(index, pos);

        System.out.println("position is " + pos);
    }

    public void directory()
    {
        // Make sure it reads from beginning of directory thought

        byte[] dir = this.oftable.get_buf(0).toByteArray();
        PackableMemory memory = new PackableMemory(64);

        if (BitSet.valueOf(dir).isEmpty())
        {
            //System.out.println("");
            return;
        }

        for (byte b : memory.mem = dir) {}

        // IN PROGRESS PAST HERE
        for (int i = 0 ; i < memory.size; i+=4)
        {
            System.out.println(memory.unpack(i));
        }
    }

    public void init(String filename) // if no filename, new file -> fix this error
    {
        /*
            - Have to re-init bitmap and other variables when class recreated

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
            this.bitmap.set(0,8); // 7 for directory / 0-6 are for bitmap & descriptors
            this.disk.write_block(0, this.bitmap.toByteArray());
            _init_descriptor();
            System.out.println("disk initialized");
        }

        // This implementation automatically reserves the first block of ldisk for the first dir file.
        OFTEntry directory = new OFTEntry(BitSet.valueOf(this.disk.read_block(7)), 0, 7);
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
