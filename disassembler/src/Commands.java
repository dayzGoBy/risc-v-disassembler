import java.util.Arrays;

public enum Commands {
    // DEFAULT RV32I COMMANDS
    LUI("LUI"),
    AUIPC("AUIPC"),
    JAL("JAL"),
    JALR("JALR"),
    BEQ("BEQ"),
    BNE("BNE"),
    BLT("BLT"),
    BGE("BGE"),
    BLTU("BLTU"),
    BGEU("BGEU"),
    LB("LB"),
    LH("LH"),
    LW("LW"),
    LBU("LBU"),
    LHU("LHU"),
    SB("SB"),
    SH("SH"),
    SW("SW"),
    ADDI("ADDI"),
    SLTI("SLTI"),
    SLTIU("SLTIU"),
    XORI("XORI"),
    ORI("ORI"),
    ANDI("ANDI"),
    SLLI("SLLI"),
    SRLI("SRLI"),
    SRAI("SRAI"),
    ADD("ADD"),
    SUB("SUB"),
    SLL("SLL"),
    SLT("SLT"),
    SLTU("SLTU"),
    XOR("XOR"),
    SRL("SRL"),
    SRA("SRA"),
    OR("OR"),
    AND("AND"),
    FENCE("FENCE"),
    ECALL("ECALL"),
    EBREAK("EBREAK"),

    // RV32M commands
    MUL("MUL"),
    MULH("MULH"),
    MULHSU("MULHSU"),
    MULHU("MULHU"),
    DIV("DIV"),
    DIVU("DIVU"),
    REM("REM"),
    REMU("REMU");

    final String value;

    Commands(String value) {
        this.value = value;
    }

    // method that classifies the command in the line
    public static Command classify (boolean[] line) {
        return new Command(getByCode(line), line);
    }

    private static Commands getByCode(boolean[] line) {
        int opcode = 0;
        for (int i = 0; i < 7; i++) {
            opcode += (line[i] ? 1 << i : 0);
        }
        int func3 = 0;
        for (int i = 0; i < 3; i++) {
            func3 += (line[i + 12] ? 1 << i : 0);
        }
        int func7 = 0;
        for (int i = 0; i < 7; i++) {
            func7 += (line[i + 25] ? 1 << i : 0);
        }
        return switch (opcode) {
            case 0b0110111 -> LUI;
            case 0b0010111 -> AUIPC;
            case 0b1101111 -> JAL;
            case 0b1100111 -> JALR;
            case 0b1100011 -> switch (func3) {
                case 0b000 -> BEQ;
                case 0b001 -> BNE;
                case 0b100 -> BLT;
                case 0b101 -> BGE;
                case 0b110 -> BLTU;
                case 0b111 -> BGEU;
                default -> throw new IllegalArgumentException("Unknown B* command");
            };
            case 0b0000011 -> switch (func3) {
                case 0b000 -> LB;
                case 0b001 -> LH;
                case 0b010 -> LW;
                case 0b100 -> LBU;
                case 0b101 -> LHU;
                default -> throw new IllegalArgumentException("Unknown L* command");
            };
            case 0b0100011 -> switch (func3) {
                case 0b000 -> SB;
                case 0b001 -> SH;
                case 0b010 -> SW;
                default -> throw new IllegalArgumentException("Unknown S* command");
            };
            case 0b0010011 -> switch (func3) {
                case 0b000 -> ADDI;
                case 0b010 -> SLTI;
                case 0b011 -> SLTIU;
                case 0b100 -> XORI;
                case 0b110 -> ORI;
                case 0b111 -> ANDI;
                case 0b001 -> SLLI;
                case 0b101 -> switch (func7) {
                    case 0b0000000 -> SRLI;
                    case 0b0100000 -> SRAI;
                    default -> throw new IllegalStateException("Unexpected value: " + func7);
                };
                default -> throw new IllegalArgumentException("Unknown *I command");
            };
            case 0b0110011 -> switch (func3) {
                case 0b000 -> switch (func7) {
                    case 0b0000000 -> ADD;
                    case 0b0100000 -> SUB;
                    case 0b0000001 -> MUL;
                    default -> throw new IllegalArgumentException("Unknown ADD/SUB command");
                };
                case 0b001 -> switch(func7) {
                    case 0b0000000 -> SLL;
                    case 0b0000001 -> MULH;
                    default -> throw new IllegalArgumentException();
                };
                case 0b010 -> switch(func7) {
                    case 0b0000000 -> SLT;
                    case 0b0000001 -> MULHSU;
                    default -> throw new IllegalArgumentException();
                };
                case 0b011 -> switch(func7) {
                    case 0b0000000 -> SLTU;
                    case 0b0000001 -> MULHU;
                    default -> throw new IllegalArgumentException();
                };
                case 0b100 -> switch(func7) {
                    case 0b0000000 -> XOR;
                    case 0b0000001 -> DIV;
                    default -> throw new IllegalArgumentException();
                };
                case 0b101 -> switch (func7) {
                    case 0b0000000 -> SRL;
                    case 0b0100000 -> SRA;
                    case 0b0000001 -> DIV;
                    default -> throw new IllegalArgumentException();
                };
                case 0b110 -> switch(func7) {
                    case 0b0000000 -> OR;
                    case 0b0000001 -> REM;
                    default -> throw new IllegalArgumentException();
                };
                case 0b111 -> switch(func7) {
                    case 0b0000000 -> AND;
                    case 0b0000001 -> REMU;
                    default -> throw new IllegalArgumentException();
                };
                default -> throw new IllegalArgumentException();
            };
            case 0b0001111 -> FENCE;
            case 0b1110011 -> line[20] ? EBREAK : ECALL;
            default -> throw new IllegalArgumentException("Unknown command");
        };
    }



    @Override
    public String toString() {
        return value;
    }
}

