package ai.testzombie.appium.tests;

import ai.testzombie.appium.testbase.BaseIosTest;
import ai.testzombie.appium.testbase.OnboardingScenario;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Self-Healing: five independent XCUITest sessions, always with Level-0 selectors. */
class HealingOnboardingTest extends BaseIosTest {
    @ParameterizedTest(name = "Self-Healing at iOS mutation level {0}")
    @ValueSource(ints = {0, 1, 2, 3, 4})
    void completeOnboardingOnEveryMutationLevel(int level) {
        try {
            OnboardingScenario.run(driver, wait, level, "Self-Healing Level " + level);
        } catch (Throwable error) {
            saveScreenshot("ios-healing-level-" + level + "-failed");
            throw error;
        }
    }
}
