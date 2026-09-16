# Email Campaign Management API(Backend only)

A backend REST API for managing email campaigns, recipients, scheduling, and simulated email delivery.

The application is built using Java and Spring Boot with MySQL as the database.

---

## Tech Stack

* Java 17
* Spring Boot
* Spring Web
* MySQL 8
* Maven
* Docker & Docker Compose
* JUnit

---

<img width="2873" height="1688" alt="Screenshot 2026-09-16 205339" src="https://github.com/user-attachments/assets/b8add61c-a8e8-4160-bbb0-f8e4d33e3c50" />


## Features

* Create email campaigns
* Add multiple recipients to a campaign
* Prevent duplicate recipients within the same campaign
* Schedule campaigns
* Simulate email delivery with delivered/failed statuses
* Campaign status management
* View campaign details
* Dockerized application and MySQL database

---

## Running the Application with Docker

### Prerequisites

* Docker Desktop
* Git

### Clone the repository

```bash
git clone <your-github-repository-url>
cd email-campaign-api
```

### Environment Configuration

Create a local `.env` file based on `.env.example`.

Example:

```env
DB_USERNAME=root
DB_PASSWORD=your_database_password
SERVER_PORT=8080
```

The `.env` file should not be committed to the repository.

### Start the application

```bash
docker compose up --build
```

Docker Compose starts:

* MySQL database
* Spring Boot application

The API will be available at:

```text
http://localhost:8080
```

### Stop the application

```bash
docker compose down
```

---

## Project Structure

```text
src/
├── main/
│   ├── java/com/example/campaign/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   │
│   └── resources/
│       └── application.yml
│
└── test/
    └── java/com/example/campaign/

Dockerfile
docker-compose.yml
schema.sql
.env.example
pom.xml
```

---

## Database Design

The application uses two main tables: Campaign & Recipient

### Campaign

Stores the main information about an email campaign.

Important fields include:

* `id`
* `name`
* `subject`
* `sender_email`
* `content`
* `scheduled_at`
* `status`
* `created_at`
* `updated_at`

Campaign statuses:

```text
DRAFT
SCHEDULED
PROCESSING
COMPLETED
```

### Recipient

Stores recipients associated with a campaign.

Important fields include:

* `id`
* `campaign_id`
* `name`
* `email`
* `status`
* `created_at`

Recipient statuses:

```text
PENDING
DELIVERED
FAILED
```

A foreign key connects each recipient to its campaign.

A unique constraint on:

```text
(campaign_id, email)
```

prevents the same email address from being added more than once to the same campaign.

Indexes are used on commonly queried fields to improve database access.

The database schema is provided in `schema.sql`.

---

## Campaign Workflow

A campaign follows this basic lifecycle:

```text
DRAFT
  |
  | Add recipients
  |
  v
DRAFT
  |
  | Schedule
  |
  v
SCHEDULED
  |
  | Processing
  |
  v
PROCESSING
  |
  | All recipients processed
  |
  v
COMPLETED
```

A campaign must contain at least one recipient before it can be scheduled.

Only campaigns in `DRAFT` status can be scheduled.

The scheduled time must be in the future.

---

## API Endpoints

### Create Campaign

```http
POST /api/campaigns
```

Example request:

```json
{
  "name": "Kasplo Product Update",
  "subject": "Introducing Our Latest Product Updates",
  "senderEmail": "sender@example.com",
  "content": "Hello, here are our latest product updates.",
  "scheduledAt": "2026-12-15T10:00:00"
}
```

A newly created campaign has the status:

```text
DRAFT
```

---

### Add Recipients

```http
POST /api/campaigns/{campaignId}/recipients
```

Example request:

```json
{
  "recipients": [
    {
      "name": "Shashank",
      "email": "shash@example.com"
    },
    {
      "name": "Akki",
      "email": "akki@example.com"
    }
  ]
}
```

The API validates email addresses and prevents duplicate emails within the same campaign.

---

### Schedule Campaign

