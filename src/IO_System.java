import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class IO_System
{
    public static short L = 64; // number of logical blocks on ldisk (64 blocks)
    public static short B = 64; // length of blocks in bytes (64B = 16 int)
    private byte ldisk[][];     // virtual byte array

    public IO_System()
    {
        this.ldisk = new byte[L][B];
        for (int i = 0; i < L; i++)
        {
            this.ldisk[i] = new byte[B];
            for (int j = 0; j < B; j++)
            {
                this.ldisk[i][j] = 0;
            }
        }
    }

    public byte[] read_block(int i)
    {
        byte[] p;
        for (byte b : p = this.ldisk[i]) {}
        return p;
    }

    public void write_block(int i, byte[] p)
    {
        for (byte b : this.ldisk[i] = p) {}
    }

    public void save_disk(File file) throws IOException
    {
        try (FileOutputStream fos = new FileOutputStream(file))
        {
            for (int i = 0; i < L; i++)
            {
                for (int j = 0; j < B; j++)
                {
                    fos.write(ldisk[i][j]);
                }
            }
        }
    }
}
