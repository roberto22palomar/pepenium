# Contributing to Pepenium

First of all, thank you for taking the time to contribute to **Pepenium** 🙌  
Contributions of any kind are welcome: code, documentation, bug reports, or suggestions.

---

## 📌 Project Language

To keep the project consistent and accessible:

- **Issues and Pull Requests:** English
- **Documentation:** English (Spanish version available where applicable)
- **Code and comments:** English

Please stick to English for all new contributions.

---

## 🐛 Reporting Bugs

Before opening a bug report:

1. Check existing issues to avoid duplicates
2. Make sure you are using the latest version of the project

When opening a bug report, please use the **Bug report** issue template and include:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Relevant logs or stack traces
- Environment details (OS, Java version, provider, etc.)

---

## ✨ Requesting Features

Feature requests are welcome.  
Please use the **Feature request** issue template and clearly describe:
- The problem you are trying to solve
- The proposed solution
- Why it would be useful for the project

Well-defined feature requests are more likely to be accepted.

---

## 🧪 Running the Project Locally

Basic requirements:
- Java
- Maven
- Appium (for mobile testing)

Typical commands:

```bash
mvn clean test
mvn clean package
```

Some features may require specific Maven profiles (e.g. BrowserStack or AWS Device Farm).

---

## 🔀 Pull Requests

Before submitting a Pull Request:

- Ensure the project builds successfully
- Keep changes focused and scoped
- Update documentation if behavior changes
- Avoid breaking changes unless clearly justified

Pull Requests should:
- Reference an existing issue when possible
- Include a clear description of what was changed and why

---

## 🧱 Project Structure

High-level structure:

- `core/` – Shared configuration and driver setup
- `toolkit/` – Utilities and reusable components
- `tests/` – Project-specific tests and flows

Please respect the existing architecture and avoid adding project-specific logic to `core/` unless it benefits all users.

---

## 🔖 Versioning & Changelog

This project follows **Semantic Versioning**.

Notable changes should be documented in `CHANGELOG.md` under the **[Unreleased]** section.

---

## 🤝 Code of Conduct

Be respectful and constructive in discussions.  
This project aims to maintain a friendly and professional environment for everyone.

---

Thanks again for contributing to Pepenium 🚀
