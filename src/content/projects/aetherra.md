---
title: "Aetherra"
teamName: "Aetherra"
thumbnail: "/images/aetherra-gallery.jpg"
type: "IoT"
description: "a climate MRV (Measurement, Reporting, and Verification) system that enables continuous, audit-ready carbon verification using real-world sensor time-series data"
isFinalist: false 
---


# Aetherra

**Aetherra is a climate MRV (Measurement, Reporting, and Verification) system that enables continuous, audit-ready carbon verification using real-world sensor time-series data.**

Current carbon MRV processes rely heavily on manual surveys, assumptions, and infrequent audits, making verification slow, non-transparent, and inaccessible to small farmers and grassroots projects.

Aetherra solves this by collecting tree-level environmental data (CO₂, proxies, soil moisture, temperature, humidity) through low-cost sensors, storing it in **GridDB** time-series containers, and visualizing it through live dashboards. This allows environmental impact to be measured continuously, reported digitally, and verified transparently.

The project demonstrates how GridDB is well-suited for climate-critical time-series workloads, where precision, reliability, and long-term data integrity are essential.

---

## Inspiration
Carbon credit verification is extremely broken. **Today:**
* Verification is slow.
* Data is manual.
* Reporting is non-standardized.
* Small farmers cannot access carbon revenue.

We asked: **“What if MRV could be continuous, automated, and backed by real-time environmental data?”**

Aetherra was born from that question — an IoT-powered MRV system that stores climate signals into GridDB, enabling transparent, timestamped evidence for carbon credits.

---

## What Aetherra Does
Aetherra is a real-time MRV data pipeline designed for carbon credit projects and agroforestry systems.

**Our system:**
1.  **Collects real sensor-like climate data:**
    * Temperature & Humidity
    * CO₂ ppm
    * Soil moisture & Pressure
    * Tree/plot identifiers
    * Accurate timestamps
2.  **Stores all records in GridDB Community Edition, using:**
    * Time-series containers
    * High-frequency inserts
    * SQL WebAPI
    * Fast queries for dashboards
3.  **Generates MRV-ready time-series logs:**
    * For issuing carbon credits and verifying field conditions.
    * For auditors, registries, and digital carbon passport creation.

Everything is done **continuously**, not manually — making MRV affordable and scalable.

---

## How We Built It

### 1. Sensor Simulator (IoT Node)
A Python script simulates a real MRV sensor device sending data packets. This imitates real-world agroforestry sensor hardware.
```json
{
  "tree_id": "T001",
  "temperature": 28.5,
  "humidity": 75.9,
  "pressure": 1012,
  "soil": 435,
  "co2": 412
}