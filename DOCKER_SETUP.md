# Docker Setup Guide

## Overview

XAI-Forge is fully containerized using Docker and Docker Compose, ensuring the application runs out-of-the-box without manual configuration.

## Quick Start

```bash
# Build and start all services
docker-compose up --build

# Run in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

## Services

### 1. PostgreSQL Database
- **Image**: `postgres:17-alpine`
- **Port**: `5432`
- **Database**: `xai_db`
- **User**: `xai_user`
- **Password**: `changeme` (change in production!)

### 2. Backend API
- **Port**: `8080`
- **Build**: Multi-stage Maven build
- **Runtime**: Java 17 JRE
- **Health Check**: Automatic connection to database

### 3. Frontend
- **Port**: `3000` (development) or `80` (production)
- **Build**: React with nginx
- **Proxy**: API requests forwarded to backend

## Environment Variables

Create a `.env` file in the root directory:

```env
JWT_SECRET=your-secure-secret-key-here
DB_PASSWORD=your-database-password
```

## Production Considerations

1. **Change default passwords** in `docker-compose.yml`
2. **Use secrets management** for sensitive data
3. **Configure proper JWT_SECRET** (use strong random string)
4. **Set up SSL/TLS** for production
5. **Configure resource limits** for containers

## Troubleshooting

### Port Already in Use
```bash
# Check what's using the port
lsof -i :8080
lsof -i :5432
lsof -i :3000

# Stop conflicting services or change ports in docker-compose.yml
```

### Database Connection Issues
```bash
# Check database health
docker-compose ps postgres

# View database logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

### Build Failures
```bash
# Clean build
docker-compose build --no-cache

# Rebuild specific service
docker-compose build backend
```

## Development Workflow

### Hot Reload (Development)
For development with hot reload, run services separately:

```bash
# Start only database
docker-compose up postgres

# Run backend locally
cd backend && mvn spring-boot:run

# Run frontend locally
cd frontend && npm start
```

## Verification

After starting, verify all services:

```bash
# Check all services are running
docker-compose ps

# Test backend health
curl http://localhost:8080/actuator/health

# Test frontend
curl http://localhost:3000
```

## Volume Management

Uploads are persisted in `./uploads` directory (mounted as volume).

Database data is persisted in Docker volume `postgres_data`.

To reset everything:
```bash
docker-compose down -v
rm -rf uploads/*
```

