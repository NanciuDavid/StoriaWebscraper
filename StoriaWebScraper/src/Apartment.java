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
    public boolean hasSchool = false;

    public Apartment(){};

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

    public float getLatitude() {
        return this.latitude;
    }

    public float getLongitude() {
        return this.longitude;
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
                '}';
    }
}
