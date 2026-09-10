package ai.testzombie.appium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Kontrolltest: kennt die erwarteten nativen und WebView-Locator-Änderungen je Level.
 */
class MutationLevelMatrixTest extends AppiumTestBase {

    @ParameterizedTest(name = "iOS Mutation Level {0}")
    @ValueSource(ints = {0, 1, 2, 3, 4})
    @DisplayName("Alle iOS-Mutationslevel mit level-spezifischen Locators")
    void runsEveryMutationLevel(int levelValue) throws Exception {
        try {
            startApp();
            MutationLevel level = MutationLevel.of(levelValue);
            HybridOnboardingPage app = new HybridOnboardingPage(driver, wait);

            System.out.printf("%n=== Starte iOS Mutation Level %d ===%n", levelValue);
            app.chooseMutationLevel(levelValue);
            app.enterPassword(level);
            app.enterPerson(level);
            app.chooseEnterprisePlan(level);
            app.completeWebSummary(level);
            System.out.printf("=== iOS Mutation Level %d erfolgreich ===%n", levelValue);
        } catch (Throwable error) {
            saveScreenshot("ios-mutation-level-" + levelValue + "-failed");
            throw error;
        }
    }
}
