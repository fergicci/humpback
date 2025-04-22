# Use Node.js base image
FROM node:slim

# Set working directory inside the container
WORKDIR /app

# Copy only what's needed
COPY dist/ ./dist/
COPY .env.local .env.production.local

# Copy package files and install only production dependencies
COPY package.json package-lock.json ./

# Install only production dependencies
RUN npm ci --only=production

# You can install one globally
RUN npm install -g serve

# Serve the built files
CMD ["serve", "-s", "dist"]
