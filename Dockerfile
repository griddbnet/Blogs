FROM node:16

# Make c_client
WORKDIR /
RUN wget --no-check-certificate https://github.com/griddb/c_client/releases/download/v5.0.0/griddb-c-client_5.0.0_amd64.deb
RUN dpkg -i griddb-c-client_5.0.0_amd64.deb

WORKDIR /app

COPY app.js /app
RUN mkdir /app/frontend
COPY frontend /app/frontend
COPY package.json /app
COPY package-lock.json /app
RUN npm run build
RUN npm install


ENTRYPOINT ["npm", "run", "start", "griddb-server:10001", "defaultCluster", "admin", "admin"]

EXPOSE 2828
