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

# 📁 Project Structure: Humpback Studio

```text
/humpback
├── backend/                           # Spring Boot backend (APIs, security, DB)
├── frontend/                          # React + Vite frontend (SPA)
│   ├── .env.local                     # Environment-specific variables (used by Vite)
│   ├── .gitignore                     # Git ignore rules (node_modules, etc.)
│   ├── Dockerfile                     # Dockerfile to build the frontend
│   ├── README.md                      # Frontend-specific documentation
│   ├── eslint.config.js              # ESLint rules for code quality
│   ├── index.html                     # Entry HTML template for Vite
│   ├── package.json                   # Project metadata and dependencies
│   ├── package-lock.json              # Lock file for exact dependency versions
│   ├── public/                        # Static assets publicly served
│   │   └── favicon.ico                # Website favicon icon
│   ├── src/                           # Source code root
│   │   ├── App.tsx                    # Main React component (entry view)
│   │   ├── app/                       # Redux store setup
│   │   │   └── store.ts               # Configures and exports Redux store
│   │   ├── assets/                    # Images and static media used in components
│   │   │   ├── console_01.jpg         # Image of the ASP8024 mixing console
│   │   │   └── humpback-logo.png      # Humpback Studio logo
│   │   ├── components/                # Reusable UI components
│   │   │   ├── BookingForm.tsx        # Booking form component
│   │   │   ├── Footer.tsx             # Site footer
│   │   │   ├── GearIntroPanel.tsx     # Highlight panel for the ASP8024 console
│   │   │   ├── Header.tsx             # Navigation bar
│   │   │   ├── Hero.tsx               # Homepage banner section
│   │   │   └── NewsPanel.tsx          # Homepage news panel component
│   │   ├── env.d.ts                   # Type declarations for custom env vars
│   │   ├── features/                  # Domain-specific Redux state/features
│   │   │   └── booking                # Booking-specific Redux state (if any)
│   │   ├── i18n/                      # i18n (internationalization) support
│   │   │   ├── en/                    # English translations
│   │   │   ├── pt-br/                 # Portuguese (Brazil) translations
│   │   │   ├── index.ts               # i18n configuration (with namespaces)
│   │   │   └── keys.ts                # Constants for translation keys
│   │   ├── main.tsx                   # App entry point (mounts React DOM)
│   │   ├── pages/                     # Route-level page components
│   │   │   ├── Booking.tsx            # Booking page (calendar, form)
│   │   │   ├── Contact.tsx            # Contact page (form + studio info)
│   │   │   ├── Gallery.tsx            # Gallery page (photos and videos)
│   │   │   ├── Gear.tsx               # Gear page (list and description)
│   │   │   ├── Home.tsx               # Home page (hero + news panel)
│   │   │   └── Shopping.tsx           # Placeholder for e-commerce or merch
│   │   ├── styles/                    # Global stylesheets
│   │   │   └── main.scss              # Global SCSS file
│   │   ├── types/                     # Custom global type declarations
│   │   │   └── assets.d.ts            # Allows importing .png/.jpg/etc as modules
│   │   └── utils/                     # Utility functions and helpers
│   │       └── datetimeUtils.ts       # Utilities for date and time formatting
│   ├── tsconfig.app.json              # TypeScript config for app files
│   ├── tsconfig.json                  # Base TypeScript config
│   ├── tsconfig.node.json             # TypeScript config for Node-related files
│   └── vite.config.ts                 # Vite configuration (plugins, aliases, env loading)
├── .github/                           # GitHub configuration (Actions, templates)
│   ├── workflows/                     # CI/CD pipeline workflows (e.g., build, deploy)
│   └── ISSUE_TEMPLATE/                # Issue and PR templates
└── README.md                          # Main project readme (overview, setup instructions)
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
- **Database:** MongoDB
- **Security:** Spring Security + JWT/OAuth2 (planned)

## 7. Frontend Design

- **Framework:** React.js (Vite)
- **Routing:** React Router
- **State Management:** Context API and Redux
- **UI Library:** Bootstrap
- **API Integration:** Axios or Fetch

## 8. Deployment Strategy

- **Frontend:** AWS
- **Backend:** AWS
- **DNS:** GoDaddy
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
