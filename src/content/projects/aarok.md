---
title: AAROK
teamName: "Vijay S, Vishnu Kumar AR, Mohamed Razik M, Ashok Kumar, Mohammad Jaim"
thumbnail: "/images/aarok-gallery.png"
type: "Health"
description: A real-time IoT platform that ingests wearable glucose and blood pressure streams into GridDB Cloud, enabling trend detection, predictive spike alerts, and doctor notifications via SMS designed for 1 lakh+ patients in underserved rural clinics."
isFinalist: True 
placement: 5
---

# AAROK

## Inspiration
[cite_start]The contemporary healthcare systems find it hard to monitor patients in real-time, particularly in distant or resource-constrained settings[cite: 259]. [cite_start]We have tried to develop a low-priced, dependable, and scalable IoT healthcare system that would be able to monitor vitals such as heart rate, SpO2, and body temperature and store the information in a high-performance cloud database[cite: 260].

## What it does
AAROK is a real-time patient health monitoring system that:
* [cite_start]**Collects Vitals:** Heart Rate (MAX30102), SpO2, and Temperature (LM35) powered by ESP32 [cite: 263-267].
* [cite_start]**Sends Data Wirelessly:** ESP32 transmits WiFi data in a cycle in the form of JSON[cite: 268, 269].
* [cite_start]**Stores in GridDB:** A lightweight Python server receives the data and stores it in a time-series optimized GridDB[cite: 271].
* [cite_start]**Provides Live Monitoring:** Real-time dashboard with latest readings, trend analysis, and alerts for unnatural values [cite: 272-276].

## How we built it

* [cite_start]**Hardware Layer:** ESP32 reads data from MAX30102 and LM35, calculates values, and packages data into JSON [cite: 287-290].
* [cite_start]**Communication Layer:** ESP32 uses HTTP POST over WiFi to send measurements to the backend [cite: 291-293].
* [cite_start]**Backend Layer:** A Python Flask REST API validates inputs, stores data into a **GridDB** time-series collection, and provides endpoints for data retrieval [cite: 295-299].
* [cite_start]**Frontend Layer:** An HTML dashboard (using Chart.js) visualizes live Heart Rate, SpO2, and Temperature with color-coded alerts [cite: 300-306, 356].

## Challenges we ran into
* [cite_start]**Sensor Accuracy Issues:** MAX30102 required precise finger placement; solved with multi-sample averaging[cite: 314, 315].
* [cite_start]**GridDB Cloud Integration:** Overcoming the learning curve for authentication and REST API time-series insertion[cite: 316, 317].
* [cite_start]**Real-time Synchronization:** Managing timing between hardware, server, and dashboard to prevent data loss[cite: 318, 319].
* [cite_start]**Device Status Tracking:** Implementing timeout detection to distinguish offline devices from network delays[cite: 320, 321].
* [cite_start]**WiFi Connectivity:** Resolved ESP32 connection drops with automatic reconnection logic[cite: 322, 323].

## Accomplishments that we're proud of
* [cite_start]**Real-time Monitoring:** Achieved under half-a-second sensor-to-dashboard response with live WebSocket information[cite: 327].
* [cite_start]**GridDB Cloud Integration:** Successfully mastered the time-series database to stream IoT data continuously[cite: 328].
* [cite_start]**Intelligent Tracking:** Integrated automatic offline detection for reliable monitoring[cite: 329].
* [cite_start]**User-Friendly Dashboard:** Developed a simple, color-coded interface accessible to non-technical users[cite: 330, 338].
* [cite_start]**Affordable Access:** Created a solution with the potential to save lives in rural and underserved locations[cite: 340, 341].

## What we learned
* [cite_start]IoT hardware integration (ESP32, MAX30102, LM35)[cite: 344].
* [cite_start]Time-series database implementation with GridDB Cloud[cite: 344].
* [cite_start]WebSocket real-time communication[cite: 345].
* [cite_start]Standards of healthcare data accuracy and reliability[cite: 346].
* [cite_start]Full-stack development from embedded systems to web dashboards[cite: 347].

## What's Next
* [cite_start]Addition of more vital signs such as **ECG and blood pressure**[cite: 349].
* [cite_start]Creating **AI-driven health predictions** based on historical data in GridDB[cite: 350].
* [cite_start]Initiating pilot projects with rural clinics and aiming for medical device certification[cite: 350, 351].

## Built With
`chart.js`, `css`, `esp32`, `flask`, `griddb`, `html`, `http`, `javascript`, `lm35`, `max30102`, `python`, `websocket`, `wifi`

## Organization
[cite_start]**Gooroo Mobility India (GMIndia)** [cite: 237]

## Created by
* [cite_start]**Vijay S** [cite: 243]
* [cite_start]**Vishnu Kumar AR** [cite: 246]