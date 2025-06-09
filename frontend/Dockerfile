# 1) Install & build
FROM node:20-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci

FROM deps AS builder
COPY . .
RUN npm run build

# 2) Serve stage (SSR)
FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
# Copy only what’s needed to run
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next ./.next
COPY --from=deps    /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./package.json
EXPOSE 3000
CMD ["npm","start"]
