# TestZombie Appium iOS Example

A small Appium iOS project that demonstrates **TestZombie Self-Healing**, **Discovery**, and optional **Persistent Healing** with `com.testzombie:testzombie-lib-java`.

The example intentionally uses normal Appium tests and Page Objects. The three main demonstrations use the original **Mutation Level 0** locators; there are no TestZombie-specific fallback locators in the test code. The demo app is **hybrid**: it starts in `NATIVE_APP` and finishes in a `WEBVIEW`.

## Quick start — 1, 2, 3

### 1. Install

Clone the project and make sure the following are available **on a Mac**:

- **Java 21** or newer and Maven
- Node.js and Appium with the **XCUITest** driver
- Xcode with a compatible iOS Simulator (or a properly configured physical iOS device)
- The bundled `TestZombieHybridIOS.app` in the project root
- WebKit/Web Inspector support for the app's WebView

Install Appium and the XCUITest driver if necessary:

```bash
npm install -g appium
appium driver install xcuitest
```

Start an iOS simulator in Xcode, then launch **Appium in a separate terminal**:

```bash
appium server
```

The example uses **TestZombie Java SDK 1.3.1** via Maven:

```xml
<dependency>
    <groupId>com.testzombie</groupId>
    <artifactId>testzombie-lib-java</artifactId>
    <version>1.3.1</version>
</dependency>
```

The default Appium server URL is `http://127.0.0.1:4723`, and the default simulator name is `iPhone 16e`. The supplied `.app` is a **simulator build**. Testing on a physical iPhone requires an appropriately built and signed device application (usually an `.ipa`) and additional Xcode/WebDriverAgent setup.

> **Platform note:** The iOS Simulator and Xcode/XCUITest test execution require macOS. You cannot run the bundled simulator app with a Linux-based Android emulator.

### 2. Configure TestZombie

Create an account at **https://testzombie.ai** and copy your **API key** and **account e-mail**. Choose one of these credential options.

#### Option 1 — Enter credentials in `pom.xml` (quickest local setup)

Open `pom.xml` and find the existing `defaults` profile near the bottom. Fill in its two empty properties:

```xml
<profiles>
    <profile>
        <id>defaults</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <testzombie.apikey>YOUR_API_KEY</testzombie.apikey>
            <testzombie.email>YOUR_EMAIL</testzombie.email>
        </properties>
    </profile>
</profiles>
```

The existing Maven Surefire plugin passes these values as Java system properties. `AppiumDriverFactory` reads them and calls `TestZombieDriver.setCredentials(...)` before creating a TestZombie iOS session.

With the simulator and Appium running, start the Self-Healing demo **without extra credential arguments**:

```bash
mvn test -Dtest=OnboardingFlowTest
```

> **Security:** Use this option for a quick local demo only. **Never commit or share a `pom.xml` containing real credentials.** Restore the properties to empty values before pushing. For CI/CD, prefer one of the alternatives below.

#### Option 2 — Pass credentials as Maven parameters

Leave the credential values in `pom.xml` empty and provide them when starting the test:

```bash
mvn test -Dtest=OnboardingFlowTest \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

#### Option 3 — Set environment variables

Leave the credential properties in `pom.xml` empty and set environment variables instead.

**macOS (Terminal)**

```bash
export TESTZOMBIE_API_KEY="YOUR_API_KEY"
export TESTZOMBIE_EMAIL="YOUR_EMAIL"
```

**PowerShell (for configuration only; iOS execution still requires macOS)**

```powershell
$env:TESTZOMBIE_API_KEY="YOUR_API_KEY"
$env:TESTZOMBIE_EMAIL="YOUR_EMAIL"
```

`AppiumDriverFactory` checks Java system properties first, then environment variables. These options all use the same `TestZombieDriver.setCredentials(...)` integration.

### 3. Run the Self-Healing demo

With your iOS simulator running and Appium started in the other terminal:

```bash
mvn test -Dtest=OnboardingFlowTest
```

The same hybrid onboarding flow runs through five mutation levels:

```text
OFF → LIGHT → REALISTIC → HARD → EXTREME
 0      1        2        3        4
```

JUnit executes a separate test/session for each level. At higher levels, original locators may become invalid. TestZombie attempts to identify the correct element so the Appium action can continue.

> **Why select a test class?** `mvn test` runs all discovered tests, including Discovery and the Code Update demo. Code Update uses `APPLY` by default and may modify your Java source files. Prefer the explicit demo commands shown here.

---

## Discovery — learn a working business flow

Self-Healing fixes broken locators. **Discovery has a different job:** it observes a successful test flow and collects useful context about what happened.

Run the dedicated Discovery example:

```bash
mvn test -Dtest=DiscoveryLevel0Test
```

This example executes **one stable onboarding flow at Mutation Level 0 / OFF**, just like the Playwright Discovery example. Nothing is intentionally broken. TestZombie observes the application's normal behavior.

### What happens during Discovery?

In simple terms:

```text
Existing Appium iOS test
        ↓
