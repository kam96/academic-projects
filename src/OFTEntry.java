import java.util.BitSet;

public class OFTEntry
{
    private BitSet rwbuffer;
    private int position;
    private int index;
    private int currInc;

    public OFTEntry()
    {
        this.index = -1;
    }

    public OFTEntry(BitSet buf, int pos, int ind)
    {
        this.rwbuffer = buf; // current read/write buffer
        this.position = pos; // position pointed to in file
        this.index = ind;    // file descriptor index
        this.currInc = 4;
    }

    public void setBuffer(BitSet buf)
    {
        this.rwbuffer = buf;
    }

    public BitSet getBuffer()
    {
        return this.rwbuffer;
    }

    public void setPosition(int pos)
    {
        this.position = pos;
    }

    public int getPosition()
    {
        return this.position;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public int getIndex()
    {
        return this.index;
    }

    public int getInc() {return this.currInc;}

    public void setInc(int inc) {this.currInc = inc;}
}
