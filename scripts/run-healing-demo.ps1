param(
    [string]$App = ".\TestZombieHybridIOS.app",
    [string]$AppiumServer = "http://127.0.0.1:4723",
    [string]$DeviceName = "iPhone 16"
)

$ErrorActionPreference = "Stop"
if (-not $env:TESTZOMBIE_API_KEY) { throw "Bitte TESTZOMBIE_API_KEY setzen." }
if (-not $env:TESTZOMBIE_EMAIL) { throw "Bitte TESTZOMBIE_EMAIL setzen." }

& mvn "-Dapp.ios=$App" "-Dappium.server=$AppiumServer" "-Ddevice.name=$DeviceName" "-Dtestzombie.healing=true" "-Dtest=BaselineHealingDemoTest" test
exit $LASTEXITCODE
