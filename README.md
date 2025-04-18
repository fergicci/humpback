# 🎛 Humpback Studio - Architecture Design Specification

## 1. Overview

**Humpback Studio** is a dynamic web application designed to showcase and manage a home recording studio environment. It includes a frontend built with **React.js**, a backend using **Java with Spring Boot**, and deployment on **Amazon Web Services (AWS)**.

## 2. Functional Requirements

- Interactive landing page
- Equipment and gear list
- Booking/contact form
- Media gallery
- Admin interface (optional, future)
- API for content and control

## 3. System Architecture

### Components
- **Frontend:** React.js with modern hooks and component architecture
- **Backend:** Spring Boot REST API
- **Hosting:** AWS (S3 for static assets, EC2 or Elastic Beanstalk for backend)
- **CI/CD:** GitHub Actions (planned)

📌 _Architecture diagram will be added here._

## 4. Folder Structure

```
/humpback
├── backend/       # Spring Boot project
├── frontend/      # React.js project
├── .github/       # GitHub Actions, CODEOWNERS, templates
└── README.md
```

## 5. Development Workflow

- `main`: stable, production-ready
- `develop`: integration branch for feature work
- `<developer-initials>/<feature-id>`: short-lived branches off `develop`
- All changes go through **pull requests**
- Code review and approval process enforced via GitHub branch rules

## 6. Backend Design

- **Framework:** Spring Boot 3+
- **Language:** Java 21
- **Build Tool:** Gradle
- **API:** REST (JSON)
- **Database:** (TBD) PostgreSQL or another AWS-compatible solution
- **Security:** Spring Security + JWT/OAuth2 (planned)

## 7. Frontend Design

- **Framework:** React.js (Vite or CRA)
- **Routing:** React Router
- **State Management:** Context API (or Redux if needed)
- **UI Library:** (TBD: Material UI / Tailwind / Custom)
- **API Integration:** Axios or Fetch

## 8. Deployment Strategy

- **Frontend:** AWS S3 + CloudFront
- **Backend:** AWS EC2 or Elastic Beanstalk
- **DNS:** Route53 (already set up)
- **Secrets:** Environment variables, `.env`, and AWS Secrets Manager (planned)

## 9. Testing Strategy

- **Backend:** JUnit
- **Frontend:** Jest, React Testing Library (TBD)
- **CI Integration:** GitHub Actions for test automation (future milestone)

## 10. Roadmap & Milestones

- ✅ Repository created
- ✅ Branch rules set up
- 🔄 Frontend scaffolding
- 🔄 Backend scaffolding
- ⏳ Initial deployment (dev)
- ⏳ Media & contact feature
- ⏳ Admin interface (optional)

## 11. Contributors & Code Ownership

- **@fergicci** – Project Owner, Architect, and Developer  

Contribution guidelines and issue templates will be added as the project grows.
