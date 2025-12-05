# Time Table App for WearOS
## Overview
This WearOS app serves as a convenient tool for students to access their course schedules, ensuring they stay organized and up-to-date with their classes.

## Features
- **WearOS Compatibility**: Seamlessly integrated with WearOS devices for on-the-go access.
- **Tile Support**: Now tiles are included giving you access to latest class info.

## Building Release APKs

This repository includes a GitHub Actions workflow that builds signed release APKs for both the phone and wear modules.

### Triggering the Build

The workflow can be triggered in two ways:
1. **Automatically**: Push a tag starting with `v` (e.g., `v1.0.0`)
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
2. **Manually**: Go to Actions → Build Signed Release APKs → Run workflow

### Required Secrets

To use the workflow, configure the following repository secrets in GitHub:
- `KEYSTORE_BASE64`: Base64-encoded Android keystore file
  ```bash
  base64 -i your-keystore.jks | pbcopy  # macOS
  base64 -w 0 your-keystore.jks         # Linux
  ```
- `KEYSTORE_PASSWORD`: Password for the keystore
- `KEY_ALIAS`: Key alias used in the keystore
- `KEY_PASSWORD`: Password for the key

The built APKs will be available as artifacts in the workflow run.

## Contributors
- [Vishnu Tejas](https://github.com/vishnutejase)

## License
This project is licensed under the [MIT License](/LICENSE.txt).
