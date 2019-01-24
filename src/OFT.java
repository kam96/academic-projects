import java.util.BitSet;

public class OFT // Open file table
{
    final private int size = 4;

    private OFTEntry[] table;

    public OFT(OFTEntry directory)  // OFT init with directory
    {
        this.table = new OFTEntry[size];

        this.table[0].setBuffer(directory.getBuffer());
        this.table[0].setIndex(directory.getIndex());
        this.table[0].setPosition(directory.getPosition());
    }

    public void addEntry(OFTEntry entry)
    {
        int index = -1;

        for (int i = 1; i < size; i++)
        {
            if (this.table[i].getIndex() == -1)
                index = i;
        }

        if (index == -1)
        {
            System.out.println("error");
            return;
        }

        this.table[index].setBuffer(entry.getBuffer());
        this.table[index].setPosition(entry.getPosition());
        this.table[index].setIndex(entry.getIndex());
    }

    public void deleteEntry(int oft_index)
    {
        this.table[oft_index].setBuffer(new BitSet());
        this.table[oft_index].setPosition(0);
        this.table[oft_index].setIndex(-1);
    }
}
