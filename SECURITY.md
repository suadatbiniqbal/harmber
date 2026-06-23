# Security Policy

## Supported Versions

The following versions of Harmber are currently receiving security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.1.x   | :white_check_mark: |
| 1.0.x   | :x:                |
| < 1.0   | :x:                |

Only the latest stable release in the `1.1.x` series is actively maintained. Users on older versions are strongly encouraged to upgrade to the latest release.

## Reporting a Vulnerability

If you discover a security vulnerability in Harmber, please **do not** open a public GitHub Issue. Instead, report it responsibly using one of the methods below.

### How to Report

- **GitHub Private Advisory:** Go to the [Security tab](https://github.com/suadatbiniqbal/harmber/security/advisories/new) of this repository and submit a private vulnerability report.
- **Email:** You may also reach the maintainer directly at the email address listed on the [GitHub profile](https://github.com/suadatbiniqbal).

Please include the following in your report:

- A clear description of the vulnerability
- Steps to reproduce the issue
- The version of Harmber affected
- Any potential impact or proof-of-concept (if available)

### What to Expect

- **Acknowledgement:** You will receive an acknowledgement within **48–72 hours** of submitting your report.
- **Updates:** You can expect status updates every **5–7 days** while the issue is being investigated.
- **Resolution:** If the vulnerability is confirmed, a fix will be targeted for the next patch release (`1.1.x`). You will be credited in the release notes unless you prefer to remain anonymous.
- **Declined Reports:** If the report is determined not to be a security issue, we will explain why and may suggest opening a regular GitHub Issue instead.

## Scope

Security issues we are most interested in include:

- Unauthorized access to user data (e.g., Spotify tokens, playback history)
- Insecure storage of credentials or API keys
- Vulnerabilities in network communication (e.g., unencrypted requests)
- WebView or JavaScript injection issues
- Bypass of any authentication or authorization logic

## Out of Scope

- Bugs that do not have a security impact (please open a regular Issue)
- Issues in third-party libraries used by Harmber (report those to the respective projects)
- Theoretical vulnerabilities without a practical exploit

## Thank You

We appreciate the efforts of security researchers and users who help keep Harmber safe. Responsible disclosure makes the app better for everyone.
