package ai.testzombie.appium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Healing-Demo: verwendet bei jedem Lauf ausschließlich die Locators aus Level 0.
 * Ohne Self-Healing sollte Level 0 erfolgreich sein und Level 1-4 an den Mutationen scheitern.
 * Mit TestZombie bleibt der Test unverändert und soll über alle Level laufen.
 */
class BaselineHealingDemoTest extends AppiumTestBase {

    @ParameterizedTest(name = "Baseline-Locators gegen iOS Mutation Level {0}")
    @ValueSource(ints = {0, 1, 2, 3, 4})
    @DisplayName("Unveränderter iOS-Baseline-Test für die Healing-Demonstration")
    void baselineLocatorsAgainstEveryLevel(int selectedLevel) throws Exception {
        try {
            startApp();
            MutationLevel baseline = MutationLevel.of(0);
            HybridOnboardingPage app = new HybridOnboardingPage(driver, wait);

            System.out.printf("%n=== iOS Baseline-Test gegen Level %d ===%n", selectedLevel);
            app.chooseMutationLevel(selectedLevel);
            app.enterPassword(baseline);
            app.enterPerson(baseline);
            app.chooseEnterprisePlan(baseline);
            app.completeWebSummary(baseline);
            System.out.println("fertig");
        } catch (Throwable error) {
            saveScreenshot("ios-baseline-against-level-" + selectedLevel + "-failed");
            throw error;
        }
    }
}
