import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class Disassembler {
    public void convert(String pathFrom, String pathTo) {
        try (
                Parser p = new Parser(pathFrom);
                Writer w = new Writer(pathTo)
        ){
            p.parseHeaderAndSections();
            p.parseText();
            w.writeText();
            w.writeSymTab();
        } catch (IOException ex) {
            System.out.println("Error: input/output exception");
        }
    }

    public static class Parser implements AutoCloseable {
        File file;
        ByteBuffer source;
        byte dataEncoding;
        final int HALF_SIZE = 2;
        final int WORD_SIZE = 4;
        final int SWORD_SIZE = 4;
        final int ADDR_SIZE = 4;
        final int OFF_SIZE = 4;

        /*
        Addr = long
        Half = int
        Off = long
        Sword = int
        Word = long
        Unsigned char = char
         */
        int pointer = 0;
        int shoff;
        int shnum;
        int shstrndx;
        List<Section> sections = new ArrayList<>();
        byte[] shstrtab;
        byte[] strtab;

        public Parser(String path) {
            try {
                this.file = new File(path);
                this.source = ByteBuffer.wrap(Files.readAllBytes(file.toPath()));
                this.source.order(ByteOrder.LITTLE_ENDIAN);
            } catch (FileNotFoundException ex) {
                System.out.println("Error: file not found");
            } catch (IOException ex) {
                System.out.println("Error: IOException");
            }
        }

        public void parseHeaderAndSections() throws IOException {
            if (readWord() == 0x7f454c46) {
                throw error("This file is not elf");
            }
            skip(1); // EI_CLASS
            dataEncoding = readByte(); // Little endian must be
            skip(10); // EI_VERSION/PAD
            //END OF e_ident
            readHalf(); // e_type/machine
            readHalf();
            readWord(); // version
            readAddr(); // entry
            readOff(); // phoff
            shoff = readOff(); // shoff
            System.out.println("shoff = " + shoff);
            readWord(); // flags
            readHalf(); // ehsize
            readHalf();// phentsize
            readHalf(); // phnum
            readHalf(); // shentsize
            shnum = readHalf(); // shnum
            System.out.println("shnum = " + shnum);
            shstrndx = readHalf(); // shstrndx

            parseSectionHeader();
            parseSectionHeaderStrtab();
            System.out.println("Sections");
            for (var s : sections) {
                System.out.println(getSectionName(s.name));
            }
        }

        public void parseSectionHeader() {
            //there we can check that our header has correct offset
            pointer = shoff;
            for (int i = 0; i < shnum; i++) {
                sections.add(new Section(
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
                ));
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
            //for (int i = )
        }

        public void parseText() {
            Section text = find(".text");

        }

        private void skip(int n) {
            for (int i = 0; i < n; i++) {
                readByte();
            }
        }

        private int readFourBytes() {
            //  create a byte buffer and wrap the array
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

        private int readSword() {
            return readFourBytes();
        }

        private int readOff() {
            return readFourBytes();
        }

        private byte readByte() {
            return source.get(pointer++);
        }

        protected IllegalArgumentException error(final String message) {
            return new IllegalArgumentException(message);
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

        public void writeText() throws IOException {
            writer.write("; this is text section");
            writer.newLine();
            writer.write(".text");
            writer.newLine();
            // TODO: ???
            writer.newLine();
        }

        public void writeSymTab() throws IOException {
            writer.write("; this is symbol table");
            writer.newLine();
            writer.write(".symtab");
            writer.newLine();
            // TODO: ???
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
