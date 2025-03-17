import java.util.*;
import java.time.LocalDateTime;

public class Apartment {
    private String header;
    private float price;
    private float pricePerSquareMeter;
    private float surface;
    private int rooms;
    private String address;
    private float latitude;
    private float longitude;
    private String url;
    private LocalDateTime extractionDate;
    private String floor;
    private float rent;
    private List<String> additionalInformation;
    private String sellerType;
    private String freeFrom;
    private String typeOfProperty;
    private String formOfProperty;
    private String status;
    private String heatingType;
    
    // Add new fields for facility data
    private Map<String, Boolean> facilityFlags;
    private Map<String, Double> facilityDistances;
    private int accessibilityScore;
    
    public Apartment(){
        facilityFlags = new HashMap<>();
        facilityDistances = new HashMap<>();
        additionalInformation = new ArrayList<>();
    }

    public Apartment(HashMap<String, String> apartmentDetails) {
        this.header = apartmentDetails.getOrDefault("Header", "N/A");
        this.price = parseFloatOrDefault(apartmentDetails.get("Price"), 0.0f);
        this.pricePerSquareMeter = parseFloatOrDefault(apartmentDetails.get("Price Per Square Meter"), 0.0f);
        this.surface = parseFloatOrDefault(apartmentDetails.get("Surface"), 0.0f);
        this.rooms = parseIntOrDefault(apartmentDetails.get("Rooms"), 0);
        this.address = apartmentDetails.getOrDefault("Address", "N/A");
        this.latitude = parseFloatOrDefault(apartmentDetails.get("Latitude"), 0.0f);
        this.longitude = parseFloatOrDefault(apartmentDetails.get("Longitude"), 0.0f);
        this.url = apartmentDetails.getOrDefault("URL", "N/A");
        this.extractionDate = parseDateOrDefault(apartmentDetails.get("Extraction Date"));
        this.floor = apartmentDetails.getOrDefault("Etaj:", "N/A");
        this.rent = parseFloatOrDefault(apartmentDetails.get("Chirie:"), 0.0f);
        this.additionalInformation = parseAdditionalInformation(apartmentDetails.get("Informații suplimentare:"));
        this.sellerType = apartmentDetails.getOrDefault("Tip vânzător:", "N/A");
        this.freeFrom = apartmentDetails.getOrDefault("Liber de la:", "N/A");
        this.typeOfProperty = apartmentDetails.getOrDefault("Tip proprietate:", "N/A");
        this.formOfProperty = apartmentDetails.getOrDefault("Forma de proprietate:", "N/A");
        this.status = apartmentDetails.getOrDefault("Stare:", "N/A");
        this.heatingType = apartmentDetails.getOrDefault("Încălzire:", "N/A");
        
        // Initialize facility maps
        this.facilityFlags = new HashMap<>();
        this.facilityDistances = new HashMap<>();
    }   

    // Getters for all fields
    public String getHeader() {
        return header;
    }

    public float getPrice() {
        return price;
    }

    public float getPricePerSquareMeter() {
        return pricePerSquareMeter;
    }

    public float getSurface() {
        return surface;
    }

    public int getRooms() {
        return rooms;
    }

