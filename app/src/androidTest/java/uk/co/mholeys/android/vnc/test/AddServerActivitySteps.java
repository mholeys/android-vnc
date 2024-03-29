package uk.co.mholeys.android.vnc.test;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.util.Log;

import org.junit.Rule;

import java.util.Collection;

import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import uk.co.mholeys.android.vnc.AddServerActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import uk.co.mholeys.android.vnc.R;

/**
 * Created by Matthew on 10/01/2018.
 */

@CucumberOptions(features = "features")
public class AddServerActivitySteps {

    @Rule
    public ActivityTestRule<AddServerActivity> activityTestRule = new ActivityTestRule<>(AddServerActivity.class, true, false);


    @Before
    public void setup() {
        Log.d("AddServerTest", "setup: ");
        CommonSteps.disableAnimations();
    }

    @After
    public void tearDown() {
        Log.d("AddServerTest", "tearDown: ");
        CommonSteps.enableAnimations();
    }

    @Nullable
    private Activity getActivity() {
        final Activity[] currentActivity = new Activity[1];
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED);
                if (resumedActivities.iterator().hasNext()){
                    currentActivity[0] = (Activity) resumedActivities.iterator().next();
                }
            }
        });

        return currentActivity[0];
    }

    @Given("^I am on the AddServerActivity$")
    public void given_add_server_activity() {
        if (activityTestRule.getActivity() == null) {
            activityTestRule.launchActivity(null);
        }
    }

    @Then("^I should see the AddServerActivity$")
    public void I_have_a_AddServerActivity$() {
        Activity activity = getActivity();
        Log.d("VncTest", "I_have_a_AddServerActivity$: " + activity);
        assertTrue(activity instanceof AddServerActivity);
    }

}
