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

## 1. Inspiration
* The contemporary healthcare systems find it hard to monitor patients in real-time particularly in distant or resource constrained settings. [cite: 155]
* We have tried to develop a low-priced, dependable and scalable IoT healthcare system that would be able to monitor vitals such as heart rate, SpO2 and body temperature and store the information in a high-performance cloud database. [cite: 156]

## 2. What It Does
* Collects Vitals: Heart Rate using MAX30102, SpO2 (blood oxygen saturation), and Temperature using LM35. All powered by ESP32. [cite: 159, 160, 161, 162, 163]
* Sends Data Wirelessly: ESP32 transmits WiFi data in a cycle in the form of JSON. [cite: 164, 165]
* Stores in GridDB: The server is a lightweight Python server that receives the data and stores it in a time-series optimized grid database, GridDB. [cite: 166, 167]
* Provides Live Monitoring: Real-time vitals dashboard, latest reading display, notifications about unnatural values, and historical trend analysis. [cite: 168, 169, 170, 171, 172]
* Everyone can access it on any network device. [cite: 173]

## 3. How We Built It
* **Hardware Layer:** ESP32 reads data from MAX30102 and LM35. [cite: 183, 184] Noise declares, samples averages to accuracy. [cite: 185] Calculates: Heart rate, SpO2 ratio, Temperature (ADC to Celsius) and packages data into JSON. [cite: 186]
* **Communication Layer:** ESP32 - WiFi - Python Flask API. [cite: 187, 188] Measurement is done in every cycle by HTTP POST. [cite: 189] Connection retry - error handling. [cite: 190]
* **Backend Layer:** Sensor data is sent to Python Flask REST API which validates inputs. [cite: 191, 192] Stores are loaded into a time-series collection of a gridDB. [cite: 193] Carries out aggregation and querying. [cite: 194] Provides endpoints for: data, latest stats, count, delete. [cite: 195]
* **Frontend Layer:** Datamonitor indicated through HTML dashboard (or Streamlit): Live heart rate, Live SpO2, Live temperature, Colors of alert (Red/yellow/green), and Statistics and trends. [cite: 196, 197, 198, 199, 200, 201, 202]

## 4. Challenges We Ran Into
* Sensor Accuracy Issues - MAX30102 required precise finger placement; solved with multi-sample averaging and validation logic. [cite: 210, 211]
* GridDB Cloud Integration - Authentication and REST API learning curve for time-series data insertion. [cite: 212, 213]
* Real-time Synchronization - Managing timing between hardware, server, and dashboard to prevent data loss. [cite: 214, 215]
* Device Status Tracking - Implementing timeout detection to distinguish offline devices from network delays. [cite: 216, 217]
* WiFi Connectivity Problems - ESP32 connection drops; resolved with automatic reconnection logic. [cite: 218, 219]
* Building Production-Ready Systems - Each challenge improved system robustness and reliability. [cite: 220, 221]

## 5. Accomplishments We're Proud Of
* Real-time Monitoring - Under half a second sensor-to-dashboard response with live WebSocket information. [cite: 223]
* GridDB Cloud Integration - Managed to conquer time-series database to stream data in IoT continuously. [cite: 224]
* Intelligent Check in Device Tracking - Integrated automatic offline detection system where it makes sure that there is some degree of reliability in the monitoring. [cite: 225]
* Simple Dashboard User Interface - Developed an easy-to-use computer to non-technical user friendly dashboard that has color-coded alerts. [cite: 226, 234]
* Complete IoT Solution - Provided end to end hardware to frontend and cloud-based systems within limited time. [cite: 235]
* Affordable Healthcare Access - Remote monitoring is proved to be affordable. [cite: 236]
* Real-World Impact - Developed a solution that has the potential to literally save the lives in rural and underserved locations. [cite: 237, 238]

## 6. What We Learned
* IOT hardware integration (ESP32, MAX30102, LM35). [cite: 240]
* Time-series database implementation - gridDB Cloud. [cite: 240]
* WebSocket real-time communication. [cite: 241]
* Standards of healthcare data accuracy and reliability. [cite: 242]
* Embedded system to web dashboard development (full-stack). [cite: 243]
* Project management and team work. [cite: 243]

## 7. What's Next
* Addition of more vital signs such as ECG, blood pressure. [cite: 245]
* Creating Al-driven health predictions on the basis of historical data in GridDB, creating and initiating pilot projects with rural clinics. [cite: 246]
* We aim to be a certified medical device that is part of healthcare systems and quality monitoring that is monitored by millions. [cite: 247]

## 8. Built With
* Software & Tools [cite: 249]
* Hardware: ESP32 + MAX30102 + LM35 [cite: 250]
* Backend: Python Flask + GridDB Cloud [cite: 251]
* Frontend: HTML/CSS/JS + Chart.js [cite: 252]
* Protocols: WiFi, HTTP, WebSocket [cite: 253]