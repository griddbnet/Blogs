FROM node:20

WORKDIR /usr/src/app

COPY package*.json ./
COPY . ./
RUN npm install
EXPOSE 5115
CMD [ "npm", "start" ]