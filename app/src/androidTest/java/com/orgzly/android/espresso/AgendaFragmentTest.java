package com.orgzly.android.espresso;

import android.content.pm.ActivityInfo;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.rule.ActivityTestRule;
import android.widget.DatePicker;

import com.orgzly.R;
import com.orgzly.android.OrgzlyTest;
import com.orgzly.android.ui.MainActivity;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.orgzly.android.espresso.EspressoUtils.listViewItemCount;
import static com.orgzly.android.espresso.EspressoUtils.onListItem;
import static com.orgzly.android.espresso.EspressoUtils.setNumber;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

public class AgendaFragmentTest extends OrgzlyTest {
    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<>(MainActivity.class, true, false);

    private void defaultSetUp() {
        shelfTestUtils.setupBook("book-one",
                "First book used for testing\n" +

                        "* Note A.\n" +

                        "* TODO Note B\n" +
                        "SCHEDULED: <2014-01-01>\n" +

                        "*** TODO Note C\n" +
                        "SCHEDULED: <2014-01-02 ++1d>\n");

        shelfTestUtils.setupBook("book-two",
                "Sample book used for tests\n" +

                        "*** DONE Note 1\n" +
                        "CLOSED: [2014-01-03 Tue 13:34]\n" +

                        "**** Note 2\n" +
                        "SCHEDULED: <2014-01-04 Sat>--<2044-01-10 Fri>\n");

        activityRule.launchActivity(null);
    }

    private void emptySetup() {
        activityRule.launchActivity(null);
    }

    private void openAgenda() {
        onView(withId(R.id.drawer_layout)).perform(open());
        onView(withText("Agenda")).perform(click());
    }

    private void selectAgendaPeriod(int days) {
        onView(withId(R.id.agenda_period)).perform(click());
        onView(withId(R.id.number_picker)).perform(setNumber(days));
        onView(withText(R.string.set)).perform(click());
    }

    @Test
    public void testWithNoBook() {
        emptySetup();
        openAgenda();
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(3)));
        selectAgendaPeriod(7);
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(7)));
    }

    @Test
    public void testDayAgenda() {
        defaultSetUp();
        openAgenda();
        selectAgendaPeriod(1);
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(4)));
        onListItem(1).onChildView(withId(R.id.item_head_title)).check(matches(allOf(withText(endsWith("Note B")), isDisplayed())));
        onListItem(2).onChildView(withId(R.id.item_head_title)).check(matches(allOf(withText(endsWith("Note C")), isDisplayed())));
        onListItem(3).onChildView(withId(R.id.item_head_title)).check(matches(allOf(withText(endsWith("Note 2")), isDisplayed())));
    }

    @Test
    public void testWeekAgenda() {
        defaultSetUp();
        openAgenda();
        selectAgendaPeriod(7);
        // 7 date headers, 1 Note B, 7 x Note C, 7 x Note 2
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(22)));
    }

    @Test
    public void testOneTimeTaskMarkedDone() {
        defaultSetUp();
        openAgenda();
        selectAgendaPeriod(7);
        onListItem(1).perform(swipeRight());
        onListItem(1).onChildView(withId(R.id.item_menu_done_state_btn)).perform(click());
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(21)));
    }

    @Test
    public void testRepeaterTaskMarkedDone() {
        defaultSetUp();
        openAgenda();
        selectAgendaPeriod(7);
        onListItem(2).perform(swipeRight());
        onListItem(2).onChildView(withId(R.id.item_menu_done_state_btn)).perform(click());
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(21)));
    }

    @Test
    public void testRangeTaskMarkedDone() {
        defaultSetUp();
        openAgenda();
        selectAgendaPeriod(7);
        onListItem(3).perform(swipeRight());
        onListItem(3).onChildView(withId(R.id.item_menu_done_state_btn)).perform(click());
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(15)));
    }

    @Test
    public void testChangeStateUntilDone() {
        defaultSetUp();
        openAgenda();
        selectAgendaPeriod(7);
        onListItem(2).perform(swipeRight());
        onListItem(2).onChildView(withId(R.id.item_menu_next_state_btn)).perform(click());
        onListItem(2).onChildView(withId(R.id.item_menu_next_state_btn)).perform(click());
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(21)));
    }

    @Test
    public void testShiftRepeaterTaskToTomorrow() {
        DateTime tomorrow = DateTime.now().withTimeAtStartOfDay().plusDays(1);

        defaultSetUp();
        openAgenda();
        selectAgendaPeriod(7);
        onListItem(2).perform(swipeRight());
        onListItem(2).onChildView(withId(R.id.item_menu_schedule_btn)).perform(click());
        onView(withId(R.id.dialog_timestamp_date_picker)).perform(click());
        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(
                        tomorrow.getYear(),
                        tomorrow.getMonthOfYear(),
                        tomorrow.getDayOfMonth()));
        onView(withText(R.string.ok)).perform(click());
        onView(withText(R.string.set)).perform(click());
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(21)));
    }

    @Test
    public void testPersistedSpinnerSelection() {
        defaultSetUp();
        openAgenda();
        selectAgendaPeriod(7);
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(22)));
        activityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        onView(allOf(withId(android.R.id.list), isDisplayed())).check(matches(listViewItemCount(22)));
    }
}