successful native + WebView user flow
        ↓
TestZombie Discovery
        ↓
pages + elements + interactions + context
        ↓
AI Test Design
        ↓
business test cases
        ↓
selected test cases
        ↓
automation code
```

Discovery can collect context from the successful journey, including visited screens/pages, relevant elements, interactions, and their sequence. This can support future AI-based test design and automation generation.

The dedicated test is in:

```text
src/test/java/ai/testzombie/appium/tests/DiscoveryLevel0Test.java
```

The important part is deliberately small:

```java
@Override
protected boolean discoveryEnabled() {
    return true;
}
```

Before the iOS Appium session is created, `BaseIosTest` passes this flag to `AppiumDriverFactory`, which calls:

```java
NativeDiscovery.configure(discoveryEnabled);
```

This helper configures **the TestZombie SDK's native Discovery**, rather than creating a separate local recorder. When the SDK exposes the relevant public setters, the equivalent configuration is:

```java
TestZombieDriver.setDiscoveryEnabled(true);
TestZombieDriver.setDiscoveryFileEnabled(true);
TestZombieDriver.setDiscoveryFilePath(
    "test-results/testzombie-discovery-{testRunId}.json"
);
```

The provided `NativeDiscovery` helper looks for those setters at runtime. **Local JSON output is requested, not guaranteed:** its availability depends on the SDK's file-output methods. When supported, the requested output location is:

```text
test-results/testzombie-discovery-<testRunId>.json
```

The test itself stays normal Appium/Page-Object code. No application-side JSON recorder is used.

### Why Mutation Level 0?

The main Healing demo intentionally changes locator-relevant UI properties. Discovery uses the stable baseline so the collected data describes a **valid business process**.

```text
Discovery     → understand the working flow
Self-Healing  → keep the flow running when locators change
```

---

## Code Update — run Level 0, heal Level 1 and update the source

Discovery learns a working flow. **Code Update** demonstrates the maintenance step after a UI change: TestZombie heals a broken locator and can write the verified replacement into the Java Page Object.

Run the dedicated demo:

```bash
mvn test -Dtest=CodeUpdateLevel0And1Test
```

Unlike the Playwright example, which starts on Level 1, this iOS example runs the same journey **twice in one Appium/TestZombie session**:

1. **Mutation Level 0 / OFF** — complete a successful baseline.
2. **Restart the iOS app**, keeping the same Appium/TestZombie driver session.
3. **Mutation Level 1 / LIGHT** — repeat the steps using the original Level-0 locators, allowing TestZombie to heal and persist working replacements.

### What should happen?

```text
Existing Appium iOS test
        ↓
successful Level-0 baseline
        ↓
terminate + activate app (same Appium session)
        ↓
Mutation Level 1 changes the UI / locators
        ↓
original locator no longer works
        ↓
TestZombie heals the element at runtime
        ↓
action succeeds with the healed locator
        ↓
Code Update writes the working locator back to the Page Object
```

The dedicated test is in:

```text
src/test/java/ai/testzombie/appium/tests/CodeUpdateLevel0And1Test.java
```

It selects `APPLY` by default and also supports `PREVIEW`:

```java
@Override
protected SourceUpdateMode sourceUpdateMode() {
    String value = System.getProperty("testzombie.codeupdate.mode", "apply").trim();
    return switch (value.toLowerCase(Locale.ROOT)) {
        case "apply" -> SourceUpdateMode.APPLY;
        case "preview" -> SourceUpdateMode.PREVIEW;
        default -> throw new IllegalArgumentException(
            "testzombie.codeupdate.mode must be apply or preview");
    };
}
```

`AppiumDriverFactory` configures the mode **before** creating the iOS session:

```java
TestZombieDriver.setSourceUpdateMode(sourceUpdateMode);
```

Preview the edits without changing files:

```bash
mvn test -Dtest=CodeUpdateLevel0And1Test -Dtestzombie.codeupdate.mode=preview
```

After an `APPLY` run, inspect the source changes:

```bash
git diff -- src/test/java/ai/testzombie/appium/pages
```

To restore the baseline Page Object locators for another demonstration:

```bash
git restore -- src/test/java/ai/testzombie/appium/pages
```

> **Warning:** `git restore` discards **all uncommitted changes** in those files. Review `git diff` before using it.

### Why keep the same driver session?

`BaseIosTest.resetDemoState()` switches to `NATIVE_APP`, calls `terminateApp(bundleId)` and then `activateApp(bundleId)`. **It does not quit and recreate the Appium driver.** This keeps the source-update sequence together, while restarting the app's UI for Level 1.

This assumes the restarted app returns to the mutation-level selection screen. If the app persists state across restarts, the app may need an additional reset operation.

```text
Discovery Level 0        → learn a valid business flow
Code Update Level 0 → 1  → establish baseline, heal changed UI, persist locators
```

---

## How TestZombie is integrated

The central integration is intentionally small.

Normal Appium iOS:

```java
IOSDriver driver = new IOSDriver(serverUrl, options);
```

With TestZombie:

```java
TestZombieDriver.setCredentials(apiKey, email);
TestZombieDriver.setSourceUpdateMode(sourceUpdateMode);
IOSDriver driver = TestZombieDriverMobile.createIOS(serverUrl, options);
```

The code above illustrates the expected factory call. The project itself locates a public `createIOS(...)` or `createIos(...)` method **at runtime** to accommodate the available TestZombie SDK factory spelling/signature.

All driver setup is centralized in:

```text
src/test/java/ai/testzombie/appium/support/AppiumDriverFactory.java
```

It uses `XCUITestOptions` with the default bundle ID `ai.testzombie.hybridios`, device name `iPhone 16e`, app path `./TestZombieHybridIOS.app`, and Appium URL `http://127.0.0.1:4723`.

