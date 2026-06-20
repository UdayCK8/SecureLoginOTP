# Secure Login System Using Email-Based OTP Authentication in Java

A Java project implementing two-factor authentication: username/password login followed by a 6-digit OTP sent to the user's registered email via SMTP (Jakarta Mail API).

This project supports **two modes of operation**:

1. **🌐 Web Application** — Spring Boot REST API with a responsive HTML/CSS/JS frontend (default, runs on port `8081`)
2. **💻 Console Application** — Command-line menu-driven interface (run `Main.java` directly)

Both modes share the same database, business logic (`AuthService`), and security architecture.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17+ |
| Web Framework | Spring Boot 3.2.12 (Web + Embedded Tomcat) |
| Database | MySQL 8 (JDBC via `mysql-connector-j`) |
| Email | Jakarta Mail API 2.0.1 (SMTP with STARTTLS) |
| Security | BCrypt (`jbcrypt`) for password & OTP hashing |
| Build Tool | Maven |
| Testing | JUnit 5 + Mockito |
| Frontend | Vanilla HTML5, CSS3, JavaScript |

---

## Maven Dependencies (pom.xml)

Below are all the libraries used in this project, their exact Maven coordinates, and official links.

### Production Dependencies

| # | Library | Maven Coordinates | Version | Official Link |
|---|---|---|---|---|
| 1 | **Spring Boot Web** | `org.springframework.boot:spring-boot-starter-web` | 3.2.12 | [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/3.2.12/reference/html/) |
| 2 | **MySQL Connector** | `com.mysql:mysql-connector-j` | 9.7.0 | [MySQL Docs](https://dev.mysql.com/doc/connector-j/en/) |
| 3 | **Jakarta Mail** | `com.sun.mail:jakarta.mail` | 2.0.1 | [Jakarta Mail Docs](https://eclipse-ee4j.github.io/mail/docs/api/) |
| 4 | **BCrypt (jBCrypt)** | `org.mindrot:jbcrypt` | 0.4 | [jBCrypt GitHub](https://github.com/jeremyh/jBCrypt) |

### Test Dependencies

| # | Library | Maven Coordinates | Version | Official Link |
|---|---|---|---|---|
| 5 | **JUnit 5** | `org.junit.jupiter:junit-jupiter` | 5.11.0 | [JUnit 5 Docs](https://junit.org/junit5/docs/current/user-guide/) |
| 6 | **Mockito Core** | `org.mockito:mockito-core` | 5.15.2 | [Mockito Docs](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html) |
| 7 | **Mockito JUnit** | `org.mockito:mockito-junit-jupiter` | 5.15.2 | [Mockito Docs](https://javadoc.io/doc/org.mockito/mockito-junit-jupiter/latest/) |
| 8 | **Spring Boot Test** | `org.springframework.boot:spring-boot-starter-test` | 3.2.12 | [Spring Test Docs](https://docs.spring.io/spring-boot/docs/3.2.12/reference/html/features.html#features.testing) |

### Build Plugins

| # | Plugin | Maven Coordinates | Version | Purpose |
|---|---|---|---|---|
| 9 | **Maven Compiler** | `org.apache.maven.plugins:maven-compiler-plugin` | 3.13.0 | Compiles Java 17 source code |
| 10 | **Maven Surefire** | `org.apache.maven.plugins:maven-surefire-plugin` | 3.5.2 | Runs JUnit 5 tests during build |
| 11 | **Spring Boot Plugin** | `org.springframework.boot:spring-boot-maven-plugin` | 3.2.12 | Packages JAR + repackages with all dependencies |

### How to Find & Download Any Dependency

All dependencies are downloaded **automatically** by Maven from [Maven Central](https://repo1.maven.org/maven2/) when you run `mvn clean package`. No manual download is needed.

If you want to search for any Java library manually:
1. **Maven Central Search:** https://search.maven.org/
2. **Maven Repository:** https://mvnrepository.com/
3. Search by `groupId:artifactId` (e.g. `org.mindrot:jbcrypt`)

### Full pom.xml dependency snippet

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MySQL JDBC Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.7.0</version>
    </dependency>

    <!-- Jakarta Mail API (Sun/Eclipse implementation) -->
    <dependency>
        <groupId>com.sun.mail</groupId>
        <artifactId>jakarta.mail</artifactId>
        <version>2.0.1</version>
    </dependency>

    <!-- BCrypt for password hashing -->
    <dependency>
        <groupId>org.mindrot</groupId>
        <artifactId>jbcrypt</artifactId>
        <version>0.4</version>
    </dependency>

    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.11.0</version>
        <scope>test</scope>
    </dependency>

    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.15.2</version>
        <scope>test</scope>
    </dependency>

    <!-- Mockito JUnit Jupiter integration -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <version>5.15.2</version>
        <scope>test</scope>
    </dependency>

    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Project Structure

```
SecureLoginOTP/
├── pom.xml                                    -> Maven build configuration
├── sql/schema.sql                             -> MySQL database schema (run first)
├── src/main/resources/
│   ├── application.properties                 -> Spring Boot config (server.port=8081)
│   ├── config.properties.template             -> copy to config.properties and fill in
│   └── static/                                -> Web frontend (HTML/CSS/JS)
│       ├── index.html
│       ├── login.html
│       ├── register.html
│       ├── dashboard.html
│       ├── change-password.html
│       ├── css/styles.css
│       └── js/app.js
├── src/main/java/com/secureauth/
│   ├── SecureLoginOtpWebApplication.java     -> Spring Boot entry point
│   ├── config/ServiceConfig.java             -> Spring bean configuration
│   ├── controller/AuthController.java        -> REST API endpoints
│   ├── dto/                                   -> Request/response DTOs
│   │   ├── ApiResponse.java
│   │   ├── RegisterRequest.java
│   │   ├── LoginPasswordRequest.java
│   │   ├── OtpIssueRequest.java
│   │   ├── OtpVerifyRequest.java
│   │   └── ChangePasswordRequest.java
│   ├── exception/GlobalExceptionHandler.java -> Global error handler
│   ├── model/                                 -> Entity classes
│   │   ├── User.java
│   │   └── OtpRecord.java
│   ├── dao/                                   -> Database access
│   │   ├── UserDAO.java
│   │   ├── OtpDAO.java
│   │   └── LoginAuditDAO.java
│   ├── service/                               -> Business logic
│   │   ├── AuthService.java
│   │   └── EmailService.java
│   ├── util/                                  -> Utilities
│   │   ├── AppConfig.java
│   │   ├── DBConnection.java
│   │   ├── PasswordUtil.java
│   │   ├── OtpGenerator.java
│   │   └── ValidationUtil.java
│   └── main/Main.java                         -> Console entry point
└── src/test/java/com/secureauth/             -> JUnit 5 + Mockito unit tests
    ├── controller/AuthControllerTest.java
    ├── service/AuthServiceTest.java
    ├── util/OtpGeneratorTest.java
    ├── util/PasswordUtilTest.java
    └── util/ValidationUtilTest.java
```

---

## Prerequisites (Software You Need to Install)

Before running this project, make sure you have the following software installed on your computer:

| Software | Version | Download Link | Why You Need It |
|---|---|---|---|
| **Java JDK** | 17 or higher | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/) | To compile and run the Java code |
| **Apache Maven** | 3.8+ | [Maven Download](https://maven.apache.org/download.cgi) | To build the project and manage dependencies |
| **MySQL Server** | 8.0+ | [MySQL Community](https://dev.mysql.com/downloads/mysql/) | To store users, OTPs, and audit logs |
| **MySQL Workbench** (optional) | 8.0+ | [MySQL Workbench](https://dev.mysql.com/downloads/workbench/) | GUI tool to run SQL scripts and manage the database |
| **Git** (optional) | Any | [Git Download](https://git-scm.com/downloads) | To clone the repository |
| **An IDE** (optional) | — | [IntelliJ IDEA](https://www.jetbrains.com/idea/download/), [Eclipse](https://www.eclipse.org/downloads/), or [VS Code](https://code.visualstudio.com/) | For easier coding, running, and debugging |

> **Quick Check:** After installing, verify with these commands in your terminal/command prompt:
> ```bash
> java -version      # Should show 17 or higher
> mvn -version       # Should show Maven 3.8+
> mysql --version    # Should show MySQL 8.0+
> ```

---

## Setup Steps

### 1. Database

```sql
-- In MySQL Workbench / mysql CLI:
SOURCE sql/schema.sql;
```

This creates the `secure_login_otp` database with three tables:
- `users` — registered accounts (BCrypt-hashed passwords)
- `otp_records` — issued OTPs with expiry and attempt tracking
- `login_audit` — audit trail of login attempts, OTP verifications, and password changes

### 2. Gmail App Password

Gmail SMTP requires an **App Password**, not your normal Gmail password:

1. Go to https://myaccount.google.com/security
2. Turn on **2-Step Verification** (required before App Passwords appear)
3. Go to **App Passwords** → generate one for "Mail"
4. Copy the 16-character password (remove spaces when pasting)

> **Tip:** You can use any SMTP provider (Outlook, Yahoo, SendGrid, etc.) by updating the `mail.smtp.*` settings in `config.properties`.

### 3. Configuration

```bash
cp src/main/resources/config.properties.template src/main/resources/config.properties
```

Edit `config.properties` and fill in your credentials:

```properties
# ---- Database ----
db.host=localhost
db.port=3306
db.name=secure_login_otp
db.user=root
db.password=YOUR_MYSQL_PASSWORD

# ---- Email ----
mail.username=youraddress@gmail.com
mail.app.password=YOUR_16_CHAR_APP_PASSWORD

# ---- SMTP Server Settings ----
# Defaults below work for Gmail. Change for Outlook/others.
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.starttls.enable=true
```

`config.properties` is listed in `.gitignore` — it will never be committed to Git.

---

## How to Run

### Option 1: Web Application (Recommended)

**Build & run:**

```bash
mvn clean package
java -jar target/secure-login-otp.jar
```

**Then open your browser:** http://localhost:8081/

You will see the homepage with links to **Register**, **Login**, and **Change Password**.

**Or run directly from an IDE:**

Import as a Maven project, then run `SecureLoginOtpWebApplication.java`. The embedded Tomcat server starts automatically on port `8081`.

**Or if you have Spring Boot CLI:**

```bash
mvn spring-boot:run
```

### Option 2: Console Application

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.secureauth.main.Main"
```

Or run `Main.java` directly from your IDE.

> **IDE Note:** If running inside an IDE, `System.console()` may return `null`, so password input falls back to `Scanner` (visible typing). Use a real terminal for hidden password entry.

---

## Build & Run (JAR Method) — Quick Reference

This is the fastest way to run the project on any machine. Follow these steps in order:

### Step 1: Build the JAR

Open a terminal in the project root folder and run:

```bash
mvn clean package
```

**What this does:**
- Downloads all dependencies (Spring Boot, MySQL driver, Jakarta Mail, BCrypt, etc.)
- Compiles the Java source code
- Runs all unit tests
- Packages everything into a single runnable JAR file at `target/secure-login-otp.jar`

> **Troubleshoot:** If tests fail, you can skip them with:
> ```bash
> mvn clean package -DskipTests
> ```

### Step 2: Run the JAR

#### 🌐 For Web Mode (default)

```bash
java -jar target/secure-login-otp.jar
```

Then open your browser: **http://localhost:8081/**

> The server runs on port **8081** (set in `application.properties`). Make sure no other app is using that port.

#### 💻 For Console Mode

```bash
java -cp target/secure-login-otp.jar com.secureauth.main.Main
```

### Step 3: Stop the Application

Press `Ctrl + C` in the terminal to stop the running server.

### Where is the JAR file?

```
SecureLoginOTP/
└── target/
    └── secure-login-otp.jar
```

**JAR file size:** ~25-35 MB (includes all dependencies bundled inside).

> **Note:** The JAR is self-contained. You can copy `secure-login-otp.jar` and `config.properties` to another computer with Java 17+ installed, and it will run there without needing Maven or any other setup.

---

## Full Functionality & Detailed Workflow

This section describes every module and user journey in detail, including data flow, layer interaction, and screen-by-step behavior for both Web Mode and Console Mode.

---

## Codebase Inventory

| # | Feature | Web Mode | Console Mode | Description |
|---|---|---|---|---|
| 1 | **User Registration** | Register page (`register.html`) | Main Menu option 1 | Create an account with username, password, email |
| 2 | **Login (3-Step MFA)** | Login page (`login.html`) | Main Menu option 2 | Step 1: Password → Step 2: OTP issuance → Step 3: OTP verification |
| 3 | **Resend OTP** | "Resend OTP" button in login | Login sub-option 2 | Invalidate old OTP, generate & email a fresh one |
| 4 | **Change Password** | Change Password page (`change-password.html`) | Main Menu option 3 | Verify old password, set new password with strength rules |
| 5 | **Logout** | "Logout" button on dashboard | Not applicable | Destroy session and return to home page |
| 6 | **Dashboard** | Dashboard page (`dashboard.html`) | Shown inline after login | Welcome screen showing logged-in user |
| 7 | **Audit Logging** | Invisible (background) | Invisible (background) | Every password check, OTP send/verify, password change is logged |

---

## Architecture Overview (Layer Diagram)

```
┌──────────────────────────────────────────────────────────────────┐
│                         PRESENTATION                              │
│  ┌────────────────────┐       ┌──────────────────────────────┐  │
│  │ Web Frontend        │       │ Console UI (Main.java)       │  │
│  │ HTML + CSS + JS     │       │ Scanner + System.console()   │  │
│  │ (fetch API calls)   │       │ (menu-driven text flow)      │  │
│  └─────────┬───────────┘       └────────────┬─────────────────┘  │
│            │ HTTP requests                  │ direct Java call   │
├────────────┼────────────────────────────────┼────────────────────┤
│            ▼                                ▼                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │             CONTROLLER (AuthController.java)              │   │
│  │  REST endpoints: /api/auth/register, /login/*, etc.      │   │
│  │  Spring Boot @RestController + HttpSession state         │   │
│  │  Session attributes: PENDING_USERNAME, AUTHENTICATED_USER│   │
│  └──────────┬────────────────────────────────┬───────────────┘   │
│             │ method call                    │ method call       │
├─────────────┼────────────────────────────────┼───────────────────┤
│             ▼                                ▼                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              SERVICE (AuthService.java)                   │   │
│  │  Core business logic: register(), authenticatePassword() │   │
│  │  issueOtp(), verifyOtp(), resendOtp(), changePassword()  │   │
│  └──────────┬──────────┬──────────────┬──────────────┬──────┘   │
│             │          │              │              │           │
├─────────────┼──────────┼──────────────┼──────────────┼───────────┤
│             ▼          ▼              ▼              ▼           │
│  ┌──────────────┐ ┌────────────┐ ┌────────────┐ ┌───────────┐  │
│  │ UserDAO       │ │ OtpDAO      │ │ EmailService│ │ LoginAudit│  │
│  │ (users table) │ │ (otp_records│ │ (SMTP via  │ │ DAO       │  │
│  │               │ │  table)     │ │  Jakarta   │ │ (audit    │  │
│  │               │ │             │ │  Mail)     │ │  table)   │  │
│  └──────┬───────┘ └──────┬─────┘ └──────┬─────┘ └─────┬─────┘  │
│         │                │              │             │          │
├─────────┼────────────────┼──────────────┼─────────────┼──────────┤
│         ▼                ▼              ▼             ▼          │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                  MySQL Database                           │   │
│  │  Tables: users | otp_records | login_audit               │   │
│  └──────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────┘
```

---

## Module 1: User Registration

### What It Does
Creates a new user account by storing a BCrypt-hashed password, username, and email in the `users` table. Validates all inputs before writing to the database.

### Input Validation Rules

| Field | Rule | Error Message |
|---|---|---|
| Username | 4–20 characters, letters/digits/underscore only | "Invalid username format" |
| Password | Minimum 8 characters, at least 1 letter and 1 digit | "Password too weak: min 8 chars with letters and digits" |
| Email | Standard email format (regex) | "Invalid email format" |

### Registration Flow (Detailed)

```
User enters username, password, email
        │
        ▼
┌──────────────────┐
│ ValidationUtil   │ — checks username, password, email format
└────────┬─────────┘
        │ valid?
   ┌────┴────┐
   ▼         ▼
 YES        NO → return INVALID_USERNAME / INVALID_PASSWORD / INVALID_EMAIL
   │
   ▼
┌──────────────────┐
│ UserDAO          │ — check if username already exists
└────────┬─────────┘
        │ exists?
   ┌────┴────┐
   ▼         ▼
  NO        YES → return USERNAME_TAKEN
   │
   ▼
┌──────────────────┐
│ UserDAO          │ — check if email already exists
└────────┬─────────┘
        │ exists?
   ┌────┴────┐
   ▼         ▼
  NO        YES → return EMAIL_TAKEN
   │
   ▼
┌──────────────────┐
│ PasswordUtil     │ — hash(password) → BCrypt hash with cost factor 12
└────────┬─────────┘
        │
        ▼
┌──────────────────┐
│ UserDAO          │ — INSERT INTO users (username, password_hash, password_salt, email)
└────────┬─────────┘
        │
        ▼
  return SUCCESS
```

### Database Change
```sql
INSERT INTO users (username, password_hash, password_salt, email)
VALUES ('alice', '$2a$12$...', '', 'alice@gmail.com');
```

### Web Mode Experience
1. Navigate to `register.html`
2. Fill username, password, email
3. Frontend validates format in real-time (JS)
4. Click "Register" → POST `/api/auth/register`
5. On success: redirected to `login.html`
6. On failure: error message displayed (e.g. "Username is already taken")

### Console Mode Experience
1. Choose menu option **1. Register**
2. Enter username, password (hidden if terminal supports it), email
3. See immediate result message (✔ Registration successful / ✘ errors)

---

## Module 2: Login (Three-Step Multi-Factor Authentication)

This is the core feature. Login is split into **3 distinct steps** across **2 channels** (password + email OTP).

---

### Step 2.1 — Password Verification

#### What It Does
Checks if the username exists and the password matches the stored BCrypt hash.

#### Password Check Flow

```
User enters username + password
        │
        ▼
┌──────────────────┐
│ UserDAO          │ — SELECT * FROM users WHERE username = ?
└────────┬─────────┘
        │ found?
   ┌────┴────┐
   ▼         ▼
  YES        NO → log audit (FAILURE) → return USER_NOT_FOUND
   │
   ▼
┌──────────────────┐
│ PasswordUtil     │ — verifyPassword(inputPassword, storedHash)
└────────┬─────────┘
        │ match?
   ┌────┴────┐
   ▼         ▼
  YES        NO → log audit (FAILURE) → return WRONG_PASSWORD
   │
   ▼
 log audit (SUCCESS) → return SUCCESS
```

#### Audit Log Entry
```sql
INSERT INTO login_audit (username, stage, result)
VALUES ('alice', 'PASSWORD_CHECK', 'SUCCESS');
```

#### Web Mode
- POST `/api/auth/login/password` → `{username, password}`
- On success: server sets `PENDING_USERNAME` in HTTP session
- On failure: 401 Unauthorized with error message

#### Console Mode
- After typing username and password
- See ✔ "Password verified" or ✘ "Incorrect password"

---

### Step 2.2 — OTP Issuance

#### What It Does
Generates a cryptographically secure 6-digit OTP, hashes it with BCrypt, stores it with a 5-minute expiry, and emails the **plain** OTP to the user's registered email address.

#### OTP Issue Flow

```
Password verified successfully
        │
        ▼
┌──────────────────┐
│ OtpDAO           │ — DELETE FROM otp_records WHERE expires_at < NOW()
└────────┬─────────┘      (cleans old/expired OTPs)
        │
        ▼
┌──────────────────┐
│ OtpGenerator     │ — SecureRandom generates 6-digit number (e.g. 483920)
└────────┬─────────┘
        │
        ▼
┌──────────────────┐
│ PasswordUtil     │ — hash(otp) → BCrypt hash of the OTP
└────────┬─────────┘
        │
        ▼
┌──────────────────┐
│ OtpDAO           │ — INSERT INTO otp_records (user_id, otp_code_hash, expires_at)
└────────┬─────────┘      expires_at = NOW() + 5 minutes
        │
        ▼
┌──────────────────┐
│ EmailService     │ — sends HTML email with the plain OTP via SMTP (Gmail/Outlook/etc.)
└────────┬─────────┘
        │
   ┌────┴────┐
   ▼         ▼
Email sent  Email failed
   │            │
   ▼            ▼
 log audit   log audit
(SUCCESS)   (FAILURE)
   │
   ▼
 return SENT
```

#### Email Content (Example)
```
Subject: Your Secure Login OTP

Your one-time password (OTP) is: 483920

This code is valid for 5 minutes.
If you did not request this, please ignore this email.
```

#### Database Change
```sql
INSERT INTO otp_records (user_id, otp_code_hash, expires_at, is_used, attempt_count)
VALUES (1, '$2a$12$...', '2026-06-20 14:35:00', FALSE, 0);
```

#### Web Mode
- POST `/api/auth/login/otp/issue` (requires `PENDING_USERNAME` session)
- On success: frontend shows OTP input box with a **live countdown timer** (5:00, 4:59, ...)
- On failure: error message (e.g. "Failed to send OTP email")

#### Console Mode
- System automatically proceeds after password success
- Message: "✔ OTP sent! Please check your email (and Spam folder)."

---

### Step 2.3 — OTP Verification

#### What It Does
Compares the user's entered OTP against the stored BCrypt hash. Enforces expiry (5 min), max attempts (3), and single-use (`is_used` flag).

#### OTP Verify Flow

```
User enters 6-digit OTP
        │
        ▼
┌──────────────────┐
│ ValidationUtil   │ — check format (exactly 6 digits)
└────────┬─────────┘
        │ valid format?
   ┌────┴────┐
   ▼         ▼
  YES        NO → increment attempt_count → return WRONG_OTP
   │
   ▼
┌──────────────────┐
│ OtpDAO           │ — SELECT latest active OTP for user
└────────┬─────────┘
        │ found?
   ┌────┴────┐
   ▼         ▼
  YES        NO → return NO_ACTIVE_OTP
   │
   ▼
│ attempt_count >= 3? │
   ┌────┴────┐
   ▼         ▼
  YES        NO → continue
   │
   ▼
return MAX_ATTEMPTS_EXCEEDED
   │
   ▼
│ NOW() > expires_at? │
   ┌────┴────┐
   ▼         ▼
  YES        NO → continue
   │
   ▼
return EXPIRED
   │
   ▼
┌──────────────────┐
│ PasswordUtil     │ — verifyPassword(enteredOtp, storedOtpHash)
└────────┬─────────┘
        │ match?
   ┌────┴────┐
   ▼         ▼
  YES        NO → increment attempt_count → return WRONG_OTP
   │
   ▼
┌──────────────────┐
│ OtpDAO           │ — UPDATE otp_records SET is_used = TRUE
└────────┬─────────┘
        │
        ▼
 log audit (SUCCESS)
   │
   ▼
 return VERIFIED
```

#### Attempt Counting
- Every submission (valid format or invalid) increments `attempt_count`
- 3 wrong attempts → `MAX_ATTEMPTS_EXCEEDED` → user must restart login entirely
- Invalid format also counts (prevents brute-forcing only format-valid guesses)

#### Audit Log Entry
```sql
INSERT INTO login_audit (username, stage, result)
VALUES ('alice', 'OTP_VERIFIED', 'SUCCESS');
-- or 'FAILURE' for wrong/expired OTP
```

#### Database Change
```sql
UPDATE otp_records
SET is_used = TRUE, attempt_count = attempt_count + 1
WHERE otp_id = 1;
```

#### Web Mode
- POST `/api/auth/login/otp/verify` → `{otp}`
- On success: session gets `AUTHENTICATED_USER`, removes `PENDING_USERNAME`
- Redirects to `dashboard.html`
- On failure: shows specific error (wrong, expired, max attempts, etc.)

#### Console Mode
- Enter 6-digit OTP
- See result: ✔✔✔ "LOGIN SUCCESSFUL!" or ✘ specific failure reason
- Menu returns to main menu on failure

---

## Module 3: Resend OTP

#### What It Does
Invalidates the previous OTP (marks all active OTPs as used) and generates a fresh one with a new 5-minute timer.

#### Resend Flow

```
User clicks "Resend OTP" / chooses option 2
        │
        ▼
┌──────────────────┐
│ OtpDAO           │ — UPDATE otp_records SET is_used = TRUE
└────────┬─────────┘      WHERE user_id = X AND is_used = FALSE
        │
        ▼
┌──────────────────┐
│ AuthService      │ — calls issueOtp() (same as Step 2.2)
└────────┬─────────┘
        │
        ▼
 New OTP generated, stored, emailed
 log audit (OTP_RESENT, SUCCESS)
```

#### Web Mode
- Click "Resend OTP" button during login
- Timer resets to 5:00
- Previous OTP becomes invalid immediately

#### Console Mode
- In OTP phase, choose option **2. Resend OTP**
- System invalidates old one and sends new email

---

## Module 4: Change Password

#### What It Does
Allows a user to change their password after verifying their old password. New password must meet strength rules and cannot be the same as the old one.

#### Change Password Flow

```
User enters username, old password, new password, confirm password
        │
        ▼
│ new password == confirm password? │
   ┌────┴────┐
   ▼         ▼
  YES        NO → "Passwords do not match"
   │
   ▼
┌──────────────────┐
│ ValidationUtil   │ — isStrongPassword(newPassword)?
└────────┬─────────┘
        │ strong?
   ┌────┴────┐
   ▼         ▼
  YES        NO → "New password too weak..."
   │
   ▼
┌──────────────────┐
│ AuthService      │ — authenticatePassword(username, oldPassword)
└────────┬─────────┘
        │ correct?
   ┌────┴────┐
   ▼         ▼
  YES        NO → "Current password is incorrect"
   │
   ▼
│ newPassword == oldPassword? │
   ┌────┴────┐
   ▼         ▼
  NO         YES → "New password must be different from old"
   │
   ▼
┌──────────────────┐
│ PasswordUtil     │ — hash(newPassword) → new BCrypt hash
└────────┬─────────┘
        │
        ▼
┌──────────────────┐
│ UserDAO          │ — UPDATE users SET password_hash = ? WHERE user_id = ?
└────────┬─────────┘
        │
        ▼
 log audit (PASSWORD_CHANGE_SUCCESS)
 return SUCCESS
```

#### Web Mode
- Navigate to `change-password.html`
- Fill form with username, old password, new password, confirm password
- On success: "Password changed!" → redirect to `login.html`

#### Console Mode
- Choose menu option **3. Change Password**
- Enter username, old password, new password, confirm password
- See result message

---

## Module 5: Logout

#### What It Does
Destroys the HTTP session, removing `PENDING_USERNAME` and `AUTHENTICATED_USER`.

#### Web Mode
- POST `/api/auth/logout`
- Session invalidated
- Redirected to `index.html`

#### Console Mode
- No explicit logout; login state is transient per session

---

## Module 6: Dashboard / Welcome Screen

#### Web Mode (`dashboard.html`)
- Protected page: requires `AUTHENTICATED_USER` session
- Displays: "Welcome, [username]!"
- Contains: **Logout** button
- If unauthenticated: auto-redirect to `login.html`

#### Console Mode
- After successful OTP verification, prints:
  ```
  ✔✔✔ LOGIN SUCCESSFUL! Welcome, alice. ✔✔✔
  ```
- Returns to main menu (logout is implicit by returning to menu)

---

## Module 7: Audit Logging (Background)

Every security-relevant action is logged to the `login_audit` table.

| Stage | When Logged | Result |
|---|---|---|
| `PASSWORD_CHECK` | After every username+password attempt | SUCCESS / FAILURE |
| `OTP_SENT` | After OTP email attempt | SUCCESS / FAILURE |
| `OTP_RESENT` | After resend OTP | SUCCESS |
| `OTP_VERIFIED` | After every OTP submission | SUCCESS / FAILURE |
| `PASSWORD_CHANGE_ATTEMPT` | After failed change password | FAILURE |
| `PASSWORD_CHANGE_SUCCESS` | After successful change password | SUCCESS |

#### Example Audit Log
```sql
SELECT * FROM login_audit;
+----------+----------+---------------------+------------------+---------+
| audit_id | username | attempt_time        | stage            | result  |
+----------+----------+---------------------+------------------+---------+
| 1        | alice    | 2026-06-20 14:30:00 | PASSWORD_CHECK   | SUCCESS |
| 2        | alice    | 2026-06-20 14:30:05 | OTP_SENT         | SUCCESS |
| 3        | alice    | 2026-06-20 14:31:20 | OTP_VERIFIED     | SUCCESS |
| 4        | alice    | 2026-06-20 14:35:00 | PASSWORD_CHANGE_ | SUCCESS |
+----------+----------+---------------------+------------------+---------+
```

---

## Web Frontend Page-by-Page Flow

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  index.html │────▶│ register.html │────▶│  login.html │
│   (Home)    │     │  (Sign Up)    │     │  (Sign In)  │
└──────┬──────┘     └───────────────┘     └──────┬──────┘
       │                                          │
       │     ┌────────────────┐    ┌─────────────┐│
       └────▶│ change-password│    │dashboard.html│◀┘
             │     .html      │◀───│  (Welcome)   │
             └────────────────┘    └─────────────┘
```

| Page | URL | Purpose | Auth Required |
|---|---|---|---|
| Home | `/index.html` | Landing page with feature overview | No |
| Register | `/register.html` | Create new account | No |
| Login | `/login.html` | 3-step MFA login | No |
| Dashboard | `/dashboard.html` | Welcome screen + logout | Yes (`AUTHENTICATED_USER`) |
| Change Password | `/change-password.html` | Update password | No (but needs old password) |

### Login Page UI Flow (Web)
```
┌─────────────────────────────┐
│  Step 1: Enter Credentials  │
│  [Username    ]             │
│  [Password    ]             │
│  [Submit]                   │
└─────────────┬───────────────┘
              │ success
              ▼
┌─────────────────────────────┐
│  Step 2: Wait for OTP       │
│  "Sending OTP to your email"│
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│  Step 3: Enter OTP          │
│  [6-digit OTP ]             │
│  ⏱ Timer: 04:32             │
│  [Verify]  [Resend OTP]     │
└─────────────┬───────────────┘
              │ success
              ▼
       dashboard.html
```

The frontend JavaScript (`app.js`) handles:
- Real-time client-side validation (username, password, email, OTP format)
- AJAX calls to all REST endpoints
- Session-aware page protection (redirect unauthenticated users)
- Live OTP countdown timer (5-minute expiry)
- Loading spinners and user-friendly alerts

---

## Console Menu Flow

```
==================================================
   SECURE LOGIN SYSTEM - EMAIL OTP AUTHENTICATION
==================================================

----------- MAIN MENU -----------
1. Register
2. Login
3. Change Password
4. Exit
Enter your choice: _

--- Option 2: LOGIN ---
Username: alice
Password: ********
✔ Password verified. Sending OTP to your registered email...
✔ OTP sent! Please check your email (and Spam folder).

--- OTP VERIFICATION ---
1. Enter OTP
2. Resend OTP
3. Cancel login
Enter your choice: 1
Enter the 6-digit OTP: 483920

✔✔✔ LOGIN SUCCESSFUL! Welcome, alice. ✔✔✔
```

---

## Configuration & Constants

All configurable values are centralized in `AppConfig.java`:

| Constant | Value | Description |
|---|---|---|
| `OTP_LENGTH` | 6 | Number of digits in the OTP |
| `OTP_VALID_MINUTES` | 5 | Time before OTP expires |
| `OTP_MAX_ATTEMPTS` | 3 | Max wrong attempts before lockout |
| PasswordUtil cost factor | 12 | BCrypt work factor (2^12 iterations) |

---

## API Endpoints Reference

All endpoints return JSON wrapped in `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "...",
  "data": null
}
```

| Method | Endpoint | Description | Session State |
|---|---|---|---|
| `POST` | `/api/auth/register` | Create a new account | — |
| `POST` | `/api/auth/login/password` | Step 1: verify password | Sets `PENDING_USERNAME` |
| `POST` | `/api/auth/login/otp/issue` | Step 2a: generate & email OTP | Requires `PENDING_USERNAME` |
| `POST` | `/api/auth/login/otp/resend` | Step 2b: invalidate old, issue new OTP | Requires `PENDING_USERNAME` |
| `POST` | `/api/auth/login/otp/verify` | Step 3: verify the 6-digit OTP | Requires `PENDING_USERNAME`, sets `AUTHENTICATED_USER` on success |
| `POST` | `/api/auth/change-password` | Change password (requires auth) | — |
| `POST` | `/api/auth/logout` | Destroy session | Clears all session data |
| `GET` | `/api/auth/me` | Get current authenticated user | Requires `AUTHENTICATED_USER` |

**HTTP Status Codes:**
- `200 OK` — success
- `400 Bad Request` — invalid input (weak password, bad email, etc.)
- `401 Unauthorized` — wrong password, wrong/expired OTP, not authenticated
- `403 Forbidden` — max OTP attempts exceeded
- `404 Not Found` — user not found
- `409 Conflict` — username/email already taken
- `500 Internal Server Error` — database or SMTP error

---

## Running Tests

```bash
mvn test
```

Runs unit tests for:
- `PasswordUtil` — BCrypt hashing & verification
- `OtpGenerator` — OTP format & uniqueness
- `ValidationUtil` — username, password, email validation rules
- `AuthService` — business logic with mocked DAOs
- `AuthController` — REST endpoint behavior with mocked service

---

## Security Notes (for viva)

- **Passwords and OTPs are never stored in plain text** — hashed with **BCrypt** (adaptive cost factor 12), which is strongly resistant to brute-force attacks compared to SHA-256.
- All SQL uses `PreparedStatement` exclusively → no SQL injection surface.
- OTPs expire after 5 minutes and are single-use (`is_used` flag).
- OTP verification is rate-limited to 3 attempts per OTP.
- Invalid OTP **format** submissions also count as attempts, preventing brute-forcing of format-valid OTPs only.
- Old/expired OTP records are cleaned from the database on each new issuance to prevent table bloat.
- SMTP credentials are loaded from a gitignored config file or environment variables — never hardcoded in source.
- `mail.smtp.*` settings are configurable, so the app can use any SMTP provider (Gmail, Outlook, SendGrid, etc.).
- Password input uses `System.console().readPassword()` for masking (with a Scanner IDE fallback).
- `login_audit` table logs every password/OTP check and password change for traceability.
- HTTP sessions are used for state management in web mode; sessions are invalidated on logout.

---

## Possible Extensions (mention in report if asked for future scope)

- Add account lockout after repeated failed login attempts.
- Replace static HTML/JS frontend with a React/Vue SPA.
- Add SMS OTP fallback via Twilio.
- Add email verification during registration (send a confirmation link).
- Implement password reset via email token.
- Add rate-limiting middleware at the controller level (e.g., bucket4j).
- Dockerize the application for easy deployment.
