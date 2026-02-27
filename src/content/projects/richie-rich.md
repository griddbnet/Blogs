---
title: Richie Rich
teamName: "Tejas Hari, Saatvik Sinha, Rahul Raman"
thumbnail: "/images/richierich1.jpeg"
type: "Finance"
description: Intelligent Multi-Asset Portfolio Tracker
isFinalist: true
placement: 4 
---
# Richie Rich  
### Intelligent Multi-Asset Portfolio Tracker

[Download the Richie Rich Presentation](/Hedge-Fund-Manager-Presentation.pptx)

---

## Elevator Pitch
**Richie Rich** is a real-time portfolio management dashboard that tracks **stocks, crypto, and commodities** across global markets in a single unified interface. Built on **GridDB’s time-series capabilities**, it stores granular price data, generates machine-learning-driven insights, and automates weekly performance reports using **GitHub Actions**.

---

## Links
- **Live Demo:** Richie Rich (Vercel App)  
- **GitHub Repository:** https://github.com/tejas6996/grib  
- **Video Demo:** https://drive.google.com/file/d/19kaGcvQ35XcBRZejvS1VzzyeDHv9hSgG/view  

---

## About the Project

### Motivation
Modern hedge funds manage portfolios spanning:
- Global equities
- Cryptocurrencies
- Commodities
- Derivatives

However, today’s tooling is fragmented:
- Bloomberg terminals for US equities  
- Specialized NSE tools for Indian markets  
- Separate crypto exchange dashboards  
- Independent commodity tracking platforms  

This fragmentation introduces **information latency** and increases **operational overhead**.

**Richie Rich** was built as a unified platform that:
- Aggregates real-time multi-asset data
- Stores granular historical price movements
- Powers predictive models to identify alpha-generating opportunities  

Traditional relational databases struggle with high-frequency financial tick data. **GridDB’s time-series architecture** provides optimized write performance and queryable history, enabling price updates every 15–20 seconds without bottlenecks.

---

## Technical Architecture

### System Components

#### 1. Backend Layer
- **FastAPI** server orchestrating data ingestion
- Sources:
  - CoinGecko API (cryptocurrency prices)
  - Simulated models (equities)
- Async price fetching with exponential backoff
- Background worker persists snapshots to GridDB every **20 seconds** when frontend activity is detected

#### 2. Storage Layer
- **GridDB Cloud – TIME SERIES container**
- Optimized for chronological writes

**Schema:**

{ timestamp, symbol, price, change24h }


- Row key collision handled via **millisecond-offset timestamps** for concurrent multi-symbol inserts

#### 3. Presentation Layer
- **React 18 + TypeScript**
- Vite build tooling
- Tailwind CSS styling

**Portfolio Valuation Formula:**

V_portfolio = Σ (Qi × Pi)


Where:
- `Qi` = quantity held
- `Pi` = current market price

**Weighted 24h Performance:**

Δ24h = (Σ (Qi × Pi × change_i,24h)) / V_portfolio


---

## Machine Learning Integration
- **XGBoost classifier** for buy / sell / hold recommendations
- Feature engineering from GridDB historical data:

Features = { price, Δ24h, highest, lowest, range, range% }


- Model trained on `stockPrice.csv`
- Recommendations exposed via `/recommendations` API endpoint

**Planned Enhancements:**
- Time-series forecasting models
- Moving averages and correlation matrices using GridDB aggregations

---

## Automation Infrastructure
A **GitHub Actions** workflow automates weekly portfolio exports:

- Triggered via:
  - Cron (Sunday, 00:00 UTC)
  - Manual dispatch
- Fetches portfolio state from backend
- Exports timestamped CSV to `/data`
- Commits and pushes via bot credentials
- Artifacts retained for **90 days**

This provides auditable performance history for compliance and backtesting.

---

## Key Learnings

### Time-Series Databases Matter
- GridDB’s TIME SERIES containers align naturally with financial analysis
- Traditional RDBMS indexing fails at scale for time-ordered inserts

### Row Key Semantics
- TIME SERIES containers use timestamps as row keys
- Concurrent inserts caused overwrites
- Solved via millisecond offsets per symbol

### Real-Time API Limitations
- CoinGecko rate limits
- Yahoo Finance blocks automation
- Required caching, fallback logic, and exponential backoff

### Frontend Performance
- Polling every 15 seconds stresses React state
- Proper `useEffect` cleanup was critical to avoid memory leaks

### Realistic Price Modeling
- Initial static pricing produced no visible movement
- Correct approach:

price_fluctuated = price_base × (1 + Δpct / 100)


### GitHub Actions Permissions
- Default `GITHUB_TOKEN` is read-only
- Required:


permissions:
contents: write


---

## Development Timeline

**Week 1:**  
- FastAPI endpoints
- React frontend with mock data

**Week 2:**  
- CoinGecko integration
- Real-time valuation logic
- Async background tasks

**Week 3:**  
- GridDB Cloud deployment
- TIME SERIES container setup
- Row key collision fixes

**Week 4:**  
- XGBoost ML model
- Recommendation API
- Glassmorphic dark UI

**Week 5:**  
- Price fluctuation bug fixes
- Render + Vercel deployment
- CORS configuration

**Week 6:**  
- GitHub Actions automation
- Weekly CSV exports
- Cron scheduling

---

## Current System Features
- Real-time tracking for **10 assets**
- Automatic GridDB persistence every 20 seconds
- Portfolio valuation with weighted 24h performance
- ML-powered buy/sell/hold recommendations
- Weekly automated CSV exports
- Responsive glassmorphic UI
- REST API with CORS support
- Environment-based configuration

---

## Technical Challenges & Solutions

### GridDB Authentication
- Requires Base64-encoded Basic Auth header
- Exact formatting needed to avoid 401 errors

### TIME SERIES Row Key Collisions
- Solved using millisecond offsets per symbol

### API Rate Limiting
- CoinGecko free tier: 50 calls/min
- Implemented exponential backoff + caching

### React Interval Bugs
- Improper cleanup caused overlapping fetches
- Fixed with strict dependency management

### GitHub Actions Issues
- Workflow location requirements
- Token permissions
- Deprecated actions upgraded

### Deployment Connectivity
- Render free tier uses dynamic IPs
- GridDB Cloud free tier requires static IPs
- Documented as a known limitation

**Production Solutions:**
- Paid GridDB tier
- Static IP proxy
- Same-VPC deployment

---

## Technology Stack

### Backend
- Python 3.11
- FastAPI
- Uvicorn

### Frontend
- React 18
- TypeScript
- Vite
- Tailwind CSS

### Database
- **GridDB Cloud (TIME SERIES)**

### Machine Learning
- XGBoost
- pandas
- numpy

### APIs
- CoinGecko
- GridDB WebAPI
- Simulated equity models

### DevOps
- GitHub Actions
- Render (backend)
- Vercel (frontend)

---

## Summary
**Richie Rich** demonstrates how a dedicated time-series database like **GridDB** enables high-frequency financial data ingestion, real-time analytics, and machine-learning-driven insights. By unifying multi-asset tracking into a single platform and automating reporting pipelines, the project showcases a scalable, production-aware approach to modern portfolio management.
