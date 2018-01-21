package uk.co.mholeys.android.vnc.test;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.util.Log;

import org.junit.Rule;

import java.util.Collection;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import uk.co.mholeys.android.vnc.*;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Matthew on 21/01/2018.
 */

public class EditServerActivitySteps {


    @Rule
    public ActivityTestRule<EditServerActivity> activityTestRule = new ActivityTestRule<>(EditServerActivity.class, true, false);


    @Before
    public void setup() {
        Log.d("EditServerTest", "setup: ");
        CommonSteps.disableAnimations();
    }

    @After
    public void tearDown() {
        Log.d("EditServerTest", "tearDown: ");
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

    @Given("^I am on the EditServerActivity$")
    public void given_edit_server_activity() {
        if (activityTestRule.getActivity() == null) {
            activityTestRule.launchActivity(null);
        }
    }

    @Then("^I should see the EditServerActivity$")
    public void I_have_a_EditServerActivity$() {
        Activity activity = getActivity();
        Log.d("VncTest", "I_have_a_EditServerActivity$: " + activity);
        assertTrue(activity instanceof EditServerActivity);
    }


}
