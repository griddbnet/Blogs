---
title: Vital Shield
teamName: "parth mehta"
thumbnail: "/images/vital-shield-gallery.jpg"
type: "IoT"
description: "VitalShield is an AI-assisted, real-time patient monitoring platform that turns ordinary hospital rooms into smart, life-saving environments.  "
isFinalist: false 
---

# 🛡️ VitalShield — Idea Submission Document

## ⭐ 1. Why I Chose This Idea (Story + Problem Statement)
The idea for VitalShield came from noticing real gaps in how we care for people—both at home and inside hospitals.

### A. Elderly safety at home
In my own family, I’ve seen elderly members suffer serious injuries simply because they slipped in the bathroom or lost balance when no one was around. A fall that could have been handled in seconds often turned into a 20–30 minute delay because nobody even knew it happened.

### B. Limited monitoring in non-ICU hospital wards
During hospital visits, I noticed that patient vitals like heart rate, SpO₂, and temperature are checked only when a nurse comes around—or when the monitors finally start beeping, which often means the situation has already become critical.

If the nurse is busy or attending another patient, small anomalies go unnoticed and can rapidly escalate into emergencies. There’s no continuous monitoring, no early warning, and no predictive understanding.

### C. Triage challenges — especially during high-pressure situations
This gap becomes most visible when patient load suddenly spikes. During COVID-19, for example, hospitals struggled to prioritize patients whose vitals were silently worsening. Nurses and doctors were overwhelmed, and without real-time alerts, many patients deteriorated before anyone could intervene.

A smart triage system could have made a huge difference:
* Flagging drops in SpO₂
* Alerting staff when a patient’s condition begins to decline
* Helping medical teams focus on the most critical cases first

### The Gap
ICU-grade monitoring systems can do all of this—but they are extremely expensive. Most emergencies happen outside the ICU: in general wards, emergency rooms, and homes.

### The Purpose
**VitalShield aims to close this gap by bringing continuous, affordable, and intelligent monitoring to the places that need it most.**

---

## ⭐ 2. What VitalShield Will Do (Overview of the Solution)
VitalShield is a real-time patient monitoring and anomaly detection system powered by IoT sensors and GridDB Cloud. It’s designed to bring ICU-grade intelligence to homes, general wards, and emergency rooms—without ICU-level costs.



### 1. Track Key Vitals Continuously
VitalShield captures real-time values updating every few seconds:
* Heart Rate
* SpO₂
* Body Temperature

### 2. Display Live Data for Multiple Patients
A multi-patient command-center dashboard shows:
* Status of all patients at once
* Live vitals
* Risk level color coding

### 3. Real-Time Early Warning Score (EWS)
The system calculates an EWS based on medical scoring standards:
* Slight SpO₂ drop → **mild warning**
* HR spike + fever → **high alert**
* Rapid decline → **critical**

### 4. Predict Abnormalities Using Time-Series Analytics
With help from GridDB Cloud, the system can detect:
* Downward oxygen trends
* Increasing heart rate variability
* Early signals leading to deterioration

### 5. Post-Event Replay (Timeline Review)
For audits or diagnosis, VitalShield allows replaying:
* Vital drift
* Alert triggers
* Speed of deterioration

### 6. Fall Detection (Sensor or Simulation)
Uses accelerometer/vibration sensors or simulated fall events.

### 7. Smart Alerting System
VitalShield triggers alerts when:
* Thresholds break
* EWS rises
* Sudden deterioration occurs
* Fall is detected

### 8. Scalable Simulation Using GridDB Cloud
Even with one actual IoT node, I will simulate 10–50 patient rooms to demonstrate scalability and stress-test ingestion.

---

## ⭐ 3. How I Plan to Implement It (Step-by-Step Plan)

* **Phase 1 — Core IoT + Data Pipeline:** Set up ESP32 with MAX30102 & temp sensor, stream data to GridDB Cloud.
* **Phase 2 — Multi-Patient Simulation:** Generate 10–50 virtual patients with realistic vitals.
* **Phase 3 — Anomaly Detection Engine:** Implement threshold rules, rolling-window analytics, and alert triggers.
* **Phase 4 — Early Warning Score System:** Develop severity scoring and real-time score updates.
* **Phase 5 — Dashboard:** Build React multi-patient UI with live trends and detailed views.
* **Phase 6 — Post-Event Review:** Query historical data to replay past events.
* **Phase 7 — Fall Detection:** Implement sensor-based or simulated fall detection logic.
* **Phase 8 — Final Polish:** Improve UI, add notifications, and prepare demo workflow.

---

## ⭐ 4. Tools, Frameworks & Technologies

### IoT Hardware
* ESP32
* MAX30102 (HR + SpO₂)
* Temperature sensor (DS18B20 / MLX90614)
* Optional: vibration sensor (IMU)

### Backend
* Python (FastAPI) or Node.js (Express)
* Data ingestion & anomaly processing services

### Database
* **GridDB Cloud**
    * Time-series containers
    * High-speed writes
    * Real-time analytics

### Frontend
* React / Next.js
* Recharts / Chart.js
* WebSockets

---

## ⭐ 5. How VitalShield Will Use GridDB Cloud
GridDB Cloud will serve as the backbone. It will manage:
* **High-speed ingestion** of vitals from many rooms.
* **Time-series queries** (1 min, 10 min, 24 hr).
* **Rolling-window analysis** for anomalies.
* **Patient history & timeline data.**
* **Scalability** from 1 → 100 patient streams.

Without GridDB, the system would struggle with performance, retention, and real-time analytics.

---

## ⭐ 6. Expected Challenges
1.  **Sensor Noise:** Requires filtering & smoothing.
2.  **Multiple Real-Time Streams:** Async ingestion complexity.
3.  **Alert Fatigue:** Requires careful threshold tuning.
4.  **UI Performance:** Rendering many tiles live.
5.  **Predictive Analytics Modeling:** Correct rolling window design.
6.  **IoT Stability:** Reconnection handling.

---

## ⭐ 7. Future Expansion
* Sepsis early prediction.
* Medication reminders.
* Nurse workflow assignment.
* Sleep monitoring.
* Room CO₂ & humidity tracking.
* Hospital-wide heatmaps.
* Wearable version for home monitoring.

---

## ⭐ 8. One-Line Summary
**VitalShield brings affordable, real-time vitals monitoring and anomaly detection to every patient room using IoT sensors and GridDB Cloud — helping hospitals respond faster and prevent emergencies.**

## Built With
`ds18b20`, `esp32`, `griddb`, `imu`, `max30102`, `mqtt`, `python`, `websockets`

## Submitted to
**[GridDB Cloud IoT Hackathon](https://griddb-iot-hackathon.devpost.com/)**

## Created by
[parth mehta](https://devpost.com/parthpmehta23)