Existing Page Objects and Selenium-style `By` locators remain familiar. The three feature tests use the Level-0 locators even after the app changes; **they do not switch to level-specific fallback locators**. The separate `MutationLevelMatrixTest` deliberately uses known selectors for each mutation level as a diagnostic control.

### Hybrid app: `NATIVE_APP` → `WEBVIEW`

The onboarding flow is:

```text
Select mutation level
        ↓
Enter and confirm password           (NATIVE_APP)
        ↓
Enter personal details               (NATIVE_APP)
        ↓
Choose Enterprise plan               (NATIVE_APP)
        ↓
Switch to WEBVIEW
        ↓
Confirm the summary                  (WEBVIEW)
        ↓
Verify success status
```

The WebView is accessed through XCUITest/WebKit. Unlike the Android example, this iOS project **does not need Android's `chromedriver_autodownload` Appium flag**. On a physical iPhone, confirm the necessary Web Inspector and device trust/developer settings are enabled.

---

## Optional: Persistent Healing modes

The dedicated `CodeUpdateLevel0And1Test` uses `APPLY` by default. The other feature demos use `OFF` for source updates.

| Mode | Behavior |
| --- | --- |
| `OFF` | Do not update Java source files. |
| `PREVIEW` | Report planned locator replacements without writing files. |
| `APPLY` | Write successfully healed locators back into the Java source. |

Choose the Code Update mode with the test-specific property:

```bash
# Preview only
mvn test -Dtest=CodeUpdateLevel0And1Test -Dtestzombie.codeupdate.mode=preview

# Apply verified updates (default)
mvn test -Dtest=CodeUpdateLevel0And1Test -Dtestzombie.codeupdate.mode=apply
```

The Java SDK also recognizes `TESTZOMBIE_SOURCE_UPDATE_MODE` when there is no explicit Java override. **In this example**, `AppiumDriverFactory` explicitly calls `TestZombieDriver.setSourceUpdateMode(...)`, so the per-test setting takes precedence.

---

## Useful commands

| Command | Purpose |
| --- | --- |
| `appium server` | Start the Appium server on macOS |
| `appium driver list --installed` | Check that `xcuitest` is installed |
| `mvn test -Dtest=OnboardingFlowTest` | Self-Healing on levels 0–4 |
| `mvn test -Dtest=DiscoveryLevel0Test` | Native Discovery on stable Level 0 |
| `mvn test -Dtest=CodeUpdateLevel0And1Test` | Level 0 → Level 1, source updates in `APPLY` mode |
| `mvn test -Dtest=CodeUpdateLevel0And1Test -Dtestzombie.codeupdate.mode=preview` | Preview source edits |
| `mvn test -Dtest=MutationLevelMatrixTest` | Diagnostic iOS app control with known level-specific locators and a regular `IOSDriver` |
| `mvn test -Dtest=OnboardingFlowTest -Ddevice.name="iPhone 16e"` | Select a simulator name |
| `mvn test -Dtest=OnboardingFlowTest -Dapp.ios=/path/to/app.app` | Use a different iOS app bundle |
| `mvn test` | Run **all** discovered tests, including Code Update (`APPLY`) |

Shell scripts are also included for the individual demos:

```bash
./scripts/run-healing-demo.sh ./TestZombieHybridIOS.app "iPhone 16e"
./scripts/run-discovery.sh ./TestZombieHybridIOS.app "iPhone 16e"
./scripts/run-codeupdate.sh ./TestZombieHybridIOS.app "iPhone 16e"
./scripts/run-matrix.sh ./TestZombieHybridIOS.app "iPhone 16e"
```

