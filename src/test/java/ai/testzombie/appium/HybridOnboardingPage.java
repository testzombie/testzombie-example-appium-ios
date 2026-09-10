package ai.testzombie.appium;

import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

final class HybridOnboardingPage {
    private static final int MAX_SCROLLS = 7;

    private final IOSDriver driver;
    private final WebDriverWait wait;

    HybridOnboardingPage(IOSDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    void chooseMutationLevel(int level) {
        clickNativeWithScroll("mutation-level-" + level);
        clickNativeWithScroll("mutation-start");
    }

    void enterPassword(MutationLevel level) {
        typeNative(level.password(), "Test1234");
        typeNative(level.passwordConfirm(), "Test1234");
        hideKeyboardSafely();
        clickNativeWithScroll(level.passwordNext());
        System.out.println("Clicked " + level.passwordNext());
    }

    void enterPerson(MutationLevel level) {
        typeNative(level.firstName(), "Chris");
        typeNative(level.lastName(), "Tester");
        typeNative(level.email(), "chris@example.com");
        hideKeyboardSafely();
        clickNativeWithScroll(level.personNext());
    }

    void chooseEnterprisePlan(MutationLevel level) {
        if (level.usesPlanDropdown()) {
            clickNativeWithScroll(level.planSelector());
        }
        clickNativeWithScroll(level.enterprisePlan());
        clickNativeWithScroll(level.planNext());
    }

    void completeWebSummary(MutationLevel level) {
        switchToWebView();
        wait.until(ExpectedConditions.elementToBeClickable(By.id(level.termsId()))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id(level.finishId()))).click();
        String status = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("status"))).getText();
        if (!status.toLowerCase().contains("erfolgreich")) {
            throw new AssertionError("Unerwarteter WebView-Status: " + status);
        }
    }

    private void switchToWebView() {
        WebDriverWait contextWait = new WebDriverWait(driver, Duration.ofSeconds(35));
        contextWait.until(ignored -> driver.getContextHandles().stream()
                .anyMatch(context -> context.startsWith("WEBVIEW")));

        Set<String> contexts = driver.getContextHandles();
        String webView = contexts.stream()
                .filter(context -> context.startsWith("WEBVIEW"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Kein WEBVIEW-Kontext gefunden: " + contexts));
        driver.context(webView);
    }

    private void typeNative(String accessibilityId, String value) {
        WebElement element = nativeElementWithScroll(accessibilityId);
        element.click();
        try {
            element.clear();
        } catch (RuntimeException ignored) {
            // Bei frischer Installation sind die Demo-Felder leer; clear() ist auf iOS nicht immer nötig.
        }
        element.sendKeys(value);
    }

    private void clickNativeWithScroll(String accessibilityId) {
        WebElement element = nativeElementWithScroll(accessibilityId);
        System.out.println(element);
        element.click();
    }


    private WebElement nativeElementWithScroll(String accessibilityId) {
        By locator = By.id(accessibilityId);

        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        for (int attempt = 0; attempt <= MAX_SCROLLS; attempt++) {
            try {
                return shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            } catch (TimeoutException ignored) {
                if (attempt < MAX_SCROLLS) {
                    ScrollHelper.scrollDown(driver);
                }
            }
        }
        throw new NoSuchElementException("iOS-Element nicht sichtbar gefunden: " + accessibilityId);
    }

    private void hideKeyboardSafely() {
        try {
            driver.hideKeyboard();
        } catch (RuntimeException ignored) {
            // Keine Tastatur offen oder XCUITest konnte sie nicht separat schließen.
        }
    }
}
