# My Team Health Service (MTHS)

## Overview

**My Team Health Service (MTHS)** is a modern healthcare platform designed to connect **patients, doctors, pharmacists, hospitals, and other healthcare providers** in one seamless ecosystem.

At its core, MTHS is built on the belief that healthcare works best when it is **team-driven**. Instead of treating health as a one-way service, MTHS empowers patients, hospitals, and providers to collaborate, share knowledge, and make better decisions together.

The platform goes beyond traditional healthcare apps by offering:

* A **secure and personalized space** for patients to manage their health records and care history.
* A **collaborative environment** where doctors, pharmacists, and hospitals can coordinate treatment and prescriptions.
* A **scalable structure** that adapts to individuals, small clinics, and large healthcare institutions.

## Our Mission

Our mission is to make healthcare:

* **Collaborative** – bringing patients, doctors, pharmacists, and hospitals into one team.
* **Accessible** – ensuring everyone can connect with the right providers and facilities.
* **Efficient** – simplifying processes, reducing delays, and improving health outcomes.

With MTHS, every user — whether a patient, a provider, or a hospital — becomes part of a **trusted health team**, where information flows seamlessly, communication is transparent, and care is continuous.

## Architecture

MTHS is built as a **monolithic application** for:
- Simplified deployment and development
- Better performance for integrated features
- Easier debugging and testing
- Lower operational complexity

The application follows a **feature-based package structure** organizing code by business domains rather than technical layers.

## Technology Stack

- **Backend**: Spring Boot 3.5.3 with Java 21
- **Database**: PostgreSQL
- **Authentication**: JWT-based authentication with Spring Security
- **File Storage**: MinIO object storage
- **Email**: Spring Mail with MailHog (development)
- **Real-time Communication**: WebSockets (SockJS + STOMP)
- **Security**: Role-based access control (RBAC) with permissions
- **Validation**: Jakarta Bean Validation
- **Mapping**: MapStruct for DTO conversions

## Getting Started

### Prerequisites

- Java 21
- Docker and Docker Compose
- PostgreSQL (or use Docker container)
- Maven

### Setup

1. Clone the repository
2. Configure environment variables in `.env` file
3. Start MinIO and MailHog:
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Services

- **Auth Service**: Port 8081
- **MinIO Console**: http://localhost:9001
- **MailHog UI**: http://localhost:8025

## Project Structure

```
MTHS/
├── auth-service/          # Authentication and user management service
├── docker-compose.yml     # Full stack Docker configuration
├── docker-compose.dev.yml # Development services (MinIO, MailHog)
└── .env                   # Environment variables
```

## Contributing

This project is under active development. More documentation will be added as the project evolves.

## License

[Add your license here]
