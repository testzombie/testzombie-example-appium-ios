# TestZombie + Appium iOS – Quickstart

Dieses Beispielprojekt zeigt die Verwendung von **TestZombie** mit **Appium** für eine iOS-Hybrid-App.

Die App enthält neben nativen iOS-Elementen auch **Web-Inhalte**. Während der Tests wird deshalb vom nativen App-Kontext (`NATIVE_APP`) in einen `WEBVIEW`-Kontext gewechselt.

Für iOS wird der Appium **XCUITest Driver** verwendet.

---

## Voraussetzungen

Für die Ausführung werden benötigt:

- macOS
- Java 17+
- Maven
- Node.js und Appium
- Appium XCUITest Driver
- Xcode
- iOS Simulator oder passend konfiguriertes physisches iOS-Gerät
- TestZombie API Key und E-Mail-Adresse

Das Beispiel-App-Bundle `TestZombieHybridIOS.app` liegt standardmäßig im Projektverzeichnis.

---

## 1. Appium starten

Die Demo-App ist eine **Hybrid-App** und enthält Web-Inhalte.

Im Test wird deshalb unter anderem in einen `WEBVIEW`-Kontext gewechselt. Unter iOS übernimmt Appium den Zugriff über den XCUITest-/WebKit-Stack.

Appium und den XCUITest Driver installieren:

Anschließend Appium normal starten:

```bash
appium
```
---

## 2. TestZombie Credentials

Für die Verwendung von TestZombie werden ein **API Key** und die zugehörige **E-Mail-Adresse** benötigt.

Die Credentials können auf verschiedene Arten gesetzt werden.

### Variante 1 – Credentials direkt im Java-Code setzen

Für einen einfachen lokalen Test oder zum schnellen Ausprobieren können die Credentials direkt bei der Initialisierung gesetzt werden.

Zum Beispiel in `AppiumTestBase`:

```java
TestZombieDriver.setCredentials(
        "YOUR_API_KEY",
        "YOUR_EMAIL"
);
```

Beispiel im Zusammenhang mit der Driver-Erzeugung:

```java
@BeforeEach
void setUp() {
    TestZombieDriver.setCredentials(
            "YOUR_API_KEY",
            "YOUR_EMAIL"
    );

    // Appium / TestZombie Driver anschließend wie gewohnt erzeugen
}
```

Danach können die Tests ohne zusätzliche Credential-Parameter gestartet werden:

```bash
mvn test
```

> Diese Variante eignet sich vor allem zum schnellen lokalen Ausprobieren. Echte API Keys sollten nicht in Git eingecheckt werden.

### Variante 2 – Credentials in der `pom.xml` setzen

Die Credentials können auch im bereits vorhandenen `defaults`-Profil der `pom.xml` hinterlegt werden.

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

Das Maven Surefire Plugin gibt diese Werte an die Tests weiter.

Im Java-Code können sie anschließend über die System Properties gelesen werden:

```java
String apiKey = System.getProperty("testzombie.apikey");
String email = System.getProperty("testzombie.email");

TestZombieDriver.setCredentials(apiKey, email);
```

Danach reicht zum Starten der Tests:

```bash
mvn test
```

> Auch hier gilt: Eine `pom.xml` mit echten Zugangsdaten sollte nicht in ein öffentliches oder gemeinsam genutztes Git-Repository eingecheckt werden.

### Variante 3 – Credentials über Maven übergeben

Die Credentials können direkt beim Maven-Aufruf als System Properties gesetzt werden:

```bash
mvn test \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

PowerShell:

```powershell
mvn test `
  -Dtestzombie.apikey=YOUR_API_KEY `
  -Dtestzombie.email=YOUR_EMAIL
```

Die Werte werden in Java über

```java
System.getProperty("testzombie.apikey")
System.getProperty("testzombie.email")
```

gelesen.

### Variante 4 – Credentials über Environment Variables

Linux / macOS:

```bash
export TESTZOMBIE_API_KEY="YOUR_API_KEY"
export TESTZOMBIE_EMAIL="YOUR_EMAIL"

mvn test
```

PowerShell:

```powershell
$env:TESTZOMBIE_API_KEY="YOUR_API_KEY"
$env:TESTZOMBIE_EMAIL="YOUR_EMAIL"

mvn test
```

Die Environment Variables werden in Java über

```java
System.getenv("TESTZOMBIE_API_KEY")
System.getenv("TESTZOMBIE_EMAIL")
```

gelesen.

### Credential-Auflösung im Beispielprojekt

Wenn die Credentials nicht direkt im Code gesetzt werden, verwendet `AppiumTestBase` die vorhandene Auflösung über System Properties und Environment Variables:

```java
String apiKey = firstNonBlank(
        System.getProperty("testzombie.apikey"),
        System.getenv("TESTZOMBIE_API_KEY")
);

String email = firstNonBlank(
        System.getProperty("testzombie.email"),
        System.getenv("TESTZOMBIE_EMAIL")
);

TestZombieDriver.setCredentials(apiKey, email);
```

Dabei haben die System Properties Vorrang vor den Environment Variables:

```text
-Dtestzombie.apikey
-Dtestzombie.email
        ↓
