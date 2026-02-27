---
title: "Presense"
teamName: "auvetralabs"
thumbnail: "/images/presense.png"
type: "IoT"
description: "Presense² is an AI-driven presence intelligence and energy management system designed for commercial buildings"
isFinalist: false 
---


# Presense²

## Inspiration
The inspiration for Presense² came from real-world exposure to commercial building automation during my second year of undergraduate studies in B.Sc. Digital & Cyber Forensic Science. While working on automation deployments across 40+ commercial sites, a consistent problem emerged: buildings were automated, but they lacked true awareness of human presence.

Most clients were not just looking to turn devices ON or OFF. They wanted a system that could understand occupancy, quantify actual energy savings, and generate data that could support ESG reporting and green building certifications. Existing solutions—largely based on PIR sensors or schedules—failed to deliver this intelligence, especially in scenarios involving static occupants or privacy-sensitive spaces.

This gap between automation and intelligence became the foundation for Presense².

---

## What it does
Presense² is an AI-driven presence intelligence and energy management system designed for commercial buildings. It detects real human presence, automates lighting and HVAC systems accordingly, and generates actionable analytics for energy optimization and sustainability reporting.

**The system:**
* Accurately detects static and dynamic human presence.
* Operates at zone level, not just room level.
* Automates lights and HVAC based on real occupancy.
* Measures and reports energy savings.
* Generates insights aligned with ESG and green building frameworks.
* Works in privacy-restricted environments without cameras.

---

## How we built it
The initial version of Presense² was built using existing CCTV infrastructure, leveraging AI vision models to detect human presence and movement patterns. This approach validated the concept but introduced limitations around privacy, lighting conditions, and deployment flexibility.

To overcome these challenges, we evolved the system to incorporate **mmWave radar sensing** for presence detection, enabling:
* Camera-free deployments.
* Reliable static occupancy detection.
* Operation in low-light and zero-light conditions.

**The architecture now combines:**
* **mmWave radar** for privacy-first presence sensing.
* **Vision-based AI** where cameras are permitted and beneficial.
* **Edge computing** for real-time decision-making.
* **Cloud analytics** for historical insights and reporting.
* **Vetra LLM:** Our building-focused intelligence layer to analyze patterns, detect anomalies, and generate actionable insights across devices and zones.

> **Note:** We use **GridDB** to store all our sensor data and retrace them to the LLM, where the LLM finds anomalies then saves occupancy data, energy saved, energy wasted, override functions, etc.

---

## Challenges we ran into
1. **Static Occupancy Detection:** Differentiating between an unoccupied space and a stationary occupant required careful tuning of sensing logic.
2. **False Positives:** Balancing sensitivity without compromising reliability was critical.
3. **Privacy Constraints:** Many commercial environments restricted camera usage, pushing us toward radar-based solutions.
4. **Energy Attribution:** Accurately correlating occupancy with energy savings required tight integration between sensing, control, and metering.
5. **Production Readiness:** Transitioning from prototypes to deployment-ready systems introduced hardware, firmware, and integration challenges.

---

## Accomplishments that we're proud of
* Transitioned from a camera-only system to a hybrid radar + AI architecture.
* Built a working MVP validated in real commercial environments.
* Received **10 Lakhs grant** from Meity, SISF, and EDII as a student entrepreneur.
* Secured multiple pilot opportunities, with **10+ pilots planned by March**.
* Developed a scalable foundation for **Vetra LLM**, focused on building intelligence.
* Achieved measurable energy efficiency outcomes in early deployments.

---

## What we learned
Through building Presense², we learned that:
* Presence detection is fundamentally different from motion detection.
* Privacy-first design is essential for enterprise adoption.
* Energy efficiency must be measurable, explainable, and auditable.
* Edge intelligence is critical for reliability in building systems.
* Real-world deployments reveal challenges that lab testing cannot.

Most importantly, we learned how to translate operational pain points into a scalable, intelligent system that delivers both technical and business value.

---

## What's next for Presense²
Our immediate focus is to execute **10+ pilot deployments by March**, primarily in commercial IT parks and corporate office environments. In parallel, we are strengthening **Vetra LLM** as a dedicated intelligence layer for buildings—starting with energy optimization, anomaly detection, and occupancy analytics.

**As the platform matures, we plan to:**
* Expand Presense² into educational institutions.
* Scale deployments through system integrators and enterprise partnerships.
* Grow the team to support larger pilots, deployments, and R&D.
* Build Presense² and Vetra LLM into a unified building intelligence platform.

This next phase is about moving from validation to scale, while maintaining accuracy, privacy, and reliability at the core.

**We are a student startup.**

## Built With
`bacnet`, `c++`, `django`, `fastapi`, `flutter`, `llm`, `modbus`, `mqtt`, `nosql`, `python`, `restapi`, `yaml`

## Try it out
* [drive.google.com](https://drive.google.com/drive/folders/1ghZuYgEnW7oLI5CmB49jg4F8Strfw6KG?usp=drive_link)
* [www.auvetralabs.com](http://www.auvetralabs.com)