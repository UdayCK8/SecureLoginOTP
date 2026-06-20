# Prompt to Create PPT for SecureLoginOTP Project

Copy and paste the prompt below into ChatGPT, Claude, Gemini, or any AI presentation maker to generate a complete PowerPoint outline and content for this project.

---

## PROMPT (Copy everything below this line)

```
Create a PowerPoint presentation for my college project called "Secure Login System Using Email-Based OTP Authentication in Java". The presentation should be 10 slides, use simple language that a college student can easily explain during a viva, and include examples and diagrams.

Here are the project details:

PROJECT OVERVIEW:
- It is a Java-based login system with Two-Factor Authentication (2FA)
- First, the user enters username and password
- Second, a 6-digit OTP is sent to their email
- Third, they enter the OTP to complete login
- It works in TWO modes: (1) Web Application (Spring Boot + HTML/JS) and (2) Console Application (command line menu)
- Tech stack: Java 17, Spring Boot 3.2, MySQL 8, Jakarta Mail API (SMTP), BCrypt for hashing, Maven, JUnit 5 + Mockito

SLIDE STRUCTURE (create exactly these slides):

SLIDE 1: Title Slide
- Project title: "Secure Login System Using Email-Based OTP Authentication"
- Subtitle: "Java-based Two-Factor Authentication System"
- My name, roll number, college name, department
- Guide name

SLIDE 2: Agenda / What We Will Cover
- List 6-7 topics in bullet points

SLIDE 3: Problem Statement (Why this project?)
- Simple passwords are not safe enough
- Hackers can guess or steal passwords
- We need a second layer of security
- OTP sent to email is something only the real user has
- Real-world examples: Bank apps, Gmail, Amazon — all use 2FA

SLIDE 4: Objectives
- Build a secure login system with 2FA
- Send OTP via email using SMTP
- Hash passwords and OTPs using BCrypt (never store plain text)
- Provide both web and console interfaces
- Log all login attempts for security audit

SLIDE 5: Technologies Used (with icons or simple visuals)
- Java 17 — programming language
- Spring Boot — web framework and REST API
- MySQL 8 — database to store users and OTPs
- Jakarta Mail API — sends emails via SMTP
- BCrypt (jBCrypt) — hashes passwords securely
- Maven — builds the project and manages libraries
- JUnit 5 + Mockito — testing
- HTML/CSS/JS — web frontend
- For each tech, give ONE LINE explaining what it does in this project

SLIDE 6: System Architecture (ASCII-style diagram)
- Show 5 layers with arrows:
  1. User (Browser or Terminal)
  2. Web Frontend / Console UI
  3. Spring Boot Controller (REST API)
  4. Java Service Layer (business logic)
  5. Database Layer (MySQL)
  6. Email Service (SMTP)
- Explain each layer in 1 sentence

SLIDE 7: Database Design
- Show 3 tables with their columns:

  Table 1: users
  - user_id (primary key)
  - username (unique)
  - password_hash (BCrypt, NOT plain text)
  - email (unique)
  - created_at

  Table 2: otp_records
  - otp_id (primary key)
  - user_id (foreign key)
  - otp_code_hash (BCrypt hashed OTP)
  - expires_at (5 minutes from creation)
  - is_used (true/false)
  - attempt_count (0 to 3)

  Table 3: login_audit
  - audit_id
  - username
  - stage (PASSWORD_CHECK, OTP_SENT, OTP_VERIFIED, etc.)
  - result (SUCCESS or FAILURE)
  - attempt_time

- Explain: Why we hash OTPs, why we track attempts, why we log everything

SLIDE 8: Registration Flow (Step-by-step with simple diagram)
1. User enters: username, password, email
2. System checks: Is username taken? Is email taken? Is password strong?
3. System hashes the password with BCrypt
4. Stores user in database
5. Shows "Registration successful"
- Include a small flowchart or numbered arrows

SLIDE 9: Login Flow — Step 1 (Password Check)
1. User enters username and password
2. System finds user in database
3. System compares entered password with stored BCrypt hash
4. If correct → go to Step 2 (send OTP)
5. If wrong → show error and log failure
- Mention: This is the FIRST factor of 2FA

SLIDE 10: Login Flow — Step 2 (OTP Generation & Email)
1. System generates a random 6-digit number (e.g., 483920) using SecureRandom
2. Cleans old/expired OTPs from database
3. Hashes the OTP with BCrypt and stores it with 5-minute expiry
4. Sends the PLAIN OTP to user's email via SMTP (Gmail/Outlook)
5. Logs the email attempt
- Show a sample email:
  Subject: Your Secure Login OTP
  Body: Your OTP is: 483920. Valid for 5 minutes.

SLIDE 11: Login Flow — Step 3 (OTP Verification)
1. User enters the 6-digit OTP from their email
2. System checks:
   a. Is the OTP format valid? (exactly 6 digits)
   b. Is there an active OTP for this user?
   c. Has it expired? (5-minute timer)
   d. Have attempts exceeded 3?
   e. Does the entered OTP match the stored BCrypt hash?
3. If all pass → LOGIN SUCCESSFUL!
4. If any fail → show specific error (wrong, expired, max attempts)
- Show a small decision tree / flowchart

SLIDE 12: Security Features (Important for viva!)
- Passwords are NEVER stored as plain text → always BCrypt hashed
- OTPs are also hashed, not stored plain
- SQL uses PreparedStatement → no SQL injection
- OTP expires in 5 minutes → time-bound security
- OTP can be used only once (is_used flag)
- Max 3 wrong attempts → prevents brute force
- Invalid format also counts as attempt → smarter brute-force protection
- All actions logged in login_audit table → full traceability
- SMTP credentials in config file (gitignored) → never in source code

SLIDE 13: Web vs Console Mode
- Web Mode:
  - Open browser at http://localhost:8081
  - Pages: Home, Register, Login, Dashboard, Change Password
  - Live OTP countdown timer
  - Session-based authentication
  
- Console Mode:
  - Run in terminal/command prompt
  - Menu: 1=Register, 2=Login, 3=Change Password, 4=Exit
  - Password hidden with asterisks (if terminal supports)
  - Same security, same database

SLIDE 14: Screenshots / UI Mockups (Describe what to show)
- Suggest these screenshots:
  1. Home page (index.html)
  2. Registration form
  3. Login form (password step)
  4. OTP entry with countdown timer
  5. Dashboard showing "Welcome, [username]"
  6. Console menu in terminal
- If no real screenshots available, describe what each screen looks like

SLIDE 15: Testing
- JUnit 5 + Mockito tests cover:
  - Password hashing and verification
  - OTP generation (always 6 digits)
  - Input validation (username, password, email rules)
  - AuthService logic with mocked database
  - Controller REST endpoints with mocked service
- Command: mvn test

SLIDE 16: Future Scope / Possible Extensions
- Account lockout after too many failed attempts
- Add SMS OTP using Twilio API
- React or Angular frontend instead of plain HTML
- Email verification before allowing registration
- Password reset via email link
- Docker deployment for easy sharing

SLIDE 17: Conclusion
- Summary: We built a secure 2FA login system in Java
- Key takeaway: Real-world security = layers (password + OTP + hashing + logging)
- Works in both web and console modes
- All data is protected with BCrypt and audit trails

SLIDE 18: Thank You / Q&A
- Thank you
- Questions?
- GitHub link (if any)

---

SPEAKER NOTES: For each slide, give 2-3 sentences of what the presenter should say. Keep it natural and simple, not robotic.

DIAGRAM STYLE: Where diagrams are needed, describe them in simple ASCII art or text-based boxes-and-arrows that can be easily recreated in PowerPoint shapes.

LANGUAGE: Use college-level English. No heavy jargon without explanation. Every technical term should have a one-line simple explanation.
```

