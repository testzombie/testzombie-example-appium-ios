package ai.testzombie.appium.tests;

import ai.testzombie.appium.testbase.BaseIosTest;
import ai.testzombie.appium.testbase.OnboardingScenario;
import org.junit.jupiter.api.Test;

/** Native SDK Discovery on, one stable Level-0 flow, like Selenium and Playwright. */
class DiscoveryLevel0Test extends BaseIosTest {
    @Override
    protected boolean discoveryEnabled() {
        return true;
    }

    @Test
    void discoverStableOnboardingAtLevelZero() {
        try {
            OnboardingScenario.run(driver, wait, 0, "Discovery Level 0");
        } catch (Throwable error) {
            saveScreenshot("ios-discovery-level-0-failed");
            throw error;
        }
    }
}
