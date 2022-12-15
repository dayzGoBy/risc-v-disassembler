public class SymtabEntry {
    public int name;
    public int value;
    public int size;
    public short info;
    public short other;
    public int shndx;

    public SymtabEntry(int name, int value, int size, short info, short other, int shndx) {
        this.name = name;
        this.value = value;
        this.size = size;
        this.info = info;
        this.other = other;
        this.shndx = shndx;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
