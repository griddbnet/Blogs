As a continuation of our previous blog: [OLD BLOG](), we will now recount the 2nd part of the GridDB IoT Hackathon. As noted, the first part of this grand event was an online portion in which the competition was open to anybody who was willing to travel to Bengaluru in the case that they won a position as one of five finalists. You can see the gallery of all submitted participants here: [GALLERY DIRECT LINK]().

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

This project was unique in that it used GridDB in a way not ncessarily envisioned by the GridDB Team. If you have read other blogs on this site before, you are probably aware that GridDB markets itself as a database for timeseries data such as for IoT devices and the like. Deevia did something interesting in that they chose instead to focus on the high performance aspect of GridDB, meaning they eschewed the IoT-focus of traditional GridDB projects and focused on its key-container data model in a generative AI project. 

At a high level, the "GenAI-based Enterprise Document Managament Platform" means that they can feed documents into their system, use an OCR to convert all of the text into raw text, save those results, and then use GridDB's raw query speed to very quickly read the text data whenever a user queries the LLM which may need some data from the documents in question. Deevia also used the key-container data architecture to successfully silo off documents from users on a per-need basis (ie, if user A should not have access to certain class of documents, they simply won't have permissions to read from that container).

Overall, I recommend going and reading their presentation as it was fascinating work.

### Wimera

Wimera, while also a strong contender, was on the opposite end of the spectrum; their usecase and project are *exactly* the kind of usecase the GridDB team expected to compete and to have strong contenders. The focus on industrial IoT is the exact usecase for which GridDB was designed for, so it was no surprise to see such a project go far in this environment. Wimera offered a strong showing of how useful GridDB can be in an industrial IoT setting, reducing cost while maintaining exquisite performance a data model perfectly adapted for the rigors of an IoT environment.

### VitalWatch

VitalWatch, one of the two health submissions, paints an optimistic picture of a future where rural communities can better track and manage the growing risk of diabetes. Although the presentation focused on the national crisis in India, the project’s impact could truly be worldwide since diabetes is on the rise everywhere. All in all, a very strong project!

### Richie Rich

Though perhaps not something that is immediately when considering usage of GridDB, financial data is actually sneakily a great fit due to its very time-dependant datasets. Indeed, if you think about stock ticker data, it's purely time-series based data! So, the team at Richie Rich decided to scrape some finance APIs, save the time-based data into GridDB, query the data directly into dataframes, and then run some fancy ML algorithms to make predictions on what to invest in. Nifty!

### GooRoo Mobility India (Gujarat)

This project was the other health project and was also a very strong entry. The passion from the team was palpable and admirable -- they were designing a low cost health solution to help reduce costly and timely doctor visits. Health and the IoT sector have been growing so this project also showed great promise and we are looking forward to what can come of it.

## Conclusion

Once again, we were blown away by the quality and breadth of submissions and we highly encourage all users to look through the hackathon gallery here: [GALLERY LINK]().