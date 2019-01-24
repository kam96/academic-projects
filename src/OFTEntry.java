import java.util.BitSet;

public class OFTEntry
{
    private BitSet rwbuffer;
    private int position;
    private int index;

    public OFTEntry(BitSet buf, int pos, int ind)
    {
        this.rwbuffer = buf;
        this.position = pos;
        this.index = ind;
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
}
