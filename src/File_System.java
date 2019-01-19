import java.io.File;
import java.io.IOException;
import java.util.BitSet;

public class File_System
{
    private IO_System disk;
    private BitSet bitmap;
    private BitSet mask;

    public File_System()
    {
        this.disk = new IO_System();        // primary ldisk
        this.bitmap = new BitSet(64); // ldisk[0]
        this.mask = new BitSet(64);
    }

    public void init(String filename)
    {
        File d_file = new File(System.getProperty("user.dir") + "\\" + filename);

        if (d_file.isFile()) // Checks if file exists & isn't dir and restores disk
        {
            // *************************
            System.out.println("disk restored");
        }

        else // Initializes new disk
        {
            this.bitmap.set(0,7);
            this.disk.write_block(0, this.bitmap.toByteArray());

            System.out.println("disk initialized");
        }
    }

    public void save(String filename) throws IOException
    {
        File d_file = new File(System.getProperty("user.dir") + "\\" + filename);

        if (!d_file.exists())
        {
            d_file.createNewFile();
        }

        disk.save_disk(d_file);
        System.out.println("disk saved");
    }



    /*
        TODO:
        - init needs disk restore and disk init functions

     */
}
