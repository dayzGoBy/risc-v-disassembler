import java.io.*;
import java.nio.charset.StandardCharsets;

public class Disassemler {
    public void parse(String path) {
        try (Parser p = new Parser(path);){
            p.parseHeader();
        } catch (IOException ex) {
            System.out.println("Error: input/output exception");
        }
    }

    public static class Parser implements AutoCloseable {
        BufferedReader source;
        public Parser(String path) {
            try {
                this.source = new BufferedReader(
                        new InputStreamReader(new FileInputStream(path))
                );
            } catch (FileNotFoundException ex) {
                System.out.println("Error: file not found");
            }
        }

        public void parseHeader() throws IOException {
            System.out.println(source.read());
        }

        public void parseData() {

        }  

        @Override
        public void close() throws IOException {
            source.close();
        }
    }
}
