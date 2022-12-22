import java.util.Arrays;
import java.util.Map;

public class Command {
    public Commands name;
    public boolean[] line;
    public int address;

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
            case J:
                boolean[] temp = Arrays.copyOf(Arrays.copyOfRange(line, 21, 31), 20);
                temp[10] = line[20];
                System.arraycopy(line, 12, temp, 11, 8);
                temp[19] = line[31];
                return temp;
            case B:
                temp = Arrays.copyOf(Arrays.copyOfRange(line, 8, 11), 12);
                temp[4] = line[7];
                System.arraycopy(line, 25, temp, 5, 6);
                temp[11] = line[31];
                return temp;
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

    public int getOffset(boolean[] imm) {
        return address + (getImm(imm) << 1);
    }

    public Command setAddress(int address) {
        this.address = address;
        return this;
    }

    public String getArgumets(Map<Integer, String> marks, Integer numberOfMarks) {
        if (name == Commands.JAL) {
            int off = getOffset(imm());
            marks.computeIfAbsent(off, k -> String.format("L%d", numberOfMarks));
            return String.format("%s, 0x%x <%s>", getReg(rd()), off, marks.get(off));
        } else {
            int off;
            switch (name.type) {
                case R:
                    return String.format("%s, %s, %s", getReg(rd()), getReg(rs1()), getReg(rs2()));
                case I:
                    return String.format("%s, %s, %s", getReg(rd()), getReg(rs1()), getImm(imm()));
                case S:
                    return String.format("%s, %s, %s", getReg(rs1()), getReg(rs2()), getImm(imm()));
                case B:
                    off = getOffset(imm());
                    marks.computeIfAbsent(off, k -> String.format("L%d", numberOfMarks));
                    return String.format("%s, %s, 0x%x <%s>", getReg(rs1()), getReg(rs2()), off, marks.get(off));
                case U:
                    return String.format("%s, 0x%x", getReg(rd()), getImm(imm()));
                case LOAD:
                    return String.format("%s, %s(%s)", getReg(rd()), getImm(imm()), getReg(rs1()));
                case STORE:
                    return String.format("%s, %s(%s)", getReg(rs2()), getImm(imm()), getReg(rs1()));
                case FENCE:
                    return "iorw, iorw, 255";
                case SHAMT:
                    return String.format("%s, %s, %s", getReg(rd()), getReg(rs1()), getImm(shamt()));
                case ECALL:
                case EBREAK:
                case UNKNOWN:
                    return "";
            }
        }
        return null;
    }

    public int getValue() {
        int value = 0;
        for (int i = 0; i < 32; i++) {
            value += (line[i] ? 1 : 0) << i;
        }
        return value;
    }
}
