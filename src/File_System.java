import java.io.*;
import java.util.BitSet;

public class File_System
{
    private IO_System disk;
    private BitSet bitmap;
    private BitSet mask;

    public File_System()
    {
        this.disk = new IO_System();        // primary ldisk
        this.bitmap = new BitSet(64); // ldisk[0] bitmap
        this.mask = new BitSet(64);   // mask for bitmap
    }

    private boolean _restore(File file)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while((line = reader.readLine()) != null)
            {
                System.out.println(line);
                // DO CODE HERE !!!!
            }

            reader.close();
        }
        catch (IOException e)
        {
            return false;
        }

        return true;
    }

    public void read(int index, int count)
    {
        
    }

    public void write(int index, byte[] chars, int count)
    {

    }

    public void init(String filename)
    {
        File d_file = new File(System.getProperty("user.dir") + "\\" + filename);

        if (d_file.isFile()) // Checks if file exists & isn't dir and restores disk
        {
            boolean check =_restore(d_file);

            if (check)
                System.out.println("disk restored");
            else
                System.out.println("DISK ERROR");
        }

        else // Initializes new disk
        {
            this.bitmap.set(0,7);
            this.disk.write_block(0, this.bitmap.toByteArray());

            System.out.println("disk initialized");
        }
    }

    public void save(String filename)
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
            System.out.println("SAVE ERROR");
        }
    }
}
