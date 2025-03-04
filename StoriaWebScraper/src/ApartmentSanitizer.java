import de.siegmar.fastcsv.reader.CsvIndex;
import de.siegmar.fastcsv.reader.CsvRecord;
import de.siegmar.fastcsv.reader.IndexedCsvReader;
import de.siegmar.fastcsv.writer.CsvWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

//the headers from the apartments.csv files are

//Description,Address,Rent,Additional infomation,Seller type,Free from,Property type,URL,Surface,Rooms, Property form,Price,Status,Heating type, Floor
//      0       1       2            3                4           5       6            7      8    9       10           11     12     13      14

//we need to sanitize the data in order to apply machine learning algorithms
// by removing the columns that are not useful and convert categorical data to numerical data :

//in order to keep only the categorical data , the url column will be removed among with the Rent, Free From, Property form, columns which contain little to no information

//the address will contain only the city name which will be converted to a number based on the city name

//the additional information will be formated like this : 1 by default ( assuming the "fara informatii" apartments also have a balcony)
//                                                        or x where x is the number of additional features provided like balcony, parking lot, etc.


//the seller type will be formated like this : 0 by default ( assuming the "fara informatii" apartments are sold by an individual)
//                                             1 if the seller is "agenție"
//                                             2 if the seller is "dezvoltator"

//the property type will be formated like this : 0 if the property is "locuință nouă"
//                                               1 if the property is "locuință utilizată"

//Surface and Rooms columns will be converted to integers

//Price will be converted to a float

//The status will be formated like this : 0 if the status is "gata de utilizare or fără informații"
//                                        1 if the status is "în construcție or "necesită renovare"


//the heating type will be formated like this : 0 if the heating type is "fără informații"
//                                              1 if the heating type is "centrală proprie or "centralizată", or "centrală pe gaz"

//the floor will be converted like this : if the floor value is "parter" it is converted to 0
//                                         if the floor value is "demisol" it is converted to -1
//                                         if the floor value is "mansardă" it is converted to -2
//                                         otherwise the floor value is converted from "x / y" to x

public class ApartmentSanitizer {