TESTZOMBIE_API_KEY
TESTZOMBIE_EMAIL
```

Für lokale Entwicklung und CI/CD sind Maven-Properties oder Environment Variables meist die bessere Wahl, damit keine Secrets im Quellcode oder in eingecheckten Konfigurationsdateien liegen.

---

## 3. Tests starten

Appium zuerst in einem Terminal starten:

```bash
appium server
```

Anschließend können die Tests – abhängig von der gewählten Credential-Variante – in einem zweiten Terminal gestartet werden.

### Credentials direkt im Java-Code oder in der `pom.xml`

Wenn die Credentials bereits direkt im Java-Code oder im `defaults`-Profil der `pom.xml` gesetzt wurden:

```bash
mvn test
```

### Credentials über Maven

```bash
mvn test \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

### Credentials über Environment Variables

Credentials einmal setzen:

```bash
export TESTZOMBIE_API_KEY="YOUR_API_KEY"
export TESTZOMBIE_EMAIL="YOUR_EMAIL"
```

Tests starten:

```bash
mvn test
```

---

## 4. iOS-App konfigurieren

Standardmäßig verwendet das Projekt:

```text
./TestZombieHybridIOS.app
```

Ein anderes `.app`-Bundle kann über eine System Property angegeben werden:

```bash
mvn test \
  -Dapp.ios=/path/to/TestZombieHybridIOS.app \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

Alternativ kann der App-Pfad über eine Environment Variable gesetzt werden:

Linux / macOS:

```bash
export APP_IOS="/path/to/TestZombieHybridIOS.app"
```

PowerShell:

```powershell
$env:APP_IOS="/path/to/TestZombieHybridIOS.app"
```

> Im Repository ist bereits `TestZombieHybridIOS.app` enthalten. Das Skript `scripts/build-ios-simulator.sh` erwartet zusätzlich ein Xcode-Projekt unter `ios-app/TestZombieHybridIOS.xcodeproj`. Dieses Quellprojekt ist im vorliegenden Projektstand nicht enthalten, deshalb kann für den Quickstart direkt das vorhandene `.app`-Bundle verwendet werden.

---

## 5. Weitere Konfiguration

Die wichtigsten Einstellungen können entweder über System Properties oder Environment Variables überschrieben werden:

| Einstellung | System Property | Environment Variable | Standard |
|---|---|---|---|
| Appium Server | `-Dappium.server=...` | `APPIUM_SERVER` | `http://127.0.0.1:4723` |
| Device Name | `-Ddevice.name=...` | `DEVICE_NAME` | `iPhone 16e` |
| iOS App | `-Dapp.ios=...` | `APP_IOS` | `./TestZombieHybridIOS.app` |
| Bundle ID | `-Dapp.bundleId=...` | `APP_BUNDLE_ID` | `ai.testzombie.hybridios` |
| Device UDID | `-Ddevice.udid=...` | `DEVICE_UDID` | – |
| Platform Version | `-Dplatform.version=...` | `PLATFORM_VERSION` | – |
| TestZombie Healing | `-Dtestzombie.healing=...` | `TESTZOMBIE_HEALING` | `true` |
| TestZombie API Key | `-Dtestzombie.apikey=...` | `TESTZOMBIE_API_KEY` | – |
| TestZombie E-Mail | `-Dtestzombie.email=...` | `TESTZOMBIE_EMAIL` | – |

Beispiel:

```bash
mvn test \
  -Ddevice.name="iPhone 16" \
  -Dplatform.version="18.0" \
  -Dapp.ios=./TestZombieHybridIOS.app \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

---

## 6. Einzelne Tests ausführen

Nur die Healing-Demo:

```bash
mvn -Dtest=BaselineHealingDemoTest test
```

Nur die Mutation-Level-Matrix:

```bash
mvn \
  -Dtestzombie.healing=false \
  -Dtest=MutationLevelMatrixTest \
  test
```

Wenn die Credentials nicht bereits direkt im Java-Code, in der `pom.xml` oder als Environment Variables gesetzt wurden, müssen sie für die Healing-Demo beim Maven-Aufruf zusätzlich angegeben werden.

Beispiel:

```bash
mvn \
  -Dtest=BaselineHealingDemoTest \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL \
  test
```

Bei `-Dtestzombie.healing=false` wird direkt ein normaler `IOSDriver` erzeugt. Dafür sind keine TestZombie-Credentials erforderlich.

Alternativ stehen die mitgelieferten Skripte zur Verfügung:

```bash
./scripts/run-healing-demo.sh ./TestZombieHybridIOS.app "iPhone 16"
./scripts/run-matrix.sh ./TestZombieHybridIOS.app "iPhone 16"
```

---

## 7. Was zeigt die Demo?

### `BaselineHealingDemoTest`

Der Test verwendet für alle Mutation Levels die Baseline-Locators aus Level 0.

Der eigentliche Testablauf bleibt unverändert, während TestZombie die Healing-Funktion übernimmt.

### `MutationLevelMatrixTest`

Der Kontrolltest enthält die erwarteten Locators für die verschiedenen Mutation Levels.

Damit kann unabhängig von der Healing-Demo geprüft werden, ob die jeweiligen Varianten der Demo-App grundsätzlich bedienbar sind.

Der Ablauf ist:

```text
Mutation Level auswählen
        ↓
