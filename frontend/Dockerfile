# =====================================================================
#  EShop frontend — build Vite puis service statique via Nginx
# =====================================================================

# ---- Étape 1 : build de l'application React ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# ---- Étape 2 : image Nginx servant le build + proxy /api,/auth ----
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
