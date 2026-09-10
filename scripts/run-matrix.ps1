param(
    [string]$App = ".\TestZombieHybridIOS.app",
    [string]$AppiumServer = "http://127.0.0.1:4723",
    [string]$DeviceName = "iPhone 16"
)

$ErrorActionPreference = "Stop"
& mvn "-Dapp.ios=$App" "-Dappium.server=$AppiumServer" "-Ddevice.name=$DeviceName" "-Dtestzombie.healing=false" "-Dtest=MutationLevelMatrixTest" test
exit $LASTEXITCODE
