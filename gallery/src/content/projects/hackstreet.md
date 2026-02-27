---
title: MetroYukti (HackStreet)
teamName: Aayush Kolte · Akshat Patil · Dhiren Sacher 
thumbnail: "/images/metro-yukthi.png"
type: "IoT"
description: MetroYukti is an AI-powered decision system that generates **safe, optimal, and feasible train induction schedules.
isFinalist: false 
---
# MetroYukti (HackStreet)

---

## Problem Statement
Rapidly expanding urban rail networks face a critical operational bottleneck: **fleet scheduling**. Balancing maintenance, safety, and commercial requirements is still largely handled using disjointed, manual systems.

This lack of digital integration is not just inefficient — it introduces **significant financial and safety risks** as networks scale.

---

## The Scalability Crisis

### Reactive Maintenance
- Unplanned repairs cost **3×–5× more** than preventive maintenance  
- Manual planning forces a reactive posture  

### Critical Dependency on Spreadsheets
- **94% of spreadsheets contain errors**  
- Manual data entry is the single largest point of failure in safety compliance  

### Growth Ceiling
- Fleet size grows linearly  
- Operational complexity grows **exponentially**  
- Manual teams cannot physically scale with network growth  

**Result:**  
- 15–20% annual inflation in operating budgets  
- Capped network capacity despite infrastructure expansion  

---

## The Global Urban Rail Challenge
Fleet scheduling must simultaneously consider:

- Stabling geometry  
- Train fitness certificates  
- Job cards & maintenance status  
- Branding & mileage balancing  
- Cleaning schedules  
- Operational service plans  

Today, these exist as **organized data silos**, leading to:
- Ad-hoc reconciliation  
- Suboptimal train assignment  
- Increased safety and compliance risks  

---

## Solution Overview: MetroYukti

**MetroYukti** is an AI-powered decision system that generates **safe, optimal, and feasible train induction schedules**.

### Key Capabilities
- Outputs a **final, actionable schedule** with step-by-step reasoning  
- Converts raw operational data into real-time decisions  
- Designed for scalability as new metro lines are added  

---

## Core Features

### Smart Train Ranking
AI prioritizes the entire fleet using a **weighted score** across all key variables.

### Safety & Compliance Check
A deterministic rule engine verifies:
- Fitness certificates  
- Open job cards  
- Hard operational constraints  

Unsafe trains are eliminated **before** optimization.

### Schedule Optimization
A high-speed **heuristic solver** balances:
- Mileage  
- Branding commitments  
- Cleaning requirements  

### Digital Twin Simulation (Unique Feature)
- Simulates the proposed schedule using **SimPy**
- Validates physical feasibility (shunting, grounding)
- Calculates performance metrics before execution

### Real-Time Data Integration
Automatically syncs:
- Mileage  
- Maintenance status  
- Branding data  
- Operational constraints  

---

## Technical Approach (Data → Decision Framework)

1. **Input Validation**  
   Validates daily train data, depot capacity, and operational objectives.

2. **Knowledge Retrieval**  
   Retrieves SOPs, historical maintenance records, and past schedules from a vector database.

3. **Safety Analysis**  
   Rule engine filters out trains failing hard constraints (expired certificates, critical job cards).

4. **LLM Ranking**  
   Mistral-powered agent ranks safe trains using a Bayesian Neural Network (BNN).

5. **Heuristic Optimization**  
   Generates an initial near-optimal assignment for:
   - Service  
   - Standby  
   - Maintenance  

6. **LLM Validation**  
   Agent validates the generated plan against knowledge and constraints.

7. **Digital Twin Simulation**  
   SimPy simulates real-world shunting movements and calculates KPIs.

8. **Final Output**  
   Produces a structured JSON schedule with:
   - Performance metrics  
   - Validation results  
   - Reasoning logs  

---

## Technology Stack

### Core
- **GridDB**
- **Mistral AI**
- **React.js**

### Digital Twin & Optimization
- **SimPy**
- Heuristic Solvers

### Mobile Applications
- Flutter / Dart  
- Kotlin  
- Swift  

### Data & Knowledge Layer
- PostgreSQL (Single Source of Truth)  
- Vector embeddings (separate schemas)  

---

## Feasibility

- Integrates with existing systems (Maximo, IoT feeds, manual logs)  
- No disruption to current operations  
- Deterministic validation prevents parameter mismatches  
- Rule engine cross-references:
  - CBTC / ATO configurations  
  - Bay-specific protocol requirements  

All safety decisions are **auditable and immutable**.

---

## Viability

### Rapid ROI
- Slashes maintenance costs
- Reduces manual planning from hours to minutes

### Safety by Design
- Scheduling is strictly conditional on passing hardcoded safety checks  
- Creates a permanent compliance log for every run  

### Predictive Maintenance
- Links job cards to real-time mileage  
- Enables Just-In-Time (JIT) spare parts availability via API integrations  

### Single Source of Truth
- Centralized PostgreSQL database  
- Eliminates data staleness and inconsistencies across tools  

---

## Impact & Benefits

### Operational Impact
- Nightly induction time reduced from **3.5 hours to ~20 minutes**
- Faster, auditable scheduling with fewer human errors  

### Energy & Asset Efficiency
- 5–15% traction energy savings (benchmarked up to 25%)  
- Reduced deadhead shunting and asset wear  

### Maintenance
- 30–50% reduction in downtime  
- 15–25% lower maintenance costs  

### Revenue Protection
- Protects non-fare revenue (~20% at DMRC)  
- Honors branding commitments with reliable inventory  

### Passenger Experience
- Fewer last-minute cancellations  
- Improved punctuality during peak hours  
- Higher passenger trust  

---

## Broader Benefits

### Social Benefits
- Reliable, punctual services  
- Smoother, disruption-free journeys  

### Economic Benefits
- Reduced costs  
- Protected revenue streams  
- Improved financial sustainability  

### Environmental Benefits
- Energy-efficient train deployment  
- Smarter workload balancing  
- Lower overall system strain  

---

## Summary
MetroYukti transforms metro fleet scheduling from a **manual, error-prone process** into a **safe, AI-driven, and digitally validated system**. By combining GridDB-powered data integration, deterministic safety checks, AI optimization, and digital twin simulation, the solution delivers scalability, safety, and measurable operational impact for modern urban rail networks.

---

**Thank You**  
*HackStreet – MetroYukti*
