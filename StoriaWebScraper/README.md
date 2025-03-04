# Household Price Prediction Web App - Project Plan

## **1. Introduction**
This project aims to build a **web application** that predicts **future household prices** based on scraped real estate data. The app will:
- Allow users to input details about an apartment.
- Predict its future price based on an ML model.
- Show similar apartments for comparison.

---

## **2. Project Roadmap**
### **Phase 1: Web Scraping (✅ Done, but improvements needed)**
The scraper collects real estate listings from **Storia.ro** using **Selenium** and stores them in a CSV file.

#### **Current Implementation:**
✅ **Multithreading:** The scraper runs multiple threads to handle different page ranges simultaneously. 
- `ApartmentScraper.java` manages thread creation and execution.
- Each thread scrapes a set number of pages (`TOTAL_PAGES` divided by `THREAD_COUNT`).
- Uses `WebDriver` to navigate and extract data efficiently.

✅ **Page Navigation & Data Extraction:**
- `FetchApartments.java` locates apartment listings on the page.
- `AccesApartment.java` handles clicking elements reliably with retries.
- `AcceptCookies.java` ensures that cookies pop-ups do not block execution.

✅ **Apartment Details Extraction:**
- `ApartmentDetailsExtractor.java` fetches:
  - **Basic details** (price, surface, rooms, address, etc.).
  - **Geo-coordinates** (Google Maps API for lat/lon conversion).
  - **Additional features** (seller type, floor, property status, etc.).

✅ **Data Storage & Formatting:**
- `DataFormatter.java` structures the extracted CSV file.
- `ApartmentSanitizer.java` cleans, transforms, and prepares data for ML.

#### **Next Steps:**

🔲 **Extract More Data Features:**
- Implement **Google Places API** to determine proximity to key locations:
  - **Schools, Metro Stations, Malls, Parks, Commercial Zones**.
  - Use these as additional features for price prediction.

🔲 **Store Apartment Images:**
- Scrape apartment images and store them for use in the frontend.
- Save images to **AWS S3, Firebase Storage, or a dedicated DB column**.

🔲 **Optimize Data Storage:**
- Directly insert scraped data into **PostgreSQL/MongoDB** instead of using CSV.
- Implement a **data pipeline** to clean and transform data automatically.

---

### **Phase 2: Data Storage & API Development**
Scraped data needs to be **stored** and **served** via an API.

#### **TODO:**
🔲 **Set Up Database (PostgreSQL / MongoDB)**
- Create tables/collections:
  - `apartments` (id, price, rooms, surface, address, lat, lon, images, features)
  - `users` (id, name, email, saved searches)

🔲 **Develop Backend API (FastAPI or Express.js)**
- **Endpoints:**
  - `POST /upload-data` - Store scraped listings.
  - `GET /apartments?location=xyz` - Fetch apartments nearby.
  - `POST /predict` - Predict apartment price based on user input.

🔲 **Image Storage**
- Store images in **AWS S3, Cloudinary, or Firebase Storage**.

---

### **Phase 3: Machine Learning Model**
The ML model will predict **future prices** based on features.

#### **TODO:**
🔲 **Feature Engineering**
- Include `price`, `surface`, `rooms`, `location`, and `proximity to amenities`.
- Extract historical trends for prediction.

🔲 **Train & Evaluate Model**
- Use **Linear Regression, XGBoost, or LSTM (time-series forecasting)**.
- Train on historical data & evaluate using RMSE, R².

🔲 **Deploy ML Model**
- Serve via **FastAPI/Flask API**.
- Connect to backend API for real-time predictions.

---

### **Phase 4: Frontend Development**
The web app will have a **form** for user input and display predictions.

#### **TODO:**
🔲 **Frontend Stack:**
- **React.js (Next.js for SSR performance).**
- **TailwindCSS for UI.**

🔲 **User Features:**
- **Form:** Users enter apartment details (rooms, surface, location).
- **Price Prediction:** Show estimated future price.
- **Comparison Listings:** Display similar scraped apartments.
- **Map View:** Show nearby apartments on Google Maps.

🔲 **Connect Frontend to API**
- Fetch predictions via `POST /predict`.
- Show nearby listings via `GET /apartments`.

---

### **Phase 5: Deployment & Optimization**
Final step: **Make the app live!**

#### **TODO:**
🔲 **Deploy Backend (FastAPI/Express) on AWS/GCP/Heroku.**
🔲 **Deploy ML Model as a microservice.**
🔲 **Host Frontend on Vercel/Netlify.**
🔲 **Automate Scraper Execution (Cron Jobs or AWS Lambda).**

---
