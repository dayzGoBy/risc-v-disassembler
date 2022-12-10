import java.io.*;
import java.util.List;

public class Disassemler {
    public void convert(String pathFrom, String pathTo) {
        try (
                Parser p = new Parser(pathFrom);
                Writer w = new Writer(pathTo)
        ){
            p.parseHeader();
            p.parseText();
            p.parseSymTable();
        } catch (IOException ex) {
            System.out.println("Error: input/output exception");
        }
    }

    public static class Parser implements AutoCloseable {
        DataInputStream source;
        public Parser(String path) {
            try {
                this.source = new DataInputStream(new FileInputStream(path));
            } catch (FileNotFoundException ex) {
                System.out.println("Error: file not found");
            }
        }

        public void parseHeader() throws IOException {
            for(byte b : new byte[]{0x7f, 0x45, 0x4c, 0x46}) {
                if (b != source.readByte()) {
                    throw new IllegalStateException("This file is not elf");
                }
            }
        }

        public void parseText() {

        }

        public void parseSymTable() {

        }

        @Override
        public void close() throws IOException {
            source.close();
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
            writer.write(".text");
            writer.newLine();
            // TODO: ???
            writer.newLine();
        }

        public void writeSymTab() throws IOException {
            writer.write(".text");
            writer.newLine();
            // TODO: ???
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
