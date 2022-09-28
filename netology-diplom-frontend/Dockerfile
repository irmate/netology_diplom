FROM node:14.20.0-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
RUN npm build
COPY . .
EXPOSE 8080
CMD ["npm", "run", "serve"]