    /**
     * Main method to sanitize apartment data from a CSV file.
     * Reads data, processes each record, and writes sanitized data to a new CSV file.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Path inputFilePath = Paths.get("apartments.csv");
        Path outputFilePath = Paths.get("sanitized_apartments.csv");

        try (IndexedCsvReader<CsvRecord> csvReader = IndexedCsvReader.builder()
                .pageSize(100) // process 100 rows at a time
                .ofCsvRecord(inputFilePath);
            CsvWriter csvWriter = CsvWriter.builder().build(outputFilePath)) {

            CsvIndex index = csvReader.getIndex();
            System.out.printf("Indexed %,d records.%n", index.getRecordCount());

            // Write sanitized headers to the output file
            csvWriter.writeRecord("Description", "Address","Longitude", "Latitude", "Seller type", "Property type",
                    "Surface", "Rooms", "Additional information", "Price",
                    "Status", "Heating type", "Floor");

            // Process each page of records
            int pageCount = index.getPageCount();
            for (int page = 0; page < pageCount; page++) {
                List<CsvRecord> records = csvReader.readPage(page);

                for (CsvRecord record : records) {
                    try {
                        String[] sanitizedRecord = sanitizeRecord(record);
                        csvWriter.writeRecord(sanitizedRecord);
                    } catch (Exception e) {
                        System.err.println("Error processing row: " + e.getMessage());
                    }
                }
            }

            System.out.println("Sanitization complete. Output written to " + outputFilePath);

        } catch (IOException e) {
            System.err.println("Error processing CSV file: " + e.getMessage());
        }
    }

    /**
     * Sanitizes a single CSV record by converting and formatting its fields.
     * Handles missing or invalid data gracefully.
     * @param record CsvRecord to sanitize
     * @return String array of sanitized record fields
     */
    private static String[] sanitizeRecord(CsvRecord record) {
        // Check if any field in the record is null or empty
        for (int i = 0; i < record.getFieldCount(); i++) {
            if (record.getField(i) == null || record.getField(i).isEmpty()) {
                return null; // Return null to indicate this row should be skipped
            }
        }

        // Handle default or missing values for fields beyond available columns
        int fieldCount = record.getFieldCount();
        String floorValue = (fieldCount > 14 && record.getField(14) != null) ? record.getField(14) : "fără informații";

        // Debugging: print out the value at index 15 to check if it's the price
        System.out.println("Value at index 14: " + floorValue);

        String[] sanitizedRecord = new String[11];

        // Description
        sanitizedRecord[0] = record.getField(0);

        // Address (extract city name and convert to number - placeholder logic provided)
        sanitizedRecord[1] = record.getField(1);

        // Seller type
        String sellerType = record.getField(4);
        sanitizedRecord[2] = sellerType.equals("fără informații") ? "0" :
                sellerType.equals("agenție") ? "1" :
                        sellerType.equals("dezvoltator") ? "2" : "0";

        // Property type
        String propertyType = record.getField(6);
        sanitizedRecord[3] = propertyType.equals("locuință nouă") ? "0" : "1";

        // Surface
        String surfaceField = record.getField(8).replaceAll("[^0-9,]", "");
        if (surfaceField.contains(",")) {
            surfaceField = surfaceField.replace(",", ".");
        }

        // Convert the surface value to a float
        float surfaceValue = Float.parseFloat(surfaceField);

        // Check if the surface is greater than 1000 for outliers
        if (surfaceValue > 1000) {
            surfaceValue = surfaceValue / 100;
            sanitizedRecord[10] = String.valueOf(surfaceValue);
        }

        sanitizedRecord[4] = String.valueOf(surfaceValue);

        // Rooms
        sanitizedRecord[5] = record.getField(9).replaceAll("[^0-9]", "");

        // Additional information
        String additionalInfo = record.getField(3).trim();
        sanitizedRecord[6] = additionalInfo.equals("fără informații") ? "1" :
                String.valueOf(additionalInfo.split("\\s+").length); // Handle multiple spaces correctly

        // Price
        sanitizedRecord[7] = record.getField(11).replaceAll("[^0-9.]", "");

        // Status
        String status = record.getField(12);
        sanitizedRecord[8] = status.equals("gata de utilizare") || status.equals("fără informații") ? "0" : "1";

        // Heating type
        String heatingType = record.getField(13);
        sanitizedRecord[9] = heatingType.equals("fără informații") ? "0" :
                heatingType.matches("centrală proprie|centralizată|centrală pe gaz") ? "1" : "2";

        // Floor (extract first part before slash and handle it normally)
        String floorBeforeSlash = floorValue.split("/")[0].trim(); // Get the part before the "/"
        sanitizedRecord[10] = handleFloor(floorBeforeSlash);

        return sanitizedRecord;
    }

    /**
     * Handles the conversion of floor values to a standardized format.
     * Converts specific floor descriptions to numerical values.
     * @param floorValue String representation of the floor
     * @return String numerical representation of the floor
     */
    private static String handleFloor(String floorValue) {
        // Handle specific floor values (parter, demisol, mansardă, etc.)
        if (floorValue.equalsIgnoreCase("parter")) {
            return "0"; // Ground floor
        } else if (floorValue.equalsIgnoreCase("demisol")) {
            return "-1"; // Basement
        } else if (floorValue.equalsIgnoreCase("mansardă")) {
            return "-2"; // Attic
        }

        // For any other case, try to parse it as an integer (should be a floor number)
        try {
            int floor = Integer.parseInt(floorValue);
            return String.valueOf(floor); // Return the floor number as a string
        } catch (NumberFormatException e) {

            return "0";
        }
    }



}