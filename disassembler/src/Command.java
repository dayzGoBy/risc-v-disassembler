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

    private static int rs1(boolean[] line) {
        int res = 0;
        for (int i = 0; i < 5; i++) {
            res += (line[i + 15] ? 1 << i : 0);
        }
        return res;
    }

    private static int rs2(boolean[] line) {
        int res = 0;
        for (int i = 0; i < 5; i++) {
            res += (line[i + 20] ? 1 << i : 0);
        }
        return res;
    }

    private static int rd(boolean[] line) {
        int res = 0;
        for (int i = 0; i < 5; i++) {
            res += (line[i + 7] ? 1 << i : 0);
        }
        return res;
    }

    private static boolean[] imm(boolean[] line, String type) {
        switch (type) {
            case "R": return new boolean[0];
            case "I": return Arrays.copyOfRange(line, 20, 32);
            case "S": {
                boolean[] res = Arrays.copyOf(Arrays.copyOfRange(line, 7, 12), 12);
                System.arraycopy(Arrays.copyOfRange(line, 25, 32), 0, res, 5, 7);
                return res;
            }
            default: throw new IllegalArgumentException("Unknown operation type");
        }
    }

    public Command setAddress(int address) {
        this.address = address;
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s", name);
    }
}
