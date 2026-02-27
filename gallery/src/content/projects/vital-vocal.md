---
title: Vital Vocal
teamName: "Sachin Gupta"
thumbnail: "/images/vital-vocal-gallery.jpg"
type: "IoT"
description: "Vital-Vocal is an AI care companion powered by GridDB. We transform high-velocity biometric data into empathetic voice conversations, allowing seniors to monitor their health simply by asking. "
isFinalist: false 
---

# Vital Vocal

## Inspiration
As our population ages, millions of seniors struggle to manage their health simply because medical devices are too complex. A smartwatch graph showing "SpO2 variability" is meaningless to an 80-year-old. We wanted to build a bridge between complex IoT data and human empathy. **Vital-Vocal** was born from the idea that checking your health should be as simple and comforting as asking a friend, "How am I doing?"

## What it does
Vital-Vocal is a voice-first AI companion for elderly care. It ingests real-time biometric data (Heart Rate, SpO2, Activity) from wearable sensors into **GridDB**. Instead of displaying charts, it uses **Google Gemini** to analyze the health trends and **ElevenLabs** to speak the results in a comforting, natural voice.

A user can simply ask, *"Did I sleep well last week?"* and the system will query GridDB, aggregate the sleep data, and respond: *"Yes, you averaged 7 hours of sleep, which is an improvement over last month."*

## How we will build it
Our architecture is designed for speed, reliability, and empathy:

1.  **IoT Ingestion Layer:** We will use a simulated medical node (Python-based) to stream high-frequency sensor data (1 reading/sec) directly to the **GridDB** database to demonstrate real-time capabilities.
2.  **Database Layer (The Core):** **GridDB Cloud** will serve as the time-series engine. We chose GridDB over others because of its "Key-Container" model which allows us to isolate each patient's data in memory for millisecond-level retrieval.
3.  **Intelligence Layer:**
    * **Google Gemini 1.5:** Acts as the medical reasoning engine. It takes the raw JSON data from GridDB and generates a medically accurate, natural language summary.
    * **ElevenLabs:** Converts that summary into a high-fidelity, empathetic voice response, reducing the "robot fatigue" often felt by elderly users.



## Detailed GridDB Usage (Technical Plan)
We have designed our schema specifically to leverage GridDB’s advanced time-series features:

1.  **Handling Data Gaps (Interpolation):** Wearable devices often disconnect, creating data gaps. We will use GridDB’s native `GROUP BY RANGE` with `FILL(LINEAR)` function to mathematically reconstruct missing heart-rate data in real-time, ensuring our AI never hallucinates due to missing inputs.
2.  **High-Speed Ingestion:** We will utilize GridDB’s Hybrid In-Memory architecture to buffer incoming high-velocity streams in RAM before flushing to disk.
3.  **Privacy & Lifecycle (Term Release):** To ensure compliance and manage storage costs, we will implement GridDB’s Term Release feature to automatically expire raw second-by-second data after 7 days, retaining only the daily aggregates for long-term trend analysis.

## Challenges we anticipate
1.  **Data Intermittency:** Real-world sensors are bursty. We plan to solve this using GridDB’s time-series interpolation rather than writing complex application logic.
2.  **Latency:** Voice interfaces feel broken if they lag. By querying GridDB's in-memory containers, we aim to keep the "Data-to-Voice" pipeline under 1 second.

## What's next for Vital-Vocal
If selected as finalists, we will deploy our local prototype to GridDB Cloud on Azure and integrate a live hardware demo using an ESP32 pulse sensor to demonstrate the end-to-end real-time pipeline in Bengaluru.

## Built With
`azure`, `docker`, `elevenlabs`, `google-gemini`, `griddb`, `iot`

## Submitted to
[GridDB Cloud IoT Hackathon](https://griddb-iot-hackathon.devpost.com/)

## Created by
[Sachin Gupta](https://devpost.com/sachingen1981)