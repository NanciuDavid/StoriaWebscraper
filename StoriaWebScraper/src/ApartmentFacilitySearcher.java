import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

public class ApartmentFacilitySearcher {

        /**
         * Computes the distance from the apartment to the nearest facility searched
         * @param lat1 
         * @param lon1
         * @param lat2
         * @param lon2
         * 
         * @return distance in meters
         */
    
        public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
            final int R = 6371; // Radius of the earth in km
            double latDistance = Math.toRadians(lat2 - lat1);
            double lonDistance = Math.toRadians(lon2 - lon1);
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = R * c;
            return distance * 1000; // Convert distance to meters
        }
    
        public static void main(String[] args) throws IOException, InterruptedException {
    
            // Hard-coded apartment for testing
            Apartment apartment = new Apartment();
            apartment.setLatitude(44.367035f);
            apartment.setLongitude(26.139484f);
    
            Dotenv dotenv = Dotenv.configure().load();
            String apiKey = dotenv.get("GOOGLE_PLACES_API");
    
            float radius = 3000f;
    
            // Search for nearest school
            double nearestSchoolDistance = searchForSchools(apartment, radius, apiKey);
            if (nearestSchoolDistance > 0) {
                System.out.println("Nearest school is " + nearestSchoolDistance + " meters away.");
            } else {
                System.out.println("No schools found in the proximity.");
            }
        }
    
        /**
         * Searches for schools in the proximity using Google Places API by sending a
         * GET Request.
         * 
         * @param apartment
         * @param radius    (sets the impact based on the urban planning measurements)
         * @param API_KEY
         * 
         * @return distance to the nearest school in meters, or -1 if no school found
         * @throws InterruptedException
         * @throws IOException
         */
    
        public static double searchForSchools(Apartment apartment, float radius, String API_KEY)
                throws IOException, InterruptedException {
    
            // setting the URL based on the
            String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                    + "?location=" + apartment.getLatitude() + "," + apartment.getLongitude()
                    + "&radius=" + radius
                    + "&type=" + "school"
                    + "&key=" + API_KEY;
    
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .GET()
                    .build();
    
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
            System.out.println("Response: " + response.body().toString());
    
            try {
                JSONObject jsonObject = new JSONObject(response.body().toString());
                JSONArray results = jsonObject.getJSONArray("results");
    
                if (results.length() == 0) {
                    System.out.println("No schools found in the proximity");
                    return -1; // No schools found
                } else {
                    System.out.println("Found " + results.length() + " schools in the proximity\n");
    
                    double nearestSchoolDistance = Double.MAX_VALUE; // Start with a very large number
    
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject school = results.getJSONObject(i);
                        String schoolName = school.getString("name");
                        JSONObject location = school.getJSONObject("geometry").getJSONObject("location");
                        double schoolLat = location.getDouble("lat");
                        double schoolLng = location.getDouble("lng");
    
                        // Calculate the distance to this school
                        double distance = calculateDistance(apartment.getLatitude(), apartment.getLongitude(), schoolLat,
                                schoolLng);
    
                        // Update nearest school distance if this one is closer
                        if (distance < nearestSchoolDistance) {
                            nearestSchoolDistance = distance;
                        }
                    }
    
                    // Return the nearest distance
                    return nearestSchoolDistance;
                }
            } catch (JSONException e) {
                System.err.println("Could not extract the response body");
                return -1; // Error or failure to process
            }
        }
    }
    
