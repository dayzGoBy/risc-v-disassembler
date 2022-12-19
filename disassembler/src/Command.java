import java.util.Arrays;

public class Command {
    public Commands name;
    public boolean[] line;
    public int source1;
    public int source2;
    public int dest;
    public boolean[] imm;
    public int address;

    public Command(Commands name, boolean[] line, int source1, int source2, int dest, boolean[] imm) {
        this.line = line;
        this.name = name;
        this.source1 = source1;
        this.source2 = source2;
        this.dest = dest;
        this.imm = imm;
    }

    public Command (Commands name, boolean[] line) {
        this.name = name;
        this.line = line;
    }

    private int rs1() {
        int res = 0;
        for (int i = 0; i < 5; i++) {
            res += (line[i + 15] ? 1 << i : 0);
        }
        return res;
    }

    private int rs2() {
        int res = 0;
        for (int i = 0; i < 5; i++) {
            res += (line[i + 20] ? 1 << i : 0);
        }
        return res;
    }

    private int rd() {
        int res = 0;
        for (int i = 0; i < 5; i++) {
            res += (line[i + 7] ? 1 << i : 0);
        }
        return res;
    }

    private String getReg(int res) {
        switch(res) {
            case 0: return "zero";
            case 1: return "ra";
            case 2: return "sp";
            case 3: return "gp";
            case 4: return "ta";
            case 5: return "t0";
            default: {
                if (res == 6 || res == 7) {
                    return String.format("t%d", res - 5);
                } else if (res >= 10 && res <= 17) {
                    return String.format("a%d", res - 10);
                } else if (res >= 18 && res <= 27) {
                    return String.format("s%d", res - 16);
                } else if (res >= 28 && res <= 31) {
                    return String.format("t%d", res - 25);
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    private boolean[] imm() {
        switch (name.getType()) {
            case R: throw new IllegalStateException();
            case I:
            case LOAD: return Arrays.copyOfRange(line, 20, 32);
            case S:
            case STORE: {
                boolean[] res = Arrays.copyOf(Arrays.copyOfRange(line, 7, 12), 12);
                System.arraycopy(Arrays.copyOfRange(line, 25, 32), 0, res, 5, 7);
                return res;
            }
            case U: return Arrays.copyOfRange(line, 12, 32);
            default: throw new IllegalArgumentException("Unknown operation type");
        }
    }

    private boolean[] shamt() {
        return Arrays.copyOfRange(line, 20, 25);
    }

    private int getImm(boolean[] imm) {
        int res = 0;
        for (int i = 0; i < imm.length - 1; i++) {
            res += (imm[imm.length - 1] != imm[i] ? 1 << i : 0);
        }
        if (imm[imm.length - 1]) res = ~res;
        return res;
    }

    public Command setAddress(int address) {
        this.address = address;
        return this;
    }

    private String getArgumets() {
        switch (name.type) {
            case R: return String.format("%s, %s, %s", getReg(rd()), getReg(rs1()), getReg(rs2()));
            case I: return String.format("%s, %s, %s", getReg(rd()), getReg(rs1()), getImm(imm()));
            case S: return String.format("%s, %s, %s", getReg(rs1()), getReg(rs2()), getImm(imm()));
            case U: return String.format("%s, %x", getReg(rd()), getImm(imm()));
            case LOAD: return String.format("%s, %s(%s)", getReg(rd()), getImm(imm()), getReg(rs1()));
            case STORE: return String.format("%s, %s(%s)", getReg(rs2()), getImm(imm()), getReg(rs1()));
            case FENCE: return "iorw, iorw";
            case SHAMT: return String.format("%s, %s, %s", getReg(rd()), getReg(rs1()), getImm(shamt()));
            case ECALL:
            case EBREAK:
            case UNKNOWN:
                return "";
            case J: return String.format("%s, %s", getReg(rd()), "SMTH");
        }
        return null;
    }

    @Override
    public String toString() {
        int value = 0;
        for (int i = 0; i < 32; i++) {
            value += (line[i] ? 1 : 0) << i;
        }

        return String.format("   %05x:\t%08x\t%7s \t%s", address, value, name.toString().toLowerCase(), getArgumets());
    }
}
