FROM node:18

# Make c_client
WORKDIR /
RUN wget --no-check-certificate https://github.com/griddb/c_client/releases/download/v5.6.0/griddb-c-client_5.6.0_amd64.deb
RUN dpkg -i griddb-c-client_5.6.0_amd64.deb


WORKDIR /app
COPY package.json /app/package.json
COPY package-lock.json /app/package-lock.json
COPY gen-data.js /app/gen-data.js

RUN npm i

ENTRYPOINT ["node", "gen-data.js"]


