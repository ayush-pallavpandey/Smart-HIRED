FROM node:18
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
EXPOSE 3000
ENV HOST=0.0.0.0
ENV DANGEROUSLY_DISABLE_HOST_CHECK=true
CMD ["npm","start"]
