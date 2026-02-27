---
title: Silent Sentry
teamName: " Aditya Sharma, Swayam Sonawane, Sumit Sonawane "
thumbnail: "/images/sillent-sentry.png"
type: "IoT"
description: Silent Sentry is an off-grid forest monitoring and early-warning system designed to detect illegal tree cutting in real time
isFinalist: false 
---
# Silent Sentry

## 1. Introduction
Forests are among the most critical yet vulnerable natural resources. Illegal tree cutting and forest intrusion often go undetected until irreversible damage has already occurred. The main reasons include vast forest areas, limited manpower, lack of internet connectivity, and absence of continuous power supply. Most existing solutions rely on continuous surveillance using cameras or drones, which are power-intensive, costly, and impractical in real forest environments. Therefore, there is a strong need for a low-power, intelligent, and deployable system that can detect illegal activity at the exact moment it begins. Silent Sentry is proposed as an off-grid, event-driven forest protection system that works even in remote locations without internet or grid power.

## 2. Core Idea of Silent Sentry
Silent Sentry transforms a tree into a smart sensing point that can detect illegal cutting activity in real time. Instead of monitoring continuously, Silent Sentry follows a Wake-on-Threat architecture:
* The system remains in deep sleep during normal conditions.
* It wakes up only when suspicious activity is detected.
* Alerts are generated only after multi-level verification.

This makes the solution energy-efficient, stealthy, and realistic for forest deployment.

## 3. Overall System Architecture
The system is divided into three main layers:

### A. Edge Layer (Sensor Node on Tree)
* ESP32 microcontroller
* Piezo vibration sensor
* Optional microphone for short audio confirmation
* LoRa communication module
* Solar-powered battery system

### B. Communication Layer
* Long-range LoRa communication
* Optional mesh routing between nodes

### C. Backend & Dashboard Layer
* LoRa gateway
* Python backend server
* GridDB time-series database
* Web-based command-center dashboard

## 4. Detailed Implementation: How We Will Build the System

### Step 1: Strategic Node Deployment
Sensor nodes are installed only in high-risk or high-value forest zones, such as sandalwood areas, protected reserves, and known smuggling routes. This targeted approach avoids unrealistic large-scale deployment.

### Step 2: Deep Sleep & Passive Monitoring
Each node stays in deep sleep mode to conserve power. The piezo vibration sensor passively monitors vibrations on the tree without active processing.

### Step 3: Wake-Up Trigger
When unusual vibration occurs:
* The piezo sensor generates a hardware interrupt.
* The ESP32 wakes up instantly.
* Power is consumed only for a short duration.

### Step 4: Signal Preprocessing
The ESP32 processes raw vibration data:
* Removes environmental noise.
* Extracts frequency and repetition patterns.
* Filters out irregular signals caused by wind or animals.

### Step 5: Pattern Recognition
Cutting tools such as axes and chainsaws produce high-energy, repetitive vibration patterns. These are compared against stored reference patterns to identify illegal cutting activity.

### Step 6: Multi-Modal Confirmation
To reduce false alerts:
* A microphone is activated for 2-3 seconds only.
* A TinyML model checks for chainsaw or tool-specific audio frequencies.
* The microphone remains OFF during normal conditions.

### Step 7: Threat Classification
Each detected event is classified with:
* Threat type (Axe / Chainsaw)
* Confidence score
* Timestamp
* Node ID

### Step 8: Location Tagging
Each node is assigned fixed GPS coordinates at deployment time.
* Location is not tracked continuously.
* Coordinates are attached only during alerts.

**Example alert packet**:
* Node ID: 12
* Latitude: 18.5204
* Longitude: 73.8567
* Threat: Chainsaw
* Confidence: 92%
* Timestamp: 10:41 AM

### Step 9: Alert Transmission via LoRa
Confirmed alerts are transmitted using LoRa, enabling:
* Long-range communication.
* Low power usage.
* Operation without internet.
* In dense forests, nearby nodes can forward alerts using mesh routing.

