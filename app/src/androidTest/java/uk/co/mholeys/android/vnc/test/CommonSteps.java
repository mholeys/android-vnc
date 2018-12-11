package uk.co.mholeys.android.vnc.test;

import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.action.ViewActions;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;

import cucumber.api.CucumberOptions;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.en.When;
import uk.co.mholeys.android.vnc.*;
import uk.co.mholeys.android.vnc.R;
import uk.co.mholeys.android.vnc.data.ServerDataSQLHelper;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withResourceName;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.support.test.uiautomator.UiDevice;
import android.widget.EditText;

/**
 * Created by Matthew on 11/01/2018.
 */

@CucumberOptions(features = "features")
public class CommonSteps {

    private static SystemAnimations mSystemAnimations;

    public static void enableAnimations() {
        mSystemAnimations = new SystemAnimations(getInstrumentation().getContext());
        mSystemAnimations.enableAll();;
    }

    public static void disableAnimations() {
        mSystemAnimations = new SystemAnimations(getInstrumentation().getContext());
        mSystemAnimations.disableAll();
    }

    @When("^I press (\\S+)$")
    public void I_press_x(String item) {
        onView(withText(equalToIgnoringCase(item))).perform(closeSoftKeyboard()).perform(click());
    }

    @When("^I long press (\\S+)$")
    public void I_long_press_x(String item) {
        onView(withText(item)).perform(longClick());
    }

    @After
    public void commonAfter(Scenario scenario) {
        if (scenario.isFailed()) {

            // Save to external storage (usually /sdcard/screenshots)
            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/screenshots/" + getTargetContext().getPackageName());
            if (!path.exists()) {
                path.mkdirs();
            }

            // Take advantage of UiAutomator screenshot method
            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            String filename = scenario.getId() + "." + scenario.getName() + ".png";
            device.takeScreenshot(new File(path, filename));
        }
    }

    @When("^I press (\\S+) in the (.*)$")
    public void I_press_x_in_y(String item, String container) {
        switch (container.toLowerCase()) {
            case "action bar":
                switch (item) {
                    case "add":
                        onView(withId(R.id.add_server_action_bar_button)).perform(click());
                        break;
                }
                break;
            case "server list":
                onView(withText(item)).perform(closeSoftKeyboard()).perform(scrollTo()).perform(click());
                break;
            default:
                onView(withText(item));
                break;
        }

    }

    @When("^I long press (\\S+) in the (.*)$")
    public void I_long_press_x_in_y(String item, String container) {
        switch (container.toLowerCase()) {
            case "action bar":
                switch (item) {
                    case "add":
                        onView(withId(R.id.add_server_action_bar_button)).perform(longClick());
                        break;
                }
                break;
            case "server list":
                onView(withText(item)).perform(closeSoftKeyboard()).perform(scrollTo()).perform(longClick());
                break;
            default:
                onView(withText(item));
                break;
        }
    }

    @When("^I fill in (\\S+) with (.*)$")
    public void I_fill_in_x_with_y(String field, String value) {
        onView(allOf(withResourceName(containsString(field)), instanceOf(EditText.class))).perform(closeSoftKeyboard()).perform(clearText()).perform(typeText(value));
    }

    @When("^I navigate up")
    public void I_navigate_up() {
        onView(withContentDescription("Navigate up")).perform(click());
    }

    @When("^I go back")
    public void I_go_back() {
        onView(isRoot()).perform(closeSoftKeyboard()).perform(ViewActions.pressBack());
    }

}
