<p align="center">
  <a href="https://github.com/roberto22palomar/pepenium/actions/workflows/build.yml">
    <img alt="Build" src="https://github.com/roberto22palomar/pepenium/actions/workflows/build.yml/badge.svg" />
  </a>
</p>

<p align="center">
  <a href="LICENSE">
    <img alt="License" src="https://img.shields.io/badge/License-MIT-green.svg" />
  </a>
  <img alt="Java" src="https://img.shields.io/badge/Java-8%2B-blue.svg" />
  <img alt="Maven" src="https://img.shields.io/badge/Maven-3.x-orange.svg" />
  <img alt="JUnit" src="https://img.shields.io/badge/JUnit-5-purple.svg" />
  <img alt="Selenium" src="https://img.shields.io/badge/Selenium-4-43B02A.svg" />
  <img alt="Appium Client" src="https://img.shields.io/badge/Appium%20Client-10-00BFFF.svg" />
</p>

# 🧪 Pepenium – Test Automation Framework

<p align="center">
  🇬🇧 <strong>English</strong> · 
  🇪🇸 <a href="README.es.md">Español</a>
</p>


**Pepenium** is a **Java-based test automation framework** for **mobile applications (Android / iOS)** and **web**, built on top of **Appium** and **Selenium**.

It’s designed around a clear idea:  
👉 **start fast**, but **scale without breaking** when the project grows.

It supports **local execution**, **device farms** (AWS Device Farm / BrowserStack), and **CI/CD pipelines**, with configuration fully decoupled from code and an architecture designed for real-world projects.

---

## ✨ Key Features

- 📱 **Mobile-first**: Android and iOS as the primary focus
- 🌐 Integrated **Web (desktop)** support
- 🧱 Clean, reusable architecture (**core / toolkit / tests**)
- ☁️ Local or remote execution (**AWS Device Farm / BrowserStack**)
- ⚙️ Externalized configuration per provider and platform
- ♻️ Project-level extensibility without touching the core
- 🧪 CI/CD–ready by design

---

## ⚙️ Requirements & Setup

### General Requirements

- Java
- Maven
- Appium Server

---

### 📱 Local Mobile Execution

#### Android

- Appium Server installed and running
- Physical Android device or configured emulator

#### iOS

- Appium Server
- **XCUITest** driver (Appium’s automation driver for iOS)
- iOS environment properly set up (Xcode, simulator or physical device)

---

### 🌐 Local Web Execution (Desktop)

- Browser driver (e.g. ChromeDriver) placed in `src/test/resources`

---

## ☁️ BrowserStack & AWS Device Farm Execution

### BrowserStack

Configure `src/test/resources/browserstack.yml` with credentials, platforms, and devices.  
Once configured, tests can be executed directly from the IDE.

---

### AWS Device Farm

AWS Device Farm is focused on packaged executions and CI/CD workflows.

To package the tests:

`mvn clean package -P my-example-app-android -DskipTests`


Upload to AWS:
- Generated JAR
- `dependency-jars` folder

---

## 🧠 Architecture

### Core (`core/`)

Provider- and platform-specific configuration:
- `core/configs/aws/(android|ios)`
- `core/configs/browserstack/(android|ios|desktop)`

---

### Toolkit (`toolkit/`)

- `toolkit/utils`
- `toolkit/<project>`

---

## 🧬 Page Object Model (POM)

### Pages

- App IDs (Android `resource-id`, iOS `accessibility id`)
- Basic actions

### Flows

- Composition of actions across multiple pages

### Tests

- Call flows and validate results

---

## PURPOSE

Pepenium aims to make automation boring.  
And in testing, that’s a feature.
