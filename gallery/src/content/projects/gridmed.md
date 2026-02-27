---
title: "GridMed: Cloud-Based Medicine Safety Monitoring System"
teamName: "Vipulesh Joi, Prabu, Varshini K, varsha vijayaragavan, Vikram .s"
thumbnail: "/images/generic.png"
type: "IoT"
description: "A GridDB-powered IoT platform that verifies medicine authenticity via QR scans and monitors storage conditions in real time to prevent spoiled medicines. "
isFinalist: false 
---

# GridMed: Cloud-Based Medicine Safety Monitoring System

## Inspiration
Many patients do not know whether the medicines they buy are genuine or stored safely. This can cause serious health risks. We wanted to create a simple system that helps people verify medicine safety instantly.

## What it does
GridMed scans QR codes to check authenticity and uses IoT sensors to monitor temperature in real time. All data is stored in **GridDB Cloud** and shown to users through a simple interface.

## How we built it
* **Arduino/ESP32** collects temperature data and sends it through a backend service.
* The **backend** stores readings and scan results in GridDB Cloud.
* A **web dashboard** displays live values, history, and alerts.

## Challenges we ran into
* Handling real-time IoT data transfer to the cloud was challenging.
* We also needed to structure time-series data properly in GridDB.
* Designing a simple interface for non-technical users was another challenge.

## Accomplishments that we're proud of
* We successfully combined IoT, QR verification, and GridDB Cloud in one system.
* We built a working concept that can scale to pharmacies and supply chains.
* Most importantly, our solution improves medicine safety and trust.

## What we learned
* We learned how IoT devices send live data to cloud databases like GridDB.
* We understood how to manage time-series data efficiently.
* We also experienced how hardware and cloud systems work together in real solutions.

## What's next for GridMed
We plan to add AI-based predictions for spoilage, more sensors for better tracking, and blockchain for secure records. We also aim to develop a mobile app for easier access anytime.

## Built With
`api`, `arduino`, `griddb`, `javascript`, `python`

### Submitted to
**[GridDB Cloud IoT Hackathon](https://griddb-iot-hackathon.devpost.com/)**