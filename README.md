# Secure Login System Using Email-Based OTP Authentication in Java

A console-based Java mini project implementing two-factor authentication:
username/password login followed by a 6-digit OTP sent to the user's
registered email via SMTP (Jakarta Mail API).

## Tech Stack
- Java 17+
- MySQL 8 (JDBC via mysql-connector-j)
- Jakarta Mail API 2.0.1 (SMTP with STARTTLS)
- Maven (build + dependency management)
- **BCrypt** for password & OTP hashing (replaces SHA-256)
- JUnit 5 + Mockito for unit tests

## Project Structure
```
SecureLoginOTP/
├── pom.xml
├── sql/schema.sql                     -> run this first in MySQL
├── src/main/resources/
│   └── config.properties.template     -> copy to config.properties and fill in
└── src/main/java/com/secureauth/
    ├── model/      User.java, OtpRecord.java
    ├── dao/        UserDAO.java, OtpDAO.java, LoginAuditDAO.java
    ├── util/       AppConfig.java, DBConnection.java, PasswordUtil.java,
    │               OtpGenerator.java, ValidationUtil.java
    ├── service/    AuthService.java (business logic), EmailService.java (SMTP)
    └── main/       Main.java (console UI / entry point)

src/test/java/com/secureauth/         <- JUnit 5 + Mockito unit tests
```

## Setup Steps

### 1. Database
```sql
-- In MySQL Workbench / mysql CLI:
SOURCE sql/schema.sql;
```
This creates the `secure_login_otp` database with `users`, `otp_records`,
and `login_audit` tables.

> **Note:** If you are migrating from a previous version that used SHA-256,
> drop the old tables and recreate them — BCrypt hashes are incompatible
> with the old salt+SHA-256 format.

### 2. Gmail App Password
Gmail SMTP requires an **App Password**, not your normal Gmail password:
1. Go to https://myaccount.google.com/security
2. Turn on **2-Step Verification** (required before App Passwords appear)
3. Go to **App Passwords** → generate one for "Mail"
4. Copy the 16-character password (remove spaces)

### 3. Configuration
```bash
cp src/main/resources/config.properties.template src/main/resources/config.properties
```
Edit `config.properties` and fill in:
```properties
db.host=localhost
db.port=3306
db.name=secure_login_otp
db.user=root
db.password=YOUR_MYSQL_PASSWORD

mail.username=youraddress@gmail.com
mail.app.password=YOUR_16_CHAR_APP_PASSWORD

# ---- SMTP Server Settings ----
# Defaults below work for Gmail. Change for Outlook/others.
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.starttls.enable=true
```
`config.properties` is in `.gitignore` — it will never be pushed to GitHub.

### 4. Build & Run
```bash
mvn clean package
java -jar target/secure-login-otp.jar
```
(The Maven Shade plugin bundles the JDBC driver, Jakarta Mail, and BCrypt
into one runnable JAR, so no manual classpath setup is needed.)

### Running Tests
```bash
mvn test
```
Runs unit tests for `PasswordUtil`, `OtpGenerator`, `ValidationUtil`, and
`AuthService` with mocked DAOs.

### Running from an IDE (Eclipse / IntelliJ / VS Code)
Import as a Maven project — dependencies in `pom.xml` will be fetched
automatically — then run `Main.java`.

> **IDE Note:** If running inside an IDE, `System.console()` may return
> `null`, so password input falls back to `Scanner` (visible typing).
> Use a real terminal for hidden password entry.

## How It Works (Workflow)
1. **Register**: user supplies username, password, email → password is
   hashed with **BCrypt** → stored in `users`. Passwords are never stored
   in plain text.
2. **Login step 1**: username/password checked with BCrypt.
3. **Login step 2**: a 6-digit OTP is generated with `SecureRandom`,
   BCrypt-hashed and stored in `otp_records` with a 5-minute expiry.
   Old/expired OTPs are cleaned up automatically.
   The plain OTP is emailed via SMTP.
4. **Login step 3**: user enters the OTP (max 3 attempts); on a correct,
   unexpired, unused match, login succeeds.
   *Resend OTP*: during the OTP stage, choose option 2 to invalidate the
   previous OTP and receive a fresh one.
5. **Change Password**: after authenticating with the old password, set a
   new BCrypt-hashed password. The new password must be different from the
   old one and meet strength requirements.

## Security Notes (for viva)
- Passwords and OTPs are **never** stored in plain text — hashed with
  **BCrypt** (adaptive cost factor 12), which is strongly resistant to
  brute-force attacks compared to SHA-256.
- All SQL uses `PreparedStatement` exclusively → no SQL injection surface.
- OTPs expire after 5 minutes and are single-use (`is_used` flag).
- OTP verification is rate-limited to 3 attempts per OTP.
- Invalid OTP **format** submissions also count as attempts, preventing
  brute-forcing of format-valid OTPs only.
- Old/expired OTP records are cleaned from the database on each new
  issuance to prevent table bloat.
- SMTP credentials are loaded from a gitignored config file or
  environment variables — never hardcoded in source.
- `mail.smtp.*` settings are configurable, so the app can use any SMTP
  provider (Gmail, Outlook, SendGrid, etc.).
- Password input uses `System.console().readPassword()` for masking
  (with a Scanner IDE fallback).
- `login_audit` table logs every password/OTP check and password change
  for traceability.

## Possible Extensions (mention in report if asked for future scope)
- Add account lockout after repeated failed login attempts.
- Move from console UI to a Spring Boot REST API + web frontend.
- Add SMS OTP fallback via Twilio (free tier).
