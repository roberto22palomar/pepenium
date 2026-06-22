# Environment Examples

This directory keeps copyable environment templates out of the repository root.

Pick the smallest file that matches the thing you want to run:

| Goal | Start from |
| --- | --- |
| Configure several profiles in one readable file | [`pepenium.yml.example`](pepenium.yml.example) |
| Run the desktop web showcase locally | [`.env.web.example`](.env.web.example) |
| Run Android with a locally installed Appium server | [`.env.android.local.example`](.env.android.local.example) |
| Run Android with Dockerized Appium and a host emulator | [`.env.android.host-emulator.example`](.env.android.host-emulator.example) |
| Experiment with Dockerized Appium and Dockerized Android emulator | [`.env.android.docker-emulator.example`](.env.android.docker-emulator.example) |
| Add extra Selenium web capabilities | [`.env.web.capabilities.example`](.env.web.capabilities.example) |
| Add extra Appium mobile capabilities | [`.env.mobile.capabilities.example`](.env.mobile.capabilities.example) |

For the full variable reference, precedence rules and capability override syntax, see [ENVIRONMENT.md](../ENVIRONMENT.md).
