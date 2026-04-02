As a continuation of our previous blog: [GridDB IoT Hackathon Recap (Part 1 of 2): The Online Idea Phase](https://griddb.net/en/blog/iot-hackathon-part-1/), we will now recount the 2nd part of the GridDB IoT Hackathon. As noted, the first part of this grand event was an online portion in which the competition was open to anybody who was willing to travel to Bengaluru in the case that they won a position as one of five finalists. You can see the gallery of all submitted participants here: [GALLERY DIRECT LINK](https://gallery.griddb.net/).

From within the gallery you can already see which teams made it to the 2nd, in-person round. The official winners of the hackathon, as determined by the panel of judges were as follows: 

1. First Place: Deevia Software (Bengaluru) – Built a GenAI-based Enterprise Document Management Platform.

2. Second Place: Wimera (Bengaluru) – Created an IoT Proof-of-Concept (PoC) for Industrial Machines.

3. Third Place: VitalWatch (Maharashtra) – Developed a Preventive Risk Disease PoC.

4. Fourth Place: Richie Rich (Bengaluru) – Designed a Financial Analytics PoC.

5. Fifth Place: GooRoo Mobility India (Gujarat) – Built a low-cost remote Healthcare Solution PoC.

During the finals, teams received direct mentorship and technical support from Toshiba’s GridDB engineers. A member of the winning team, Deevia Software, noted that the GridDB Cloud platform made it extremely easy to efficiently ingest and query time-series data under a tight deadline, allowing them to focus on designing their solution rather than worrying about infrastructure.

For the remainder of the article, we will go over in small detail each project; for more details on the event itself, you can read the official press release here: [https://toshiba-india.com/pr-toshiba-announces-winners-of-gridDB-cloud-IoT-hackathon-highlighting-industry-ready-real-time%20-solutions-from-across-india.aspx](https://toshiba-india.com/pr-toshiba-announces-winners-of-gridDB-cloud-IoT-hackathon-highlighting-industry-ready-real-time%20-solutions-from-across-india.aspx). 

## The Projects

Part of what made the hackathon so special was the breadth of the topics in the ideas being submitted. For instance, of the five finalists, 1 was based on generative AI, 2 were based on health care, 1 was based on industrial IoT factory work, and the another was based on the financial sector.

I would like to briefly describe each project, and of course, if more information is desired, we encourage all readers to look at the hackathon gallery as it contains all projects' original submissions.

### Deevia

This project was unique in that it used GridDB in a way not necessarily envisioned by the GridDB team. Rather than focusing on IoT sensor data, Deevia built an AI-powered document management platform using Python, FastAPI, and React, with GridDB as the backbone via JPype. Their core insight was clever: instead of a traditional relational database, they used a container-per-file architecture where each uploaded document gets its own GridDB container, enabling parallel reads and writes without contention. OCR via PaddleOCR extracts text from scanned files, and Llama 3.1 powers semantic search and chat over the resulting knowledge base. They even used GridDB's built-in partition expiry to handle chat history cleanup — eliminating the need for Redis or cron jobs entirely. Overall, I recommend going and reading their presentation as it was fascinating work.

At a high level, the "GenAI-based Enterprise Document Managament Platform" means that they can feed documents into their system, use an OCR to convert all of the text into raw text, save those results, and then use GridDB's raw query speed to very quickly read the text data whenever a user queries the LLM which may need some data from the documents in question. Deevia also used the key-container data architecture to successfully silo off documents from users on a per-need basis (ie, if user A should not have access to certain class of documents, they simply won't have permissions to read from that container).

Overall, I recommend going and reading their presentation as it was fascinating work.

### Wimera

Wimera, while also a strong contender, was on the opposite end of the spectrum — their usecase is **exactly** the kind of project GridDB was designed for. Built using Python, Node.js, and Angular on top of Azure IoT Hub and Azure Event Hub, the system ingests machine telemetry every few seconds via MQTT/AMQP into GridDB Cloud's time-series containers. Azure Functions handle both ingestion and KPI aggregation, computing hourly and daily metrics automatically. The result is a fully connected pipeline from machine to cloud to dashboard that gives factory floors real-time visibility into machine status, energy consumption, and production counts — exactly the kind of industrial IoT use case where GridDB shines.

### VitalWatch

VitalWatch, one of the two health submissions, paints an optimistic picture of a future where rural communities can better track and manage the growing risk of diabetes and hypertension. The stack is impressively thorough: wearable sensors transmit readings via Bluetooth to a local gateway, which publishes to an MQTT broker. A Node.js service ingests the data into GridDB Cloud in real time, while a Python service using Pandas, Scikit-learn, and TensorFlow LSTM models runs rolling averages and predictive spike detection. Doctors get Grafana dashboards for trend visualization, and high-risk events trigger SMS alerts via Twilio. Although the presentation focused on the national crisis in India, the project's impact could truly be worldwide since diabetes is on the rise everywhere.

### Richie Rich

Though perhaps not something immediately obvious when considering GridDB's typical usage, financial tick data is actually a natural fit for a time-series database. The Richie Rich team built a unified portfolio tracker using FastAPI and Python on the backend with React 18, TypeScript, and Tailwind CSS on the frontend. Price data for stocks, crypto, and commodities is fetched from the CoinGecko API and persisted to GridDB Cloud's TIME SERIES containers every 20 seconds. An XGBoost classifier trained on historical GridDB data then generates buy/sell/hold recommendations, and GitHub Actions automates weekly CSV portfolio exports for compliance and backtesting. Nifty!

### GooRoo Mobility India (Gujarat)

This project was the other health entry and was also a very strong submission. The team built a real hardware IoT solution using an ESP32 microcontroller paired with a MAX30102 sensor for heart rate and SpO2 and an LM35 for body temperature. The ESP32 transmits readings as JSON over WiFi to a lightweight Python Flask REST API, which validates and stores the data in GridDB Cloud's time-series containers. A frontend dashboard built in HTML/CSS/JS with Chart.js displays live vitals with color-coded alerts and historical trends. The passion from the team was palpable — they were designing an affordable remote monitoring solution to reduce costly and timely doctor visits in underserved areas, and we are looking forward to what can come of it.

## Conclusion

Once again, we were blown away by the quality and breadth of submissions. What stood out across all five projects was how naturally GridDB's time-series model fit into domains well beyond traditional IoT — from AI document intelligence to financial analytics to real-time patient monitoring. We highly encourage all readers to explore the full hackathon gallery here: [Hackathon Gallery](https://gallery.griddb.net/).