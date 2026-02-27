---
title: "MediGuard AI - Real-time Patient Vital Monitoring System"
teamName: "Mohammed Sahim,MAZIN MH"
thumbnail: "/images/generic.png"
type: "IoT"
description: "A real-time patient vital monitoring system that leverages GridDB Cloud's time-series capabilities and AI-powered anomaly detection to predict patient deterioration."
isFinalist: false 
---

# MediGuard AI - Real-time Patient Vital Monitoring System

## Inspiration
Healthcare facilities face a critical challenge where 65% of preventable deaths occur due to delayed response to patient vital sign deterioration. Manual monitoring systems miss early warning signs, and traditional databases cannot handle high-frequency medical IoT data effectively.

## What it does
MediGuard AI monitors patient vital signs in real-time and uses machine learning to detect anomalies and predict patient deterioration 5-10 minutes before critical events. It provides healthcare staff with a dashboard showing all patients, active alerts, and vital sign trends.

## How we built it
We designed a three-tier architecture:
1.  **IoT data ingestion layer**
2.  **FastAPI backend** with GridDB Cloud integration for time-series data storage
3.  **React frontend** for visualization

The ML component uses the Isolation Forest algorithm for anomaly detection combined with statistical rule-based checks.

## Challenges we ran into
* Designing an efficient data model for high-frequency time-series vital signs data.
* Implementing real-time anomaly detection with low latency.
* Creating a responsive dashboard that updates every 2 seconds without performance degradation.

## Accomplishments that we're proud of
* Successfully integrated **GridDB Cloud's** time-series optimization to handle 300+ writes per second.
* Achieved sub-second query response times for patient history retrieval.
* Implemented a multi-layered anomaly detection system that combines statistical and ML approaches.

## What we learned
* Understanding GridDB's time-series container optimization.
* Implementing efficient real-time data streaming with WebSocket.
* Balancing between immediate rule-based alerts and predictive ML-based warnings for medical applications.

## What's next for MediGuard AI
* Integration with real medical devices via **HL7/FHIR standards**.
* Implementation of LSTM networks for advanced time-series prediction.
* Development of mobile applications for healthcare staff.
* Achieving HIPAA compliance for production deployment.

## Built With
`fastapi`, `griddb-cloud`, `numpy`, `pandas`, `python`, `react`, `scikit-learn`, `tailwindcss`, `typescript`, `websocket`

## Submitted to
[GridDB Cloud IoT Hackathon](https://griddb-iot-hackathon.devpost.com/)

## Created by
* [Mohammed Sahim](https://devpost.com/MohammedSahim)
* [MAZIN MH](https://devpost.com/mhmazin87)