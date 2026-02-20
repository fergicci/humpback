# Use Node.js base image
FROM node:slim

# Set working directory inside the container
WORKDIR /app

# Copy only what's needed
COPY dist/ ./dist/
COPY .env.local .env.production.local

# Copy package files and install only production dependencies
COPY package.json package-lock.json ./

# Install only production dependencies without running audit/funding checks
RUN npm ci --omit=dev --no-audit --no-fund

# You can install one globally
RUN npm install -g serve

# Serve the built files
CMD ["serve", "-s", "dist"]