---

## How to Use This Prompt

1. **Copy the entire prompt above** (from the ``` block)
2. **Paste it into ChatGPT, Claude, Gemini, or any AI**
3. **The AI will generate**:
   - Slide-by-slide content
   - Speaker notes
   - Diagram descriptions
   - Simple explanations for every technical point
4. **Copy the output into PowerPoint / Google Slides / Canva**

---

## Tips for Making the PPT Look Good

- Use **1-2 colors max** (e.g., dark blue + orange accent)
- Keep **bullet points short** (max 5 per slide)
- Add **icons** from Flaticon or PowerPoint icons for each technology
- For the architecture diagram: use PowerPoint **SmartArt → Hierarchy** or draw boxes with arrows
- For database tables: use PowerPoint **Tables** with header row colored
- Add **page numbers** at the bottom
- Use **large fonts** (title 32pt, body 20-24pt) so judges can read from distance

---

## Suggested Title Slide Design

```
+------------------------------------------------+
|                                                |
|        SECURE LOGIN SYSTEM          |
|                                                |
|    Using Email-Based OTP Authentication       |
|           in Java (Spring Boot)                |
|                                                |
|    -------------------------------------       |
|                                                |
|    Presented by: [Your Name]                  |
|    Roll No: [Your Roll Number]                |
|    Department of [Your Department]            |
|    [Your College Name]                        |
|                                                |
|    Under the Guidance of: [Guide Name]        |
|                                                |
+------------------------------------------------+
```
