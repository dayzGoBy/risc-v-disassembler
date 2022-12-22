public enum Commands {
    // DEFAULT RV32I COMMANDS
    UNKNOWN("UNKNOWN_INSTRUCTION", Type.UNKNOWN),
    LUI("LUI", Type.U),
    AUIPC("AUIPC", Type.U),
    JAL("JAL", Type.J),
    JALR("JALR", Type.LOAD),
    BEQ("BEQ", Type.B),
    BNE("BNE", Type.B),
    BLT("BLT", Type.B),
    BGE("BGE", Type.B),
    BLTU("BLTU", Type.B),
    BGEU("BGEU", Type.B),
    LB("LB", Type.LOAD),
    LH("LH", Type.LOAD),
    LW("LW", Type.LOAD),
    LBU("LBU", Type.LOAD),
    LHU("LHU", Type.LOAD),
    SB("SB", Type.STORE),
    SH("SH", Type.STORE),
    SW("SW", Type.STORE),
    ADDI("ADDI", Type.I),
    SLTI("SLTI", Type.I),
    SLTIU("SLTIU", Type.I),
    XORI("XORI", Type.I),
    ORI("ORI", Type.I),
    ANDI("ANDI", Type.I),
    SLLI("SLLI", Type.SHAMT),
    SRLI("SRLI", Type.SHAMT),
    SRAI("SRAI", Type.SHAMT),
    ADD("ADD", Type.R),
    SUB("SUB", Type.R),
    SLL("SLL", Type.R),
    SLT("SLT", Type.R),
    SLTU("SLTU", Type.R),
    XOR("XOR", Type.R),
    SRL("SRL", Type.R),
    SRA("SRA", Type.R),
    OR("OR", Type.R),
    AND("AND", Type.R),
    FENCE("FENCE", Type.FENCE),
    ECALL("ECALL", Type.ECALL),
    EBREAK("EBREAK", Type.EBREAK),

    // RV32M commands
    MUL("MUL", Type.R),
    MULH("MULH", Type.R),
    MULHSU("MULHSU", Type.R),
    MULHU("MULHU", Type.R),
    DIV("DIV", Type.R),
    DIVU("DIVU", Type.R),
    REM("REM", Type.R),
    REMU("REMU", Type.R);

    final String value;
    final Type type;

    Commands(String value, Type type) {
        this.value = value;
        this.type = type;
    }

    public enum Type{
        R, I, S, B, U, J, LOAD, STORE, ECALL, EBREAK, FENCE, SHAMT, UNKNOWN
    }

    // method that classifies the command in the line
    public static Command classify (boolean[] line) {
        return new Command(getByCode(line), line);
    }

    public Type getType() {
        return this.type;
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
                default -> UNKNOWN;
            };
            case 0b0000011 -> switch (func3) {
                case 0b000 -> LB;
                case 0b001 -> LH;
                case 0b010 -> LW;
                case 0b100 -> LBU;
                case 0b101 -> LHU;
                default -> UNKNOWN;
            };
            case 0b0100011 -> switch (func3) {
                case 0b000 -> SB;
                case 0b001 -> SH;
                case 0b010 -> SW;
                default -> UNKNOWN;
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
                    default -> UNKNOWN;
                };
                default -> UNKNOWN;
            };
            case 0b0110011 -> switch (func3) {
                case 0b000 -> switch (func7) {
                    case 0b0000000 -> ADD;
                    case 0b0100000 -> SUB;
                    case 0b0000001 -> MUL;
                    default -> UNKNOWN;
                };
                case 0b001 -> switch(func7) {
                    case 0b0000000 -> SLL;
                    case 0b0000001 -> MULH;
                    default -> UNKNOWN;
                };
                case 0b010 -> switch(func7) {
                    case 0b0000000 -> SLT;
                    case 0b0000001 -> MULHSU;
                    default -> UNKNOWN;
                };
                case 0b011 -> switch(func7) {
                    case 0b0000000 -> SLTU;
                    case 0b0000001 -> MULHU;
                    default -> UNKNOWN;
                };
                case 0b100 -> switch(func7) {
                    case 0b0000000 -> XOR;
                    case 0b0000001 -> DIV;
                    default -> UNKNOWN;
                };
                case 0b101 -> switch (func7) {
                    case 0b0000000 -> SRL;
                    case 0b0100000 -> SRA;
                    case 0b0000001 -> DIV;
                    default -> UNKNOWN;
                };
                case 0b110 -> switch(func7) {
                    case 0b0000000 -> OR;
                    case 0b0000001 -> REM;
                    default -> UNKNOWN;
                };
                case 0b111 -> switch(func7) {
                    case 0b0000000 -> AND;
                    case 0b0000001 -> REMU;
                    default -> UNKNOWN;
                };
                default -> UNKNOWN;
            };
            case 0b0001111 -> FENCE;
            case 0b1110011 -> line[20] ? EBREAK : ECALL;
            default -> UNKNOWN;
        };
    }

    @Override
    public String toString() {
        return value;
    }
}