    public String getAddress() {
        return address;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getExtractionDate() {
        return extractionDate;
    }

    public String getFloor() {
        return floor;
    }

    public float getRent() {
        return rent;
    }

    public List<String> getAdditionalInformation() {
        return additionalInformation;
    }

    public String getSellerType() {
        return sellerType;
    }

    public String getFreeFrom() {
        return freeFrom;
    }

    public String getTypeOfProperty() {
        return typeOfProperty;
    }

    public String getFormOfProperty() {
        return formOfProperty;
    }

    public String getStatus() {
        return status;
    }

    public String getHeatingType() {
        return heatingType;
    }

    // Setters for all fields
    public void setHeader(String header) {
        this.header = header;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setPricePerSquareMeter(float pricePerSquareMeter) {
        this.pricePerSquareMeter = pricePerSquareMeter;
    }

    public void setSurface(float surface) {
        this.surface = surface;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLatitude(float latitude) {
        if(latitude >= -90 && latitude <= 90) {
            this.latitude = latitude;
        }
    }

    public void setLongitude(float longitude) {
        if(longitude >= -180 && longitude <= 180) {
            this.longitude = longitude;
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setExtractionDate(LocalDateTime extractionDate) {
        this.extractionDate = extractionDate;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public void setRent(float rent) {
        this.rent = rent;
    }

    public void setAdditionalInformation(List<String> additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public void setSellerType(String sellerType) {
        this.sellerType = sellerType;
    }

    public void setFreeFrom(String freeFrom) {
        this.freeFrom = freeFrom;
    }

    public void setTypeOfProperty(String typeOfProperty) {
        this.typeOfProperty = typeOfProperty;
    }

    public void setFormOfProperty(String formOfProperty) {
        this.formOfProperty = formOfProperty;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHeatingType(String heatingType) {
        this.heatingType = heatingType;
    }

    private float parseFloatOrDefault(String value, float defaultValue) {
        try {
            return (value != null && !value.equals("fara informatii")) ? Float.parseFloat(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return (value != null && !value.equals("fara informatii")) ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private LocalDateTime parseDateOrDefault(String value) {
        try {
            return (value != null && !value.equals("N/A")) ? LocalDateTime.parse(value) : LocalDateTime.now();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private List<String> parseAdditionalInformation(String value) {
        return (value != null && !value.equals("fara informatii")) ? Arrays.asList(value.split(",")) : new ArrayList<>();
    }

    // Facility data getters and setters
    public Map<String, Boolean> getFacilityFlags() {
        return facilityFlags;
    }
    
    public void setFacilityFlags(Map<String, Boolean> facilityFlags) {
        this.facilityFlags = facilityFlags;
    }
    
    public Map<String, Double> getFacilityDistances() {
        return facilityDistances;
    }
    
    public void setFacilityDistances(Map<String, Double> facilityDistances) {
        this.facilityDistances = facilityDistances;
    }
    
    public int getAccessibilityScore() {
        return accessibilityScore;
    }
    
    public void setAccessibilityScore(int accessibilityScore) {
        this.accessibilityScore = accessibilityScore;
    }
    
    // Helper methods to add individual facility data
    public void addFacilityFlag(String facilityType, boolean isNearby) {
        if (facilityFlags == null) {
            facilityFlags = new HashMap<>();
        }
        facilityFlags.put(facilityType, isNearby);
    }
    
    public void addFacilityDistance(String facilityType, double distance) {
        if (facilityDistances == null) {
            facilityDistances = new HashMap<>();
        }
        facilityDistances.put(facilityType, distance);
    }

    @Override
    public String toString() {
        return "Apartment{" + "\n" +
                "header='" + header + "\n" +
                ", price=" + price + "\n" +
                ", pricePerSquareMeter=" + pricePerSquareMeter + "\n" +
                ", surface=" + surface + "\n" +
                ", rooms=" + rooms + "\n" +
                ", address='" + address + '\'' + "\n" +
                ", latitude=" + latitude + "\n" +
                ", longitude=" + longitude + "\n" +
                ", url='" + url + '\'' + "\n" +
                ", extractionDate=" + extractionDate + "\n" +
                ", floor='" + floor + '\'' + "\n" +
                ", rent=" + rent + "\n" +
                ", additionalInformation=" + additionalInformation + "\n" +
                ", sellerType='" + sellerType + '\'' + "\n" +
                ", freeFrom='" + freeFrom + '\'' + "\n" +
                ", typeOfProperty='" + typeOfProperty + '\'' + "\n" +
                ", formOfProperty='" + formOfProperty + '\'' + "\n" +
                ", status='" + status + '\'' + "\n" +
                ", heatingType='" + heatingType + '\'' + "\n" +
                ", facilityFlags=" + facilityFlags + "\n" +
                ", facilityDistances=" + facilityDistances + "\n" +
                ", accessibilityScore=" + accessibilityScore + "\n" +
                '}';
    }

    public Map<String, String> getAllFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("Header", header);
        fields.put("Price", String.valueOf(price));
        fields.put("Price Per Square Meter", String.valueOf(pricePerSquareMeter));
        fields.put("Surface", String.valueOf(surface));
        fields.put("Rooms", String.valueOf(rooms));
        fields.put("Address", address);
        fields.put("Latitude", String.valueOf(latitude));
        fields.put("Longitude", String.valueOf(longitude));
        fields.put("URL", url);
        fields.put("Extraction Date", extractionDate.toString());
        fields.put("Floor", floor);
        fields.put("Rent", String.valueOf(rent));
        fields.put("Additional Information", String.join(", ", additionalInformation));
        fields.put("Seller Type", sellerType);
        fields.put("Free From", freeFrom);
        fields.put("Type Of Property", typeOfProperty);
        fields.put("Form Of Property", formOfProperty);
        fields.put("Status", status);
        fields.put("Heating Type", heatingType);
        
        // Add each facility flag as a separate field
        for (Map.Entry<String, Boolean> entry : facilityFlags.entrySet()) {
            fields.put("Flag_" + entry.getKey(), String.valueOf(entry.getValue()));
        }
        
        // Add each facility distance as a separate field
        for (Map.Entry<String, Double> entry : facilityDistances.entrySet()) {
            fields.put("Distance_" + entry.getKey(), String.valueOf(entry.getValue()));
        }
        
        fields.put("Accessibility Score", String.valueOf(accessibilityScore));
        
        return fields;
    }
}
