import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

public class DataFormatter {

    /**
     * Main method to format data from a CSV file.
     * Reads data, adjusts values, truncates addresses, and writes modified data to a new CSV file.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        // Define input and output file paths
        String inputFilePath = "/Users/davidnanciu/Desktop/sanitized_apartments_address_truncate.csv";
        String outputFilePath = "/Users/davidnanciu/Desktop/modified_addresses.csv";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            boolean skipHeader = true;

            // Read each line from the input file
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                String[] row = line.split(";");
                if (row.length < 10) {
                    continue; // Skip rows with insufficient columns
                }

                if (skipHeader) {
                    writer.write(line); // Write header to output
                    writer.newLine();
                    skipHeader = false;
                    continue;
                }

                // Adjust and truncate values in the row
                adjustValue(row);
                truncateAddress(row);

                // Write the modified row to the output file
                writer.write(String.join(";", row));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adjusts the value in a specific column of the row.
     * Decrements the value by 1 if it is a valid number.
     * @param row Array of strings representing a CSV row
     */
    private static void adjustValue(String[] row) {
        try {
            int value = Integer.parseInt(row[9]);
            value -= 1; // Decrement the value by 1
            row[9] = String.valueOf(value);
        } catch (NumberFormatException e) {
            System.err.println("Skipping invalid number at row: " + String.join(";", row));
        }
    }

    /**
     * Truncates the address in the row to only include the city name.
     * Extracts the city from the full address string.
     * @param row Array of strings representing a CSV row
     */
    private static void truncateAddress(String[] row) {
        String address = row[1];
        String[] addressParts = address.split(",");
        if (addressParts.length > 1) {
            String city = addressParts[addressParts.length - 1].trim(); // Extract city name
            row[1] = city;
        }
    }
}
