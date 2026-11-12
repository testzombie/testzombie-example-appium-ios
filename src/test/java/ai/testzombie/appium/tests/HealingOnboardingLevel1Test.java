package ai.testzombie.appium.tests;

import ai.testzombie.appium.testbase.BaseIosTest;
import ai.testzombie.appium.testbase.OnboardingScenario;
import org.junit.jupiter.api.Test;

public class HealingOnboardingLevel1Test extends BaseIosTest {

    @Test
    void onboardingLevel1() {
        try {
            OnboardingScenario.run(
                    driver,
                    wait,
                    1,
                    "Onboarding Level 1"
            );
        } catch (Throwable error) {
            saveScreenshot("ios-onboarding-level-1-failed");
            throw error;
        }
    }
}