Additional configuration can be passed as Maven system properties or environment variables:

| Setting | Java system property | Environment variable | Default |
| --- | --- | --- | --- |
| Appium server | `appium.server` | `APPIUM_SERVER` | `http://127.0.0.1:4723` |
| Device name | `device.name` | `DEVICE_NAME` | `iPhone 16e` |
| iOS app path | `app.ios` | `APP_IOS` | `./TestZombieHybridIOS.app` |
| Bundle ID | `app.bundleId` | `APP_BUNDLE_ID` | `ai.testzombie.hybridios` |
| Device UDID | `device.udid` | `DEVICE_UDID` | Optional |
| Platform version | `platform.version` | `PLATFORM_VERSION` | Optional |
| API key | `testzombie.apikey` | `TESTZOMBIE_API_KEY` | No value |
| Account e-mail | `testzombie.email` | `TESTZOMBIE_EMAIL` | No value |
| Discovery file path | `testzombie.discovery.file` | `TESTZOMBIE_DISCOVERY_FILE` | `test-results/testzombie-discovery-{testRunId}.json` |

---

## Project structure

```text
testzombie-example-appium-ios/
├── pom.xml
├── README.md
├── TestZombieHybridIOS.app/
├── scripts/
│   ├── build-ios-simulator.sh
│   ├── run-healing-demo.sh
│   ├── run-discovery.sh
│   ├── run-codeupdate.sh
│   └── run-matrix.sh
└── src/test/java/ai/testzombie/appium/
    ├── pages/
    │   └── HybridOnboardingPage.java
    ├── support/
    │   ├── AppiumDriverFactory.java
    │   ├── MutationLevel.java
    │   ├── NativeDiscovery.java
    │   └── ScrollHelper.java
    ├── testbase/
    │   ├── BaseIosTest.java
    │   └── OnboardingScenario.java
    └── tests/
        ├── CodeUpdateLevel0And1Test.java
        ├── DiscoveryLevel0Test.java
        ├── MutationLevelMatrixTest.java
        └── OnboardingFlowTest.java
```

`BaseIosTest` manages the Appium session lifecycle, configures SDK options per scenario, and saves failure screenshots under `target/screenshots`. `OnboardingScenario` contains the shared native-to-WebView workflow. `HybridOnboardingPage` implements the screen interactions; `ScrollHelper` supports native scrolling.

### Optional control: `MutationLevelMatrixTest`

The matrix test is **not another Self-Healing demo**. It executes mutation levels 0–4 using known, level-specific selectors and a regular `IOSDriver` without TestZombie healing. Use it to distinguish demo-app behavior from a locator-healing problem:

```bash
mvn test -Dtest=MutationLevelMatrixTest
```

---

## iOS simulator and WebView troubleshooting

**XCUITest / WebDriverAgent does not start**

Check Xcode, the selected simulator or device, and the installed XCUITest driver:

```bash
appium driver list --installed
```

Make sure `DEVICE_NAME`, `PLATFORM_VERSION`, and (if provided) `DEVICE_UDID` match your target. For physical devices, also check signing, provisioning, trust, and WebDriverAgent setup.

**No `WEBVIEW` context is available**

Make sure the demo app reached its WebView screen and has finished loading. Check Appium logs for WebKit/Web Inspector connection errors.

**App bundle not found**

Check that `TestZombieHybridIOS.app` exists in the working directory. To use another app, pass:

```bash
mvn test -Dtest=OnboardingFlowTest -Dapp.ios=/absolute/path/to/TestZombieHybridIOS.app
```

**Missing TestZombie credentials**

Fill the `defaults` profile in `pom.xml`, pass `-Dtestzombie.apikey` and `-Dtestzombie.email`, or export `TESTZOMBIE_API_KEY` and `TESTZOMBIE_EMAIL`.

**Unable to rebuild the sample app**

The bundled `TestZombieHybridIOS.app` is already compiled for the simulator. The source Xcode project (`ios-app/TestZombieHybridIOS.xcodeproj`) is **not included** in this Java example. The optional `scripts/build-ios-simulator.sh` therefore cannot rebuild the app without that separate Xcode project.

---

## Security

Never commit API keys, account credentials, or sensitive test artifacts. Placing credentials in the `pom.xml` `defaults` profile is convenient for an initial local run, but **remove them before committing or sharing** the file. In shared repositories and CI/CD, inject secrets securely at runtime.

## More validation

Use the five mutation levels to compare a stable baseline with increasingly changed UI structures. The three feature demos exercise TestZombie's SDK functions; the matrix test provides an independent control for the iOS app.

This README describes the intended workflow and the checked-in Java code. A full live run of iOS XCUITest, WebView automation, Discovery output, and source updates still requires a suitable Mac/Xcode/Appium environment and TestZombie access.
