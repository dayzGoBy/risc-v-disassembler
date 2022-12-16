public class SymtabEntry {
    public String name;
    public int value;
    public int size;
    public short info;
    public short other;
    public int shndx;

    public SymtabEntry(String name, int value, int size, short info, short other, int shndx) {
        this.name = name;
        this.value = value;
        this.size = size;
        this.info = info;
        this.other = other;
        this.shndx = shndx;
    }

    public String getBind() {
        return switch (info >> 4) {
            case 0 -> "LOCAL";
            case 1 -> "GLOBAL";
            case 2 -> "WEAK";
            case 13 -> "LOPROC";
            case 15 -> "HIPROC";
            default -> throw new IllegalArgumentException();
        };
    }

    public String getType() {
        return switch (info & 0xf){
            case 0 -> "NOTYPE";
            case 1 -> "OBJECT";
            case 2 -> "FUNC";
            case 3 -> "SECTION";
            case 4 -> "FILE";
            case 13 -> "LOPROC";
            case 15 -> "HIPROC";
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public String toString() {
        return String.format("0x%-15X %5d %-8s %-8s %-8s %6s %s", value, size,
                getType(), getBind(), "DEFAULT", shndx, name);
    }
}
