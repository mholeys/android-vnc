package uk.co.mholeys.android.vnc.test;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.util.Log;

import org.junit.Rule;

import java.util.Collection;
import java.util.Iterator;

import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import uk.co.mholeys.android.vnc.AddServerActivity;
import uk.co.mholeys.android.vnc.ServerListActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Matthew on 10/01/2018.
 */

//@CucumberOptions(features = "feaatures")
public class AddServerActivitySteps {

    @Rule
    public ActivityTestRule<AddServerActivity> activityTestRule = new ActivityTestRule<>(AddServerActivity.class, true, true);

    Intent intent = new Intent();
    Activity activity;

    @Before
    public void setup() {
        activity = activityTestRule.getActivity();
    }

    @After
    public void tearDown() {

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

    @Then("^I should see the AddServerActivity$")
    public void I_have_a_AddServerActivity$() {
        Activity activity = getActivity();
        Log.d("VncTest", "I_have_a_AddServerActivity$: " + activity);
        assertTrue(activity instanceof AddServerActivity);
    }

}
