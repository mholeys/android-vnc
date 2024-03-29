package uk.co.mholeys.android.vnc.test;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.test.espresso.ViewAction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.junit.Rule;

import java.nio.charset.MalformedInputException;
import java.util.Collection;

import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import uk.co.mholeys.android.vnc.*;
import uk.co.mholeys.android.vnc.R;
import uk.co.mholeys.android.vnc.data.ServerEntry;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.anything;

/**
 * Created by Matthew on 08/01/2018.
 */

@CucumberOptions(features = "features")
public class ServerListActivitySteps {

    @Rule
    public ActivityTestRule<ServerListActivity> activityTestRule = new ActivityTestRule<>(ServerListActivity.class, true, false);

    Intent intent = new Intent();

    @Before
    public void setup() {
        Log.d("ServerListTest", "setup: ");
        CommonSteps.disableAnimations();
    }

    @After
    public void tearDown() {
        Log.d("ServerListTest", "tearDown: ");
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

    @Given("^I am on the ServerListActivity$")
    public void given_server_list_activity() {
        if (activityTestRule.getActivity() == null) {
            if (getActivity() instanceof ServerListActivity) {
                return;
            }
            activityTestRule.launchActivity(null);
        }
    }

    @Then("^I should see the ServerListActivity$")
    public void I_have_a_ServerListActivity() {
        Activity activity = getActivity();
        assertNotNull(activity);
        assertTrue(activity instanceof ServerListActivity);
    }

    @Then("^ServerList should( | not )contain a server with \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"")
    public void server_list_has_server(String not, String name, String address, String port) {
        ServerListActivity activity = (ServerListActivity) getActivity();
        ListView list = (ListView) activity.findViewById(R.id.server_list_view);
        ListAdapter la = list.getAdapter();
        int i = -1;
        boolean found = false;
        for (i = 0; i < list.getCount(); i++) {
            ServerEntry se = (ServerEntry) la.getItem(i);
            ServerData sd = se.serverData;
            int portInt = -1;
            try {
                portInt =  Integer.valueOf(port);
            } catch (NumberFormatException e) {
                // Port isnt a number
            }
            if ((sd.name.equals(name)) && (sd.address.equals(address)) && (sd.port == portInt)) {
                assertEquals(name, sd.name);
                assertEquals(address, sd.address);
                assertEquals(portInt, sd.port);
                onData(anything())
                        .inAdapterView(withId(R.id.server_list_view)).atPosition(i)
                        .check(matches(isDisplayed()));
                found = true;
            }
        }
        // Ensure not == "not"
        if (not != null && !not.trim().equals("") && not.trim().toLowerCase().equals("not")) {
            if (found) {
                fail("Found server entry that should not be in the list " + name + " " + address + " " + port);
            }
        }
    }

    /*@Then("^I should see (\\S+)$")
    public void I_should_see_server(String server) {
        Activity activity = getActivity();
        ListView servers = (ListView) activity.findViewById(R.id.server_list_view);
        //onView(withId(R.id.server_list_view)).check(ViewAssertions.matches(withText(server));

    }*/
}
