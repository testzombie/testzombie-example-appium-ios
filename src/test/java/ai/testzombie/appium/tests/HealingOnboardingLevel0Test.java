package ai.testzombie.appium.tests;

import ai.testzombie.appium.testbase.BaseIosTest;
import ai.testzombie.appium.testbase.OnboardingScenario;
import org.junit.jupiter.api.Test;

public class HealingOnboardingLevel0Test extends BaseIosTest {

    @Test
    void onboardingLevel0() {
        try {
            OnboardingScenario.run(
                    driver,
                    wait,
                    0,
                    "Onboarding Level 0"
            );
        } catch (Throwable error) {
            saveScreenshot("ios-onboarding-level-0-failed");
            throw error;
        }
    }
}