### Step 10: Data Storage Using GridDB
The LoRa gateway forwards data to the backend server. All events are stored in GridDB, which is optimized for time-series IoT data. Stored data includes:
* Timestamp
* Node ID
* Location
* Threat type
* Confidence score

### Step 11: Historical Validation & Intelligence
Before final escalation:
* Historical data from the same zone is analyzed.
* Repeated activity patterns are identified.
* False positives are reduced.

GridDB enables evidence-based decision making, not just instant alerts.

## 5. Unified Command-Center Dashboard
All system outputs are displayed on a single unified dashboard.

### Dashboard Features
**A. Live Alert Panel**
* Real-time threat notifications
* Exact location and threat details
* Confidence score and time of detection

**B. Node Location Mapping**
* Each node shown as a fixed marker on the map
* Color-coded status:
    * Green: Normal
    * Yellow: Suspicious
    * Red: Confirmed threat

**C. Risk Heatmap Visualization**
* Heatmaps generated using historical GridDB data
* High-frequency threat zones appear as hotspots
* Time filters (24 hours / 7 days / 30 days)

**D. Historical Analysis**
* View past alerts per node or region
* Identify smuggling routes and vulnerable zones

**E. Action Controls (Optional)**
* Trigger remote deterrent (siren/buzzer)
* Flag events for investigation
* Export data for reporting

## 6. Power Efficiency & Reliability
* Event-driven sensing (no continuous monitoring)
* Solar-powered energy system
* No always-on cameras or microphones
* Camouflaged hardware with minimal exposed components

The system can operate for months with minimal maintenance.

## 7. Impact and Use Cases

### Environmental Impact
* Prevents illegal logging before damage occurs
* Protects endangered and high-value tree species

### Operational Impact
* Faster response time
* Reduced manpower requirement
* Targeted patrol deployment

### Data-Driven Planning
* Risk-zone mapping
* Smuggling route identification
* Long-term conservation strategy support

## 8. Conclusion
Silent Sentry transforms forest protection from reactive discovery to proactive prevention. By listening silently, waking intelligently, and responding with evidence, the system enables forests to report threats the moment they occur, even in the most remote locations.

## 9.1 Use Case of the Project
Silent Sentry is designed for real-time detection of illegal tree cutting in remote forest regions where internet connectivity and grid power are unavailable. The system is deployed on selected high-risk and high-value trees and continuously monitors for cutting activity using vibration sensing. When illegal logging begins, Silent Sentry detects the activity immediately, verifies it using audio and visual confirmation, and alerts forest authorities with the exact location of the event. This enables rapid intervention, prevents large-scale deforestation, protects valuable tree species, and supports long-term forest management through historical data analysis and risk heatmaps.

## 9.2 Libraries, Frameworks, and Middleware Used

### Edge / Device Side
* Arduino Framework for ESP32 firmware development
* ESP-IDF/Arduino Core for ESP32
* TinyML (Edge Impulse) for audio-based tool detection

### Communication
* LoRa communication stack (SX1278 driver libraries)

### Backend & Data Layer
* Python for backend services and data processing
* GridDB Python Client API for database interaction

### Visualization & Dashboard
* Streamlit for interactive command-center dashboard
* Folium / map visualization libraries for node location mapping and heatmap generation

## 9.3 Use of GridDB Community Edition and GridDB Cloud
During the initial development and testing phase, GridDB Community Edition is used to prototype the system, validate data ingestion from multiple sensor nodes, and test historical analysis and heatmap generation. After successful validation, the system is designed to seamlessly migrate to GridDB Cloud, where it will store real-time and historical IoT event data at scale. GridDB Cloud enables high availability, scalability, and fast querying required for live dashboards, risk heatmaps, and long-term forest activity analysis during real-world deployment.