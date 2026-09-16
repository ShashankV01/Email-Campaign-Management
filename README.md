# Email Campaign Management API

Java 17 + Spring Boot + MySQL REST API for the Email Campaign Developer Assignment.

## Features

- Create campaigns
- Add multiple recipients
- Validate emails
- Prevent duplicate recipients per campaign
- Schedule campaigns
- Simulate delivered/failed email outcomes
- Automatic background processing of due campaigns
- Campaign pagination
- Status filtering
- Campaign-name search
- Created-date sorting
- Campaign statistics
- Consistent JSON responses
- Global exception handling
- MySQL database persistence
- Pessimistic locking to prevent double processing
- Optimistic version field for consistency
- Docker Compose support

## Requirements

- Java 17+
- Maven 3.9+
- MySQL 8+

## Configuration

Create environment variables:

```bash
DB_URL=jdbc:mysql://localhost:3306/email_campaign_db?createDatabaseIfNotExist=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=your_password
SERVER_PORT=8080
```

## Run locally

Create the database:

```sql
CREATE DATABASE email_campaign_db;
```

Then:

```bash
mvn clean test
mvn spring-boot:run
```

API base URL:

```text
http://localhost:8080/api/campaigns
```

## API examples

### 1. Create campaign

```http
POST /api/campaigns
Content-Type: application/json
```

```json
{
  "name": "September Newsletter",
  "subject": "September Updates",
  "senderEmail": "marketing@example.com",
  "content": "Welcome to our September newsletter!",
  "scheduledAt": "2026-12-20T10:00:00"
}
```

### 2. Add recipients

```http
POST /api/campaigns/1/recipients
Content-Type: application/json
```

```json
{
  "recipients": [
    {
      "name": "Rahul",
      "email": "rahul@example.com"
    },
    {
      "name": "Priya",
      "email": "priya@example.com"
    }
  ]
}
```

### 3. Schedule

```http
POST /api/campaigns/1/schedule
```

### 4. Process due campaigns manually

```http
POST /api/campaigns/process
```

### 5. Process one campaign manually

```http
POST /api/campaigns/1/process
```

### 6. List campaigns

```http
GET /api/campaigns?page=0&size=10&sort=desc
```

Filter by status:

```http
GET /api/campaigns?status=SCHEDULED
```

Search by name:

```http
GET /api/campaigns?name=September
```

### 7. Campaign details

```http
GET /api/campaigns/1
```

### 8. Statistics

```http
GET /api/campaigns/1/statistics
```

Example:

```json
{
  "success": true,
  "message": "Campaign statistics retrieved successfully",
  "data": {
    "campaignId": 1,
    "totalRecipients": 100,
    "delivered": 80,
    "failed": 15,
    "pending": 5
  }
}
```

## Processing design

The scheduler checks for due SCHEDULED campaigns every 30 seconds.

Each campaign is locked using a JPA pessimistic write lock before processing. Therefore concurrent requests cannot process the same campaign simultaneously.

Each PENDING recipient is randomly marked DELIVERED or FAILED. Once all recipients have been handled, the campaign becomes COMPLETED.

## Database design

### campaigns

- id
- name
- subject
- sender_email
- content
- scheduled_at
- status
- created_at
- updated_at
- version

### recipients

- id
- campaign_id
- name
- email
- status
- created_at

A unique constraint on `(campaign_id, email)` prevents duplicate addresses within the same campaign.

## Production improvements

For production, this could be improved with:

- Flyway/Liquibase migrations
- Authentication/authorization
- Queue such as Kafka/RabbitMQ
- Retry and dead-letter handling
- Idempotency keys
- Distributed locking
- Metrics/monitoring
- Structured logging
- Rate limiting
- Integration tests using Testcontainers
