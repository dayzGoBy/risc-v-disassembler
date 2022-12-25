import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.*;

public class Disassembler {
    public static void main(String[] args) {
        try (
                Parser p = new Parser(args[1]);
                Writer w = new Writer(args[2])
        ) {
            p.parseHeaderAndSections();
            p.parseText();
            w.writeText(p.programText, p.symbolTable);
            w.writeSymTab(p.symbolTable);
        } catch (IOException ex) {
            System.out.println("Error: input/output exception");
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static class Parser implements AutoCloseable {
        ByteBuffer source;
        int pointer = 0;
        int shoff;
        int shnum;
        int shstrndx;
        List<Section> sections = new ArrayList<>();
        List<SymtabEntry> symbolTable = new ArrayList<>();
        List<Command> programText = new ArrayList<>();
        byte[] shstrtab;
        byte[] strtab;

        public Parser(String path) {
            try {
                this.source = ByteBuffer.wrap(Files.readAllBytes(new File(path).toPath()));
                this.source.order(ByteOrder.LITTLE_ENDIAN);
            } catch (FileNotFoundException ex) {
                System.out.println("Error: file not found");
            } catch (IOException ex) {
                System.out.println("Error: IOException");
            }
        }

        public void parseHeaderAndSections() throws IOException {
            if (readWord() == 0x7f454c46) {
                throw new IllegalArgumentException("This file is not elf");
            }
            skip(1); // EI_CLASS
            readByte(); // Little endian must be
            skip(10); // EI_VERSION/PAD
            readHalf(); // e_type
            readHalf(); // e_machine
            readWord(); // version
            readAddr(); // entry
            readOff(); // phoff
            shoff = readOff(); // shoff
            readWord(); // flags
            readHalf(); // ehsize
            readHalf();// phentsize
            readHalf(); // phnum
            readHalf(); // shentsize
            shnum = readHalf(); // shnum
            shstrndx = readHalf(); // shstrndx

            parseSectionHeader();
            parseSectionHeaderStrtab();
            parseStrtab();
            parseSymtab();
        }

        public void parseSectionHeader() {
            pointer = shoff;
            for (int i = 0; i < shnum; i++) {
                sections.add(
                        new Section(
                                readWord(), // sh_name;
                                readWord(), // sh_type;
                                readWord(), // sh_flags;
                                readAddr(), // sh_addr;
                                readOff(), // sh_offset;
                                readWord(), // sh_size;
                                readWord(), // sh_link;
                                readWord(), // sh_info;
                                readWord(), // sh_addralign;
                                readWord() // sh_entsize;
                        )
                );
            }
        }

        public void parseSectionHeaderStrtab() {
            Section tab = sections.get(shstrndx);
            shstrtab = new byte[tab.size];
            pointer = tab.offset;
            for (int i = 0; i < tab.size; i++) {
                shstrtab[i] = readByte();
            }
        }

        public String getSectionName(int index) {
            int i = index;
            StringBuilder s = new StringBuilder();
            byte read = shstrtab[i++];
            while (read != 0x0000) {
                s.append((char) read);
                read = shstrtab[i++];
            }

            return s.toString();
        }

        private Section find(String name) {
            for (var s : sections) {
                if (getSectionName(s.name).equals(name)) {
                    return s;
                }
            }
            throw new IllegalArgumentException(String.format("No %s section in the elf file!", name));
        }

        public void parseStrtab() {
            Section tab = find(".strtab");
            strtab = new byte[tab.size];
            pointer = tab.offset;
            for (int i = 0; i < tab.size; i++) {
                strtab[i] = readByte();
            }
        }

        public String getString(int index) {
            int i = index;
            StringBuilder s = new StringBuilder();
            byte read = strtab[i++];
            while (read != 0x0000) {
                s.append((char) read);
                read = strtab[i++];
            }

            return s.toString();
        }

        public void parseSymtab() {
            Section symtab = find(".symtab");
            pointer = symtab.offset;
            for (int i = 0; i < symtab.size / symtab.entsize; i++) {
                symbolTable.add(
                        new SymtabEntry(
                                getString(readWord()),
                                readAddr(),
                                readWord(),
                                readByte(),
                                readByte(),
                                readHalf()
                        )
                );
            }
        }

        public void parseText() {
            Section text = find(".text");
            pointer = text.offset;
            int address = text.addr;
            for (int i = 0; i < text.size / 4; i++) {
                // representing an instruction as a boolean array
                boolean[] line = new boolean[32];
                for (int j = 0; j < 4; j++) {
                    short x = readByte();
                    for (int sh = 0; sh < 8; sh++) {
                        line[8 * j + sh] = (x == (x | (1 << sh)));
                    }
                }
                programText.add(
                        Commands.classify(line).setAddress(address)
                );
                address += 4;
            }
        }

        private void skip(int n) {
            for (int i = 0; i < n; i++) {
                readByte();
            }
        }

        private int readFourBytes() {
            int res = source.getInt(pointer);
            pointer += 4;
            return res;
        }

        private char readHalf() {
            char res = source.getChar(pointer);
            pointer += 2;
            return res;
        }

        private int readAddr() {
            return readFourBytes();
        }

        private int readWord() {
            return readFourBytes();
        }

        private int readOff() {
            return readFourBytes();
        }

        private byte readByte() {
            return source.get(pointer++);
        }

        @Override
        public void close() {
            //source.close();
        }
    }

    public static class Writer implements AutoCloseable {
        BufferedWriter writer;

        public Writer(String path) {
            try {
                this.writer = new BufferedWriter(new FileWriter(path));
            } catch (FileNotFoundException ex) {
                System.out.println("Error: file not found");
            } catch (IOException e) {
                System.out.println("I/O error");
            }
        }

        public Map<Integer, String> mapFromSymtab(List<SymtabEntry> symbolTable) {
            Map<Integer, String> map = new HashMap<>();
            for (SymtabEntry s : symbolTable) {
                map.put(s.value, s.name);
            }
            return map;
        }

        private void updateSymtab(List<Command> programText, Map<Integer, String> marks) {
            int num = 0;
            for (Command command : programText) {
                if ((command.name.type == Commands.Type.B || command.name == Commands.JAL)
                        && marks.get(command.getOffset()) == null) {
                    marks.put(command.getOffset(), String.format("L%d", num++));
                }
            }
        }

        public void writeText(List<Command> programText, List<SymtabEntry> symbolTable) throws IOException {
            writer.write(".text");
            writer.newLine();
            Map<Integer, String> marks = mapFromSymtab(symbolTable);
            updateSymtab(programText, marks);
            for (Command command : programText) {
                if (marks.get(command.address) != null) {
                    writer.write(String.format("%08x   <%s>:", command.address, marks.get(command.address)));
                    writer.newLine();
                }
                writer.write(String.format("   %05x:\t%08x\t%7s\t%s", command.address, command.getValue(),
                        command.name.toString().toLowerCase(), command.getArgumets(marks)));
                writer.newLine();
            }
            writer.newLine();
        }

        public void writeSymTab(List<SymtabEntry> symbolTable) throws IOException {
            writer.write(".symtab");
            writer.newLine();
            writer.write("Symbol   Value            Size    Type    Bind       Vis   Index  Name");
            writer.newLine();
            for (int i = 0; i < symbolTable.size(); i++) {
                writer.write(String.format("[%4d] ", i) + symbolTable.get(i));
                writer.newLine();
            }
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
