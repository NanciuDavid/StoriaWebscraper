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
    

    public static void main(String[] args) throws IOException, InterruptedException {


    // Hard-coded apartment for testing 
    Apartment apartment = new Apartment();
    apartment.setLatitude(44.367035f);
    apartment.setLongitude(26.139484f);

    Dotenv dotenv = Dotenv.configure().load();
    String apiKey = dotenv.get("GOOGLE_PLACES_API");

    //based on urban planning measurements
    float radiusHighImpact = 500f; //0 - 500m
    float radiusModerateImpact = 1500f; //500 - 1.5km
    float radiusLowImpact = 3000f; //1.5 - 3km

    if(searchForFacility(apartment, "school", 5000f, apiKey) == true){
        apartment.hasSchool = true;
    }

    System.out.println(apartment.hasSchool);

    }
    
    
        /**
         * Searches for facilities in the proximity using Google Places API by sending a GET Request.
         * @param apartment 
         * @param facility (schools / parks / malls / metro / etc.)
         * @param radius (sets the impact based on the urban planning measurements)
         * @param API_KEY
         * 
         * @return boolean if facility found in the proximity
         * @throws InterruptedException 
         * @throws IOException 
         */

        
        public static boolean searchForFacility(Apartment apartment, String facility, float radius, String API_KEY) throws IOException, InterruptedException {

        //setting the url based on the 
        String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
        + "?location=" + apartment.getLatitude() + "," + apartment.getLongitude()
        + "&radius=" + radius
        + "&type=" + facility
        + "&key=" +API_KEY;
        
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response: " + response.body().toString());

        //loop through results and store the schools in a JSONObjects Array
        try {
            JSONObject jsonObject = new JSONObject(response.body().toString());
            JSONArray results = jsonObject.getJSONArray("results");
            if(results.length() == 0) {
                System.out.println("No " + facility + " found in the proximity");
                return false;
            } else {
                System.out.println("Found " + results.length() + " " + facility + " in the proximity");
                return true;
            }
        } catch (JSONException e) {
            System.err.println("Could not extract the response body");
            return false;
        }
    }

}