```http
POST /api/campaigns/{campaignId}/schedule
```

A campaign can only be scheduled when:

* The campaign exists
* The campaign is in `DRAFT` status
* At least one recipient has been added
* The scheduled time is in the future

After successful scheduling, the campaign status becomes:

```text
SCHEDULED
```

---

### Process Campaigns

```http
POST /api/campaigns/process
```

This endpoint triggers campaign processing.

Each pending recipient is randomly assigned one of the following outcomes:

```text
DELIVERED
FAILED
```

No actual email is sent.

After all recipients are processed, the campaign status becomes:

```text
COMPLETED
```

The application also includes a background scheduler that checks for campaigns that are ready for processing.

---

### List Campaigns

```http
GET /api/campaigns
```

Supports:

* Pagination
* Status filtering
* Campaign name search
* Sorting by creation date

Example:

```http
GET /api/campaigns?page=0&size=10&status=DRAFT&search=Kasplo&sortBy=createdAt&sortDirection=desc
```

---

### Get Campaign Details

```http
GET /api/campaigns/{campaignId}
```

Returns campaign information along with its recipients.

---

### Get Campaign Statistics

```http
GET /api/campaigns/{campaignId}/statistics
```

Returns:

* Total recipients
* Delivered count
* Failed count
* Pending count

Example response:

```json
{
  "total": 10,
  "delivered": 7,
  "failed": 2,
  "pending": 1
}
```

---

## API Testing

A Postman collection is included in the repository:

```text
postman/Email-Campaign-API.postman_collection.json
```

The collection contains requests for the main campaign operations.

The API can also be tested using tools such as `curl`.

---

## Validation and Error Handling

The API validates incoming requests using Spring Boot Bean Validation.

Examples of validation include:

* Required campaign fields
* Valid sender email
* Valid recipient email
* Duplicate recipient prevention
* Campaign existence
* Campaign status validation
* At least one recipient before scheduling
* Future scheduled time

A global exception handler provides consistent JSON error responses.

Example:

```json
{
  "success": false,
  "message": "Campaign not found"
}
```

---

## Concurrency Handling

Campaign processing is protected against concurrent processing using a database pessimistic lock.

When a campaign is being processed, the campaign record is locked before checking and updating its status.

This helps prevent multiple requests or scheduler executions from processing the same campaign simultaneously.

Campaign status checks also prevent an already completed campaign from being processed again.

---

## Testing

The project includes unit tests covering important business rules, including:

* Valid campaign creation
* Duplicate recipient validation
* Scheduling without recipients
* Preventing processing of completed campaigns
* Campaign statistics

Tests can be executed as part of the Maven build.

---

## Technical Decisions

### Spring Boot

Spring Boot was selected to provide a structured approach for developing REST APIs with dependency injection, validation, exception handling, and database integration.

### Spring Data JPA

Spring Data JPA is used to simplify database operations and map Java entities to MySQL tables.

### MySQL

MySQL provides persistent relational storage for campaigns and recipients and allows database-level constraints to enforce data integrity.

### DTOs

DTOs are used to separate API request/response models from database entities.

### Service Layer

Business rules are implemented in the service layer instead of placing business logic directly inside controllers.

### Global Exception Handling

A centralized exception handler provides consistent API error responses.

### Background Processing

A scheduled background process checks for campaigns that are ready to be processed.

### Pessimistic Locking

Database-level locking is used to reduce the possibility of the same campaign being processed concurrently.

### Docker

Docker Compose provides a reproducible environment containing both the application and MySQL database.

---

## Production Improvements

For a production-ready implementation, the following improvements could be considered:

* Integrate with a real email delivery provider.
* Introduce a message queue such as Kafka or RabbitMQ.
* Implement retry mechanisms for failed deliveries.

---

## Security

Sensitive configuration should be provided through environment variables.

The repository contains `.env.example` with placeholder values only.

Actual `.env` files, passwords, API keys, and other credentials should not be committed to GitHub.

---

## License

This project was developed as part of a backend development assignment.
