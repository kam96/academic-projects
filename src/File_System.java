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

    private boolean _restore(File file) // Optimize this helper function
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

    public void create(String filename)
    {

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

    public void init(String filename)
    {
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
                System.out.println("error");
        }

        else // Initializes new disk
        {
            this.bitmap.set(0,7);
            this.disk.write_block(0, this.bitmap.toByteArray());
            System.out.println("disk initialized");
        }

        OFTEntry directory = new OFTEntry(BitSet.valueOf(this.disk.read_block(1)), 0, 1);
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