Passwortdaten erfassen
        ↓
Personendaten erfassen
        ↓
Enterprise-Plan auswählen
        ↓
von NATIVE_APP in WEBVIEW wechseln
        ↓
Bestätigung ausführen
        ↓
Erfolgsstatus prüfen
```

Fehlerscreenshots werden unter `target/screenshots` gespeichert. Surefire-Berichte liegen unter `target/surefire-reports`.

---

## 8. Projektstruktur

```text
testzombie-example-appium-ios/
├── TestZombieHybridIOS.app/
├── pom.xml
├── README.md
├── scripts/
│   ├── build-ios-simulator.sh
│   ├── run-healing-demo.ps1
│   ├── run-healing-demo.sh
│   ├── run-matrix.ps1
│   └── run-matrix.sh
└── src/
    └── test/
        └── java/
            └── ai/
                └── testzombie/
                    └── appium/
                        ├── AppiumTestBase.java
                        ├── BaselineHealingDemoTest.java
                        ├── HybridOnboardingPage.java
                        ├── MutationLevel.java
                        ├── MutationLevelMatrixTest.java
                        └── ScrollHelper.java
```

### `AppiumTestBase`

Enthält:

- Appium-Server-Konfiguration
- Device- und Simulator-Konfiguration
- iOS-App- und Bundle-ID-Konfiguration
- TestZombie Credential-Auflösung
- optionale TestZombie-Healing-Aktivierung
- iOS Driver-Erzeugung über XCUITest
- Screenshot-Erzeugung

### `HybridOnboardingPage`

Enthält die vorhandenen nativen iOS- und WebView-Interaktionen.

### `MutationLevel`

Enthält die Locator-Konfiguration für die Mutation Levels.

### Hinweis zur iOS-Factory

Für Android stellt die verwendete TestZombie-Library eine direkte Android-Factory bereit.

Im iOS-Beispiel sucht `AppiumTestBase` deshalb zur Laufzeit nach einer öffentlichen `createIOS(...)`- oder `createIos(...)`-Factory in `TestZombieDriverMobile`.

Falls die verwendete TestZombie-Version eine andere iOS-Factory bereitstellt, muss nur dieser Integrationspunkt an die konkrete API-Version angepasst werden.

---

## 9. Troubleshooting

### `Missing TestZombie credentials`

Es wurden weder System Properties noch Environment Variables gefunden.

Prüfen, ob die Credentials über eine der beschriebenen Varianten gesetzt wurden:

1. direkt im Java-Code
2. im `defaults`-Profil der `pom.xml`
3. über Maven mit `-Dtestzombie.apikey` und `-Dtestzombie.email`
4. über `TESTZOMBIE_API_KEY` und `TESTZOMBIE_EMAIL`

Beispiel über Maven:

```bash
mvn test \
  -Dtestzombie.apikey=YOUR_API_KEY \
  -Dtestzombie.email=YOUR_EMAIL
```

### `iOS-App nicht gefunden`

Prüfen, ob `TestZombieHybridIOS.app` im Projektroot liegt.

Alternativ einen eigenen Pfad setzen:

```bash
-Dapp.ios=/path/to/TestZombieHybridIOS.app
```

### XCUITest / WebDriverAgent Fehler

Wenn keine iOS-Session aufgebaut werden kann:

1. prüfen, ob Xcode und der gewünschte Simulator installiert sind
2. prüfen, ob der Appium XCUITest Driver installiert ist
3. prüfen, ob `DEVICE_NAME`, `PLATFORM_VERSION` und optional `DEVICE_UDID` zum Zielgerät passen
4. Appium-Log auf Fehler beim Start von WebDriverAgent prüfen

Installierte Appium Driver anzeigen:

```bash
appium driver list --installed
```

### WebView wird nicht gefunden

Wenn beim Wechsel in den `WEBVIEW` kein Web-Kontext verfügbar ist:

1. prüfen, ob die Demo-App korrekt gestartet wurde
2. prüfen, ob die WebView innerhalb der App vollständig geladen wurde
3. Appium-Log auf WebKit-/WebView-Verbindungsfehler prüfen

---

## Quickstart

```text
1. Credentials setzen
   - direkt im Java-Code
   - oder in der pom.xml
   - alternativ über Maven / Environment Variables
2. Appium normal starten
3. mvn test ausführen
```

Einfachste Variante für einen lokalen Test:

```java
TestZombieDriver.setCredentials(
        "YOUR_API_KEY",
        "YOUR_EMAIL"
);
```

Appium starten:

```bash
appium server
```

In einem zweiten Terminal:

```bash
mvn test
```

Die Tests bleiben normale Appium-Tests. TestZombie wird zentral bei der Driver-Erzeugung in `AppiumTestBase` integriert.
