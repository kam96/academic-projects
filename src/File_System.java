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
        for (int i = 4; i < memory.size; i+=4)
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

            for (int j = 0; j < memory.size; j+=4)
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
        int[] desc = _find_descriptor();

        if (desc[0] == -1 || desc[1] == -1)
        {
            System.out.println("error");
            return;
        }


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

    public void read(int index, int count) // return bytes read
    {
        BitSet block = new BitSet(64);

    }

    public int write(int index, byte[] chars, int count) // return #bytes written
    {
        return 0; //temp
    }

    public void lseek(int index, int pos)
    {
        // should use some sort of "this" variable
    }

    public String[] directory()
    {
        return new String[]{}; // tempo placeholder
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
            this.bitmap.set(0,8); // first for directory / 0-7 are for bitmap & descriptors
            this.disk.write_block(0, this.bitmap.toByteArray());
            _init_descriptor();
            System.out.println("disk initialized");
        }

        /*PackableMemory memtest = new PackableMemory(64);
        for (int i = 2; i < 7; i++)
            for (byte b : memtest.mem = disk.read_block(i)) {}
        System.out.println(memtest.unpack(0));*/

        // This implementation automatically reserves the first block of ldisk for the first dir file.
        OFTEntry directory = new OFTEntry(BitSet.valueOf(this.disk.read_block(1)), 0, 7);
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
