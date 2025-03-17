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

    private static final HttpClient client = HttpClient.newHttpClient();
    private static String apiKey;
    
    static {
        Dotenv dotenv = Dotenv.configure().load();
        apiKey = dotenv.get("GOOGLE_PLACES_API");
    }

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

        float radius = 3000f;

        // Search for various facilities
        double nearestSchoolDistance = searchForSchools(apartment, radius);
        double nearestParkDistance = searchForParks(apartment, radius);
        double nearestPublicTransportDistance = searchForPublicTransport(apartment, radius);
        double nearestSupermarketDistance = searchForSupermarkets(apartment, radius);
        double nearestHospitalDistance = searchForHospitals(apartment, radius);
        double nearestRestaurantDistance = searchForRestaurants(apartment, radius);
        double nearestGymDistance = searchForGyms(apartment, radius);
        double nearestShoppingMallDistance = searchForShoppingMalls(apartment, radius);

        // Display results
        System.out.println("Facility Distances from Property:");
        System.out.println("--------------------------------");
        printDistanceInfo("School", nearestSchoolDistance);
        printDistanceInfo("Park", nearestParkDistance);
        printDistanceInfo("Public Transport", nearestPublicTransportDistance);
        printDistanceInfo("Supermarket", nearestSupermarketDistance);
        printDistanceInfo("Hospital", nearestHospitalDistance);
        printDistanceInfo("Restaurant", nearestRestaurantDistance);
        printDistanceInfo("Gym", nearestGymDistance);
        printDistanceInfo("Shopping Mall", nearestShoppingMallDistance);
    }
    
    private static void printDistanceInfo(String facilityType, double distance) {
        if (distance > 0) {
            System.out.printf("Nearest %s is %.1f meters away.%n", facilityType, distance);
        } else {
            System.out.printf("No %s found within search radius.%n", facilityType);
        }
    }

    /**
     * Searches for schools in the proximity using Google Places API by sending a
     * GET Request.
     * 
     * @param apartment
     * @param radius    (sets the impact based on the urban planning measurements)
     * 
     * @return distance to the nearest school in meters, or -1 if no school found
     * @throws InterruptedException
     * @throws IOException
     */
    public static double searchForSchools(Apartment apartment, float radius)
            throws IOException, InterruptedException {
        return searchForFacility(apartment, radius, "school");
    }
    
    /**
     * Searches for parks and green spaces in the proximity.
     * 
     * @param apartment
     * @param radius
     * 
     * @return distance to the nearest park in meters, or -1 if none found
     * @throws InterruptedException
     * @throws IOException
     */
    public static double searchForParks(Apartment apartment, float radius)
            throws IOException, InterruptedException {
        return searchForFacility(apartment, radius, "park");
    }
    
    /**
     * Searches for public transport stations (bus, subway, tram).
     * 
     * @param apartment
     * @param radius
     * 
     * @return distance to the nearest transit station in meters, or -1 if none found
     * @throws InterruptedException
     * @throws IOException
     */
    public static double searchForPublicTransport(Apartment apartment, float radius)
            throws IOException, InterruptedException {
        // For public transport, we'll check for transit stations
        return searchForFacility(apartment, radius, "transit_station");
    }
    
    /**
     * Searches for supermarkets and grocery stores.
     * 
     * @param apartment
     * @param radius
     * 
     * @return distance to the nearest supermarket in meters, or -1 if none found
     * @throws InterruptedException
     * @throws IOException
     */
    public static double searchForSupermarkets(Apartment apartment, float radius)
            throws IOException, InterruptedException {
        return searchForFacility(apartment, radius, "supermarket");
    }
    
    /**
     * Searches for hospitals and healthcare facilities.
     * 
     * @param apartment
     * @param radius
     * 
     * @return distance to the nearest hospital in meters, or -1 if none found
     * @throws InterruptedException
     * @throws IOException
     */
    public static double searchForHospitals(Apartment apartment, float radius)
            throws IOException, InterruptedException {
        return searchForFacility(apartment, radius, "hospital");
    }
    
    /**
     * Searches for restaurants and dining options.
     * 
     * @param apartment
     * @param radius
     * 
     * @return distance to the nearest restaurant in meters, or -1 if none found
     * @throws InterruptedException
     * @throws IOException
     */
    public static double searchForRestaurants(Apartment apartment, float radius)
            throws IOException, InterruptedException {
        return searchForFacility(apartment, radius, "restaurant");
    }
    
    /**
     * Searches for gyms and fitness centers.
     * 
     * @param apartment
     * @param radius
     * 
     * @return distance to the nearest gym in meters, or -1 if none found
     * @throws InterruptedException
     * @throws IOException
     */
    public static double searchForGyms(Apartment apartment, float radius)
            throws IOException, InterruptedException {
        return searchForFacility(apartment, radius, "gym");
    }
    
    /**
     * Searches for shopping malls and retail centers.
     * 
     * @param apartment
     * @param radius
     * 
     * @return distance to the nearest shopping mall in meters, or -1 if none found
     * @throws InterruptedException
     * @throws IOException
     */
    public static double searchForShoppingMalls(Apartment apartment, float radius)
            throws IOException, InterruptedException {
        return searchForFacility(apartment, radius, "shopping_mall");
    }
    
    /**
     * Generic method to search for any type of facility.
     * 
     * @param apartment
     * @param radius
     * @param facilityType the type of facility to search for (uses Google Places API types)
     * 
     * @return distance to the nearest facility in meters, or -1 if none found
     * @throws InterruptedException
     * @throws IOException
     */
    public static double searchForFacility(Apartment apartment, float radius, String facilityType)
            throws IOException, InterruptedException {
        
        // Building the URL for the Places API request
        String urlString = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                + "?location=" + apartment.getLatitude() + "," + apartment.getLongitude()
                + "&radius=" + radius
                + "&type=" + facilityType
                + "&key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        try {
            JSONObject jsonObject = new JSONObject(response.body());
            JSONArray results = jsonObject.getJSONArray("results");

            if (results.length() == 0) {
                return -1; // No facilities found
            } else {
                double nearestDistance = Double.MAX_VALUE;

                for (int i = 0; i < results.length(); i++) {
                    JSONObject facility = results.getJSONObject(i);
                    JSONObject location = facility.getJSONObject("geometry").getJSONObject("location");
                    double facilityLat = location.getDouble("lat");
                    double facilityLng = location.getDouble("lng");

                    // Calculate the distance to this facility
                    double distance = calculateDistance(apartment.getLatitude(), apartment.getLongitude(), 
                            facilityLat, facilityLng);

                    // Update nearest distance if this one is closer
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                    }
                }

                return nearestDistance;
            }
        } catch (JSONException e) {
            System.err.println("Could not extract the response body for " + facilityType + ": " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Evaluates the overall accessibility score based on proximity to essential facilities.
     * Returns a score from 0-100 with higher values indicating better accessibility.
     * 
     * @param apartment The apartment to evaluate
     * @return An accessibility score from 0-100
     */
    public static int calculateAccessibilityScore(Apartment apartment) throws IOException, InterruptedException {
        float radius = 5000f; // Expanded radius for more comprehensive evaluation
        
        // Get distances to various facilities
        double schoolDist = searchForSchools(apartment, radius);
        double parkDist = searchForParks(apartment, radius);
        double transportDist = searchForPublicTransport(apartment, radius);
        double supermarketDist = searchForSupermarkets(apartment, radius);
        double hospitalDist = searchForHospitals(apartment, radius);
        
        // Convert distances to scores (closer = higher score)
        int schoolScore = calculateProximityScore(schoolDist, 1000);
        int parkScore = calculateProximityScore(parkDist, 1000);
        int transportScore = calculateProximityScore(transportDist, 500); // Public transport should be closer
        int supermarketScore = calculateProximityScore(supermarketDist, 800);
        int hospitalScore = calculateProximityScore(hospitalDist, 2000);
        
        // Weight the scores based on importance
        int weightedScore = (int)((transportScore * 0.3) + 
                                 (supermarketScore * 0.25) + 
                                 (schoolScore * 0.2) + 
                                 (parkScore * 0.15) + 
                                 (hospitalScore * 0.1));
        
        return Math.min(100, weightedScore); // Cap at 100
    }
    
    /**
     * Converts a distance to a score from 0-100, with shorter distances getting higher scores.
     * 
     * @param distance Distance in meters
     * @param optimalDistance The distance considered "perfect" (gets 100 points)
     * @return A score from 0-100
     */
    private static int calculateProximityScore(double distance, double optimalDistance) {
        if (distance < 0) {
            return 0; // Facility not found
        }
        
        if (distance <= optimalDistance) {
            // If closer than optimal, give full or nearly full score
            return (int)(100 - (distance / optimalDistance) * 20);
        } else {
            // Score decreases as distance increases beyond optimal
            double factor = Math.min(1.0, (distance - optimalDistance) / (5 * optimalDistance));
            return (int)(80 * (1 - factor));
        }
    }
    
    /**
     * Updates the apartment object with information about nearby facilities
     * Useful for adding this data to your model features
     * 
     * @param apartment The apartment to enrich with facility data
     * @throws IOException
     * @throws InterruptedException
     */
    public static void enrichApartmentWithFacilityData(Apartment apartment) throws IOException, InterruptedException {
        float radius = 3000f;
        
        // Check each facility type and add both distance and boolean flags
        double schoolDist = searchForSchools(apartment, radius);
        apartment.addFacilityFlag("hasSchoolNearby", schoolDist > 0 && schoolDist < 1000);
        apartment.addFacilityDistance("schoolDistance", schoolDist);
        
        double parkDist = searchForParks(apartment, radius);
        apartment.addFacilityFlag("hasParkNearby", parkDist > 0 && parkDist < 800);
        apartment.addFacilityDistance("parkDistance", parkDist);
        
        double transportDist = searchForPublicTransport(apartment, radius);
        apartment.addFacilityFlag("hasPublicTransportNearby", transportDist > 0 && transportDist < 500);
        apartment.addFacilityDistance("publicTransportDistance", transportDist);
        
        double supermarketDist = searchForSupermarkets(apartment, radius);
        apartment.addFacilityFlag("hasSupermarketNearby", supermarketDist > 0 && supermarketDist < 800);
        apartment.addFacilityDistance("supermarketDistance", supermarketDist);
        
        double hospitalDist = searchForHospitals(apartment, radius);
        apartment.addFacilityFlag("hasHospitalNearby", hospitalDist > 0 && hospitalDist < 2000);
        apartment.addFacilityDistance("hospitalDistance", hospitalDist);
        
        double restaurantDist = searchForRestaurants(apartment, radius);
        apartment.addFacilityFlag("hasRestaurantNearby", restaurantDist > 0 && restaurantDist < 1000);
        apartment.addFacilityDistance("restaurantDistance", restaurantDist);
        
        double gymDist = searchForGyms(apartment, radius);
        apartment.addFacilityFlag("hasGymNearby", gymDist > 0 && gymDist < 1200);
        apartment.addFacilityDistance("gymDistance", gymDist);
        
        double mallDist = searchForShoppingMalls(apartment, radius);
        apartment.addFacilityFlag("hasShoppingMallNearby", mallDist > 0 && mallDist < 2000);
        apartment.addFacilityDistance("shoppingMallDistance", mallDist);
        
        // Calculate and set the accessibility score
        int score = calculateAccessibilityScore(apartment);
        apartment.setAccessibilityScore(score);
    }
}