import java.io.*;
import java.util.zip.*;

public class Decompress {
    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        try (FileInputStream fis = new FileInputStream(file);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             BufferedReader reader = new BufferedReader(new InputStreamReader(gzis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}