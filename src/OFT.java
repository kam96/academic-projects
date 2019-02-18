import java.util.BitSet;

public class OFT // Open file table
{
    final private int size = 4;
    private int filecount = 0;

    private OFTEntry[] table;

    public OFT(OFTEntry directory)  // OFT init with directory
    {
        this.filecount = 0;
        this.table = new OFTEntry[size];
        this.table[0] = new OFTEntry();
        this.table[0].setBuffer(directory.getBuffer());
        this.table[0].setIndex(directory.getIndex());
        this.table[0].setPosition(directory.getPosition());
        this.table[0].setInc(4);

        for (int i = 1; i < size; i++)
            this.table[i] = new OFTEntry();
    }

    public int addEntry(OFTEntry entry)
    {
        int index = -1;

        for (int i = 1; i < size; i++)
        {
            if (this.table[i].getIndex() == -1)
            {
                index = i;
                break;
            }
        }

        if (index == -1)
            return -1;

        // Can edit this to produce an object in 1 line
        this.table[index].setBuffer(entry.getBuffer());
        this.table[index].setPosition(entry.getPosition());
        this.table[index].setIndex(entry.getIndex());
        this.table[index].setInc(4);

        this.filecount++;
        return index;
    }

    public void deleteEntry(int oft_index)
    {
        this.table[oft_index].setBuffer(new BitSet(512));
        this.table[oft_index].setPosition(0);
        this.table[oft_index].setIndex(-1);
        this.table[oft_index].setInc(4);

        this.filecount--;
    }

    public void set_buf(BitSet buf, int index)
    {
        this.table[index].setBuffer(buf);
    }

    public BitSet get_buf(int index)
    {
        return this.table[index].getBuffer();
    }

    public void set_pos(int index, int pos)
    {
        this.table[index].setPosition(pos);
    }

    public int get_pos(int index)
    {
        return this.table[index].getPosition();
    }

    public int get_index(int index)
    {
        return this.table[index].getIndex();
    }

    public int get_inc(int index)
    {
        return this.table[index].getInc();
    }

    public void set_inc(int index, int inc)
    {
        this.table[index].setInc(inc);
    }

    public int getFilecount()
    {
        return this.filecount;
    }
}
