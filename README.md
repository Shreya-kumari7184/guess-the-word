# Guess the Word

## Overview

Guess the Word is a web-based word guessing game with separate `PLAYER` and `ADMIN` roles. Players register and sign in, play daily word games, and review their guesses. Administrators can view daily and per-user activity reports.

## Features

- Player registration and session-based login
- Optional ADMIN account initialization from environment variables
- A database word bank of exactly 20 uppercase, five-letter words
- Random server-side word selection
- Up to 3 games per player per day and 5 guesses per game
- GREEN, ORANGE, and GREY letter feedback with duplicate-letter handling
- Stored games and ordered guess history
- Daily and per-user admin reports
- BCrypt password hashing
- Secret words are not included in game start or game history responses

## Technology Stack

**Frontend:** React, Vite, JavaScript, CSS, React Router  
**Backend:** Java 21, Spring Boot 4.1.1, Spring Data JPA, Spring Security, Maven  
**Database:** MySQL

## Project Structure

The Maven backend project is in `guesstheword/`; the Vite frontend is its sibling directory.

```text
Guess the Word/
├── frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.js
└── guesstheword/
    ├── pom.xml
    ├── mvnw
    ├── mvnw.cmd
    └── src/
        ├── main/java/com/guessword/guesstheword/
        ├── main/resources/application.properties
        └── test/java/com/guessword/guesstheword/
```

## Database Setup

Create the MySQL database:

```sql
CREATE DATABASE guess_word;
```

Configure these environment variables before starting the backend:

- `DB_URL` — defaults to `jdbc:mysql://localhost:3306/guess_word?useSSL=false&serverTimezone=UTC`
- `DB_USERNAME` — defaults to `root`
- `DB_PASSWORD` — database password; defaults to empty
- `ADMIN_USERNAME` — optional username for the initial ADMIN account
- `ADMIN_PASSWORD` — optional password for the initial ADMIN account

Use your own values locally. Do not commit real credentials. If either admin variable is absent or blank, the application skips initial ADMIN account creation. The configured admin password is stored using BCrypt.

For example, in Windows PowerShell, set placeholders to your local values before running the application:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/guess_word?useSSL=false&serverTimezone=UTC"
$env:DB_USERNAME = "<YOUR_MYSQL_USERNAME>"
$env:DB_PASSWORD = "<YOUR_MYSQL_PASSWORD>"
$env:ADMIN_USERNAME = "<YOUR_ADMIN_USERNAME>"
$env:ADMIN_PASSWORD = "<YOUR_ADMIN_PASSWORD>"
```

## Running the Backend

From the workspace root, after configuring the environment variables:

```powershell
cd guesstheword
.\mvnw.cmd spring-boot:run
```

The API listens on port `8080`. On systems where the Maven wrapper cannot run, use `mvn spring-boot:run` with Maven installed.

## Running the Frontend

In a second terminal from the workspace root:

```powershell
cd frontend
npm install
npm run dev
```

With PowerShell script execution restricted, use `npm.cmd install` and `npm.cmd run dev`. Vite serves the frontend at `http://localhost:5173` and proxies `/api` requests to the backend at `http://localhost:8080`.

## API Overview

All routes below use the existing backend APIs. Login uses an HTTP session cookie; the frontend sends requests with credentials enabled.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a PLAYER account |
| `POST` | `/api/auth/login` | Sign in and establish a session |
| `POST` | `/api/auth/logout` | End the session |
| `GET` | `/api/auth/me` | Get the current authenticated user |
| `POST` | `/api/games/start` | Start a game |
| `POST` | `/api/games/{gameId}/guess` | Submit a five-letter guess |
| `GET` | `/api/games/{gameId}` | Get a player's game and ordered guesses |
| `GET` | `/api/admin/reports/daily?date=YYYY-MM-DD` | Get daily activity totals |
| `GET` | `/api/admin/reports/user/{userId}` | Get a user's activity by date |

The game history API retrieves one game by ID; there is no backend endpoint to list all games. The frontend keeps recently started game IDs in browser storage and also allows a player to enter a game ID. The backend verifies ownership for every history request.

## Game Rules

- A player can start at most 3 games per day.
- Each game has at most 5 guesses.
- Each guess must be exactly 5 letters.
- **GREEN:** the letter is correct and in the correct position.
- **ORANGE:** the letter is in the word but in a different position.
- **GREY:** the letter is not available in the word. Repeated letters are evaluated without matching more occurrences than the secret word contains.
- A game ends when the player guesses the word or submits the fifth incorrect guess. Completed games reject further guesses.

## Admin Reports

- **Daily report:** the selected date, distinct users who played that day, and saved correct guesses for their games.
- **User report:** one row per game date for the selected user, with games/words tried and saved correct guesses.

Only ADMIN accounts can access the report endpoints. PLAYER access is rejected by backend authorization.

## Testing

The application has been tested across the backend, frontend, database, and API layers.

Backend Testing
Authentication tests
Registration validation
Admin authorization
Word bank initialization
Game creation
Daily game limit
Guess validation
Duplicate-letter handling
Win/loss scenarios
Maximum guess limit
Game ownership
Game history
Admin reporting
API Testing

Postman was used to verify:

Registration
     ↓
Login
     ↓
Start Game
     ↓
Submit Guess
     ↓
Game Completion
     ↓
Game History
     ↓
Admin Reports
Frontend Testing

The React application was tested through the browser for:

Login
Registration
Player dashboard
Game interface
Guess submission
Game completion
History
Admin dashboard
Protected routes
Build Verification
Backend Build       ✅
Frontend Build      ✅
Automated Tests     ✅
API Testing         ✅
Database Testing    ✅
Browser Testing     ✅

## Security

- Passwords are hashed with BCrypt.
- Authentication uses Spring Security and server-side HTTP sessions; the frontend does not use JWT.
- Admin report routes require the ADMIN role.
- Backend authorization remains authoritative for player and admin access.
- The secret word is not returned before or after game completion by the current game responses.

## 📸 Screenshots

### 🔐 Login

<img width="959" height="503" alt="image" src="https://github.com/user-attachments/assets/55c3fe30-2f1f-44c9-9619-02bc4b373e22" />

### 📝 Registration

<img width="959" height="505" alt="image" src="https://github.com/user-attachments/assets/d5adc7b8-c401-4559-b1ce-5d97ec0a3086" />

### 🏠 Player Dashboard

<img width="647" height="349" alt="image" src="https://github.com/user-attachments/assets/43b1b223-b39b-40b6-9bdb-08370a599cf0" />

### 🎮 Game

<img width="577" height="163" alt="image" src="https://github.com/user-attachments/assets/4e191af9-d88d-4887-a542-0bc5f9aea723" />

### 📜 Game History

<img width="645" height="349" alt="image" src="https://github.com/user-attachments/assets/ae1bf605-afe3-4716-9cea-67ae56643255" />


### 👑 Admin Dashboard

<img width="669" height="343" alt="image" src="https://github.com/user-attachments/assets/02c5f6ef-dffd-45fd-be94-9adfab85bd32" />


## GitHub

GitHub Repository: https://github.com/Shreya-kumari7184/guess-the-word
