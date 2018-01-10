package uk.co.mholeys.android.vnc.test;

import android.app.Activity;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import android.widget.ListView;

import org.junit.Rule;

import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import uk.co.mholeys.android.vnc.*;
import uk.co.mholeys.android.vnc.R;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Matthew on 08/01/2018.
 */

@CucumberOptions(features = "features")
public class ServerListActivitySteps {

    @Rule
    public ActivityTestRule<ServerListActivity> activityTestRule = new ActivityTestRule<>(ServerListActivity.class, true, true);

    Intent intent = new Intent();
    Activity activity;

    @Before
    public void setup() {
        Log.d("VNCTest", "setup: ");
        activity = activityTestRule.getActivity();
    }

    @After
    public void tearDown() {
        Log.d("VNCTest", "tearDown: ");
    }

    @Given("^I am on the ServerListActivity$")
    public void I_have_a_ServerListActivity() {
        assertNotNull(activity);
        assertTrue(activity instanceof ServerListActivity);
    }

    @When("^I press (\\S+)$")
    public void I_press_x(String item) {
        switch (item) {
            case "Add":
                onView(withId(R.id.add_server_action_bar_button));
                break;
        }
        // onView
        // with...
    }

    @When("^I press (\\S+) in the (.*)$")
    public void I_press_x_in_y(String item, String container) {
        switch (container.toLowerCase()) {
            case "action bar":
                switch (item) {
                    case "add":
                        onView(withId(R.id.add_server_action_bar_button));
                        break;
                }
                break;
            case "server list":
                switch (item) {
                }
                break;
            default:
                onView(withText(item));
                break;
        }

    }

    @When("^I long press (\\S+)$")
    public void I_long_press_x(String item) {

    }

    @When("^I long press (\\S+) in the (.*)$")
    public void I_long_press_x_in_y(String item, String container) {

    }

    @Then("^I should see (\\S+)$")
    public void I_should_see_server(String server) {
        ListView servers = (ListView) activity.findViewById(R.id.server_list_view);
        //onView(withId(R.id.server_list_view)).check(ViewAssertions.matches(withText(server));

    }
}
