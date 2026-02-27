---
title: Esmart
teamName: "* Rohan Kumar Shah. Nishant Chhetri, Manish Dey"
thumbnail: "/images/generic.png"
type: "Health"
description: Esmart is an AI-driven platform designed to solve the predictive intelligence crisis in modern energy grids.
isFinalist: false 
---
# Esmart: AI-Driven Prosumer Energy Platform

## Inspiration
India's grid is currently drowning in green energy it cannot manage. [cite_start]This isn't a shortage of power, but a crisis of **predictive intelligence** where the lack of real-time AI coordination forces massive energy wastage and financial instability [cite: 378-379].

[cite_start]We noticed that clean energy is frequently "curtailed"—essentially thrown away—because grid operators cannot predict surges in generation or shifts in demand in real-time[cite: 426]. [cite_start]Esmart bridges the gap between the "prosumer" and a smarter, more resilient grid[cite: 428].

## What it does
Esmart is an AI-driven platform designed to solve the predictive intelligence crisis in modern energy grids. [cite_start]Unlike current software designed for damage control, Esmart is built for **occurrence prevention**[cite: 381, 404].


### The 6-Step Core Engine:
1.  [cite_start]**Solar/Wind Forecasting:** Uses weather APIs and historical datasets to predict local generation[cite: 409].
2.  [cite_start]**Load Prediction:** Analyzes consumer demand patterns to understand exactly when energy is needed[cite: 410].
3.  [cite_start]**Congestion Check:** Monitors transmission line strength to identify potential grid bottlenecks[cite: 411].
4.  [cite_start]**Storage Health (SoH/SoC):** Real-time monitoring of battery capacity and health to determine usable backup[cite: 412, 416].
5.  [cite_start]**Curtailment Prediction:** Synthesizes the first four steps to identify when clean energy will be wasted[cite: 418].
6.  [cite_start]**Action Recommendation:** Suggests the best path—storing in batteries, shifting industrial loads, or trading with neighbors[cite: 419].

### Key Features
* [cite_start]**Peer-to-Peer (P2P) Trading:** Decentralized marketplace for neighbors to buy/sell excess solar energy, bypassing utility markups[cite: 421].
* [cite_start]**Grid Stability Monitoring:** Tracks voltage (e.g., 228V) and frequency (e.g., 50.2 Hz) to warn of impending outages[cite: 422].
* [cite_start]**Waste Reduction:** Targets "curtailment" to monetize surplus green energy[cite: 423].
* [cite_start]**Environmental Impact Analytics:** Translates data into metrics like CO₂ saved and trees planted[cite: 424].

## How we built it
We developed the platform using a modern web stack designed for real-time data handling:
* [cite_start]**Frontend (React):** Component-based architecture for handling live data streams without page reloads[cite: 430].
* [cite_start]**Styling:** CSS3 with Custom Properties (Variables) for an "electric" aesthetic[cite: 431].
* [cite_start]**State Management:** Tracks global states like battery capacity (SoC) and trading offers[cite: 433].
* [cite_start]**Predictive Logic:** A custom engine integrating weather forecasting, demand patterns, and congestion data[cite: 435].
* [cite_start]**Simulation:** Vanilla JavaScript used to simulate real-time energy flow and metric updates[cite: 436].

## Challenges we ran into
* [cite_start]**Data Synchronization:** Coordinating unpredictable weather data with fluctuating consumer demand patterns[cite: 438].
* [cite_start]**UI Complexity:** Designing a dashboard that makes complex technical data (Voltage, Frequency, SoH) intuitive for homeowners[cite: 439].
* [cite_start]**Real-time Alerts:** Engineering the system to trigger "Power Cut" predictions with high confidence based on instability markers[cite: 440].

## How we use GridDB
[cite_start]The Esmart platform uses **GridDB**, a high-performance time-series database optimized for IoT, to store real-time energy readings [cite: 441-442].

* [cite_start]**Data Storage:** Stores continuous streams of voltage, current, solar output, and battery levels collected every few seconds[cite: 443].
* [cite_start]**Data Management:** Manages user profiles, AI-generated power cut predictions, and P2P trading offers[cite: 447].
* [cite_start]**Integration:** The React frontend queries GridDB through **REST APIs** to fetch live grid status and energy flow charts[cite: 448].
* [cite_start]**Analytics:** Leverages GridDB's time-series capabilities for fast retrieval of historical data for trend analysis and AI model training[cite: 449].

## Links
* [cite_start]**Live Demo:** [code2rohanshah.github.io/esmartenergy](https://code2rohanshah.github.io/esmartenergy/) [cite: 450]
* [cite_start]**Video Demonstration:** [YouTube Video](https://www.youtube.com/watch?feature=shared&v=hvCkdSX2e80) [cite: 467]

## Built With
`css3`, `griddb`, `javascript`, `react`, `rest-api`

## Team Fakerz
* Rohan Kumar Shah
* Nishant Chhetri
* Manish Dey
[cite_start][cite: 453-455]