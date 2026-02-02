# Chess Clone Microservices

A real-time, distributed chess platform built with a modern microservices architecture. This project enables players to register, find opponents through a matchmaking system, and play chess matches with live timers and move validation.

## 🏗️ Architecture

The system is designed with a decentralized approach, ensuring scalability and resilience.

* **API Gateway**: The central entry point (Port 8080) that routes requests to specific services based on URL patterns.
* **Service Discovery (Eureka)**: Maintains a dynamic registry of all active service instances.
* **Asynchronous Messaging (RabbitMQ)**: Facilitates decoupled communication, such as profile creation events and game result processing.
* **Real-time State (Redis)**: Stores active game FEN strings, matchmaking queues, and high-frequency timer data.
* **WebSockets (STOMP)**: Enables bi-directional, real-time communication for moves and clock updates.

---

## 🛠️ Microservices Overview

### 1. IAM Service (Auth)

Manages user identity and security.

* **Responsibilities**: Registration, JWT-based authentication, and session management.
* **Base Path**: `/auth/**`

### 2. Game Engine Service

The core logic engine of the platform.

* **Responsibilities**: Move validation (UCI), board state management, and match synchronization.
* **Base Path**: `/game/**`

### 3. Matchmaking Service

Connects players based on skill level.

* **Responsibilities**: Skill-based queuing and match initialization.
* **Base Path**: `/matchmaking/**`

### 4. User Profile Service

Handles player personas and statistics.

* **Responsibilities**: Managing ratings (ELO), profile details, and internal rating lookups.
* **Base Path**: `/user/**`

### 5. Timer Referee Service

Ensures fair play through time management.

* **Responsibilities**: Initializing game clocks and managing turn-based countdowns.
* **Base Path**: `/timer/**`

---

## 🚀 API Endpoints

### Authentication (IAM Service)

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/auth/register` | Create a new account |
| POST | `/auth/login` | Authenticate and receive a JWT cookie |
| POST | `/auth/logout` | Invalidate the current session |

### Matchmaking

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/matchmaking/start` | Join the queue for a specific game mode |
| POST | `/matchmaking/cancel` | Leave the matchmaking queue |

### Game Logic (REST)

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/game/match/{id}/metadata` | Fetch player IDs, mode, and match status |
| GET | `/user/profile` | Retrieve the current user's profile |

### WebSockets (Game Engine)

* **Connection**: `ws://localhost:8080/game/ws`
* **Destination `/app/move**`: Send a move (requires `from`, `to`, `matchId`).
* **Destination `/app/match/sync**`: Request full current match state.
* **Topic `/topic/match/{matchId}**`: Receive live move and timer updates.

---

## ⚙️ Setup & Requirements

* **Java 17+**
* **Redis**: Required for match state and timer persistence.
* **RabbitMQ**: Required for cross-service event handling.
* **Eureka Server**: Must be running for service discovery.
* **Gateway Configuration**:
* **Port**: 8080
* **JWT Secret**: Must be consistent across IAM and Gateway.