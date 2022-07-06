FROM node:16

# Make c_client
WORKDIR /
RUN wget --no-check-certificate https://github.com/griddb/c_client/releases/download/v5.0.0/griddb-c-client_5.0.0_amd64.deb
RUN dpkg -i griddb-c-client_5.0.0_amd64.deb

WORKDIR /app

COPY app.js /app
COPY ingest.js /app
COPY cereal.csv /app
RUN mkdir /app/public
COPY public /app/public
COPY package.json /app
COPY package-lock.json /app
RUN npm install


ENTRYPOINT ["npm", "run", "start", "239.0.0.1",  "31999", "defaultCluster", "admin", "admin"]

#ENTRYPOINT ["node", "ingest.js", "239.0.0.1",  "31999", "defaultCluster", "admin", "admin"]
