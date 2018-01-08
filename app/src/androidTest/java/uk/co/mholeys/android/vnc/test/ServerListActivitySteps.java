package uk.co.mholeys.android.vnc.test;

import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.assertion.ViewAssertions;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import uk.co.mholeys.android.vnc.*;
import uk.co.mholeys.android.vnc.R;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

/**
 * Created by Matthew on 08/01/2018.
 */

@CucumberOptions(features = "features")
public class ServerListActivitySteps extends ActivityInstrumentationTestCase2<ServerListActivity> {

    public ServerListActivitySteps() {
        super(ServerListActivity.class);
    }

    @Given("^I have a ServerListActivity$")
    public void I_have_a_ServerListActivity() {
        assertNotNull(getActivity());
    }

    @When("^I press (\\S+)$")
    public void I_press_d(String button) {
        ServerListActivity activity = getActivity();
        // onView
        // with...
    }

    @Then("^I should see (\\S+)$")
    public void I_should_see_server(String server) {
        ServerListActivity activity = getActivity();
        ListView servers = (ListView) activity.findViewById(R.id.server_list_view);
        //onView(withId(R.id.server_list_view)).check(ViewAssertions.matches(withText(server));

    }
}
