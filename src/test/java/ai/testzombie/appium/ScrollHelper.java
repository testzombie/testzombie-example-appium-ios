package ai.testzombie.appium;

import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.List;

/**
 * W3C-Touch-Geste statt Android-spezifischem mobile: scrollGesture.
 */
final class ScrollHelper {
    private ScrollHelper() {}

    static void scrollDown(IOSDriver driver) {
        Dimension size = driver.manage().window().getSize();
        int x = size.width / 2;
        int startY = (int) (size.height * 0.78);
        int endY = (int) (size.height * 0.28);

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), x, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(new Pause(finger, Duration.ofMillis(120)));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(500),
                PointerInput.Origin.viewport(), x, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(swipe));
    }
}
