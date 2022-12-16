// a structure to store section info

public class Section {
    public int name;
    public int type;
    public int flags;
    public int addr;
    public int offset;
    public int size;
    public int link;
    public int info;
    public int addralign;
    public int entsize;

    public Section(int name, int type, int flags, int addr, int offset,
                   int size, int link, int info, int addralign, int entsize) {
        this.name = name;
        this.type = type;
        this.flags = flags;
        this.addr = addr;
        this.offset = offset;
        this.size = size;
        this.link = link;
        this.info = info;
        this.addralign = addralign;
        this.entsize = entsize;
    }

    @Override
    public String toString() {
        return String.format("name: %d, type: %d, flags: %d, addr: %d, offset: %d, size: %d, link: %d, info: %d, addralign: %d, entsize: %d",
                name, type, flags, addr, offset, size, link, info, addralign, entsize);
    }

    public boolean isStrTab() {
        return type == 3;
    }
}
