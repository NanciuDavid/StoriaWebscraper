import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

public class DataFormatter {
    public static void main(String[] args) {
        String inputFilePath = "/Users/davidnanciu/Desktop/sanitized_apartments_address_truncate.csv"; // Path to input CSV file
        String outputFilePath = "/Users/davidnanciu/Desktop/modified_addresses.csv"; // Path to output CSV file

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            boolean skipHeader = true;
            
            
            while ((line = reader.readLine()) != null) {
                
                if (line.trim().isEmpty()) {
                    continue;
                }

                
                String[] row = line.split(";");
                
                
                if (row.length < 10) {
                    continue; 
                }

            
                if (skipHeader) {
                    writer.write(line); 
                    writer.newLine();
                    skipHeader = false;
                    continue;
                }

            
                try {
                    int value = Integer.parseInt(row[9]);
                    value -= 1; 
                    row[9] = String.valueOf(value); 
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid number at row: " + line);
                }

                String address = row[1];

                String[] addressParts = address.split(",");
                if (addressParts.length > 1) {
                    String city = addressParts[addressParts.length - 1].trim(); 
                    row[1] = city; 
                }
                
            
                writer.write(String.join(";", row));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
