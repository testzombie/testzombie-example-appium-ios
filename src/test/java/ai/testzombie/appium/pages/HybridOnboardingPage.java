package ai.testzombie.appium.pages;

import ai.testzombie.appium.support.MutationLevel;
import ai.testzombie.appium.support.ScrollHelper;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

/**
 * iOS hybrid Page Object.
 *
 * All three feature demos use the literal Level-0 selectors below, including
 * when the running iOS app is at mutation levels 1–4. TestZombie is responsible
 * for healing them and, when APPLY is on, updating these Java declarations.
 */
public final class HybridOnboardingPage {
    private static final int MAX_SCROLLS = 7;

    private static final By MUTATION_START = By.id("mutation-start");
    private static final By PASSWORD = By.id("password-input");
    private static final By PASSWORD_CONFIRM = By.id("password-confirm-input");
    private static final By PASSWORD_NEXT = By.id("password-next");
    private static final By FIRST_NAME = By.id("first-name-input");
    private static final By LAST_NAME = By.id("last-name-input");
    private static final By EMAIL = By.id("email-input");
    private static final By PERSON_NEXT = By.id("person-next");
    private static final By ENTERPRISE_PLAN = By.id("plan-enterprise");
    private static final By PLAN_SELECTOR = By.id("plan-selector");
    private static final By PLAN_NEXT = By.id("plan-next");
    private static final By TERMS = By.id("terms");
    private static final By FINISH = By.id("finishButton");
    private static final By STATUS = By.id("status");

    private final IOSDriver driver;
    private final WebDriverWait wait;

    public HybridOnboardingPage(IOSDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public HybridOnboardingPage chooseMutationLevel(int level) {
        if (level < 0 || level > 4) {
            throw new IllegalArgumentException("Mutation Level muss zwischen 0 und 4 liegen: " + level);
        }
        driver.context("NATIVE_APP");
        clickNative(By.id("mutation-level-" + level));
        clickNative(MUTATION_START);
        return this;
    }

    public HybridOnboardingPage enterPassword() {
        typeNative(PASSWORD, "Test1234");
        typeNative(PASSWORD_CONFIRM, "Test1234");
        hideKeyboardSafely();
        clickNative(PASSWORD_NEXT);
        return this;
    }

    public HybridOnboardingPage enterPerson() {
        typeNative(FIRST_NAME, "TestZombieAI");
        typeNative(LAST_NAME, "Tester");
        typeNative(EMAIL, "demo@example.com");
        hideKeyboardSafely();
        clickNative(PERSON_NEXT);
        return this;
    }

    public HybridOnboardingPage chooseEnterprisePlan(int mutationLevel) {
        // iOS level 4 needs the additional dropdown click, just like Android.
        if (mutationLevel == 4) clickNative(PLAN_SELECTOR);
        clickNative(ENTERPRISE_PLAN);
        clickNative(PLAN_NEXT);
        return this;
    }

    public String completeWebSummary() {
        switchToWebView();
        wait.until(ExpectedConditions.elementToBeClickable(TERMS)).click();
        wait.until(ExpectedConditions.elementToBeClickable(FINISH)).click();
        return wait.until(ExpectedConditions.visibilityOfElementLocated(STATUS)).getText();
    }

    /** Diagnostic iOS control only; known selectors intentionally bypass locator healing. */
    public String completeWithKnownLocators(MutationLevel level) {
        typeNative(By.id(level.password()), "Test1234");
        typeNative(By.id(level.passwordConfirm()), "Test1234");
        hideKeyboardSafely();
        clickNative(By.id(level.passwordNext()));

        typeNative(By.id(level.firstName()), "TestZombieAI");
        typeNative(By.id(level.lastName()), "Tester");
        typeNative(By.id(level.email()), "demo@example.com");
        hideKeyboardSafely();
        clickNative(By.id(level.personNext()));

        if (level.usesPlanDropdown()) clickNative(By.id(level.planSelector()));
        clickNative(By.id(level.enterprisePlan()));
        clickNative(By.id(level.planNext()));

        switchToWebView();
        wait.until(ExpectedConditions.elementToBeClickable(By.id(level.termsId()))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id(level.finishId()))).click();
        return wait.until(ExpectedConditions.visibilityOfElementLocated(STATUS)).getText();
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

    private void typeNative(By locator, String text) {
        WebElement element = nativeElementWithScroll(locator);
        element.click();
        try {
            element.clear();
        } catch (RuntimeException ignored) {
            // XCTest can reject clear() for empty native fields; the fresh flow is empty.
        }
        element.sendKeys(text);
    }

    private void clickNative(By locator) {
        nativeElementWithScroll(locator).click();
    }

    private WebElement nativeElementWithScroll(By locator) {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        for (int attempt = 0; attempt <= MAX_SCROLLS; attempt++) {
            try {
                return shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            } catch (TimeoutException ignored) {
                if (attempt < MAX_SCROLLS) ScrollHelper.scrollDown(driver);
            }
        }
        throw new NoSuchElementException("iOS element not found: " + locator);
    }

    private void hideKeyboardSafely() {
        try {
            driver.hideKeyboard();
        } catch (RuntimeException ignored) {
            // No keyboard shown or WDA cannot dismiss it separately.
        }
    }
}
