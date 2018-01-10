package uk.co.mholeys.android.vnc.test;

import android.app.Activity;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;

import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import uk.co.mholeys.android.vnc.AddServerActivity;
import uk.co.mholeys.android.vnc.ServerListActivity;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Matthew on 10/01/2018.
 */

@CucumberOptions(features = "features")
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

    @Then("^I should see the AddServerActivity$")
    public void I_have_a_ServerListActivity() {
        assertTrue(activity instanceof AddServerActivity);
    }

}
