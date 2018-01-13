package uk.co.mholeys.android.vnc.test;

import android.support.test.espresso.ViewAssertion;
import android.widget.Button;

import cucumber.api.CucumberOptions;
import cucumber.api.java.en.When;
import uk.co.mholeys.android.vnc.*;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.instanceOf;

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
        /*switch (item) {
            case "Add":
                onView(withId(uk.co.mholeys.android.vnc.R.id.add_server_action_bar_button)).perform(click());
                break;
        }*/
        onView(withText(item)).perform(closeSoftKeyboard()).perform(click());
    }

    @When("^I long press (\\S+)$")
    public void I_long_press_x(String item) {
        onView(withText(item)).perform(longClick());
    }

}
