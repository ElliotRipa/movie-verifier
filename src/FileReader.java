import java.io.File;			// Import the File class
import java.io.FileNotFoundException;	// Import this class to handle errors
import java.util.ArrayList;		// Import this class to create ArrayList
import java.util.Scanner;		// Import the Scanner class to read text files

public class FileReader {

    public static ArrayList<String> readFile(String path) {
        ArrayList<String> output = new ArrayList<>();
        try {
            File file = new File(path);
            Scanner scanner = new Scanner(file);
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                output.add(line);
            }
            scanner.close();
            return output;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }

}
