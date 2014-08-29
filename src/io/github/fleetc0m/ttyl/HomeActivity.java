package io.github.fleetc0m.ttyl;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import io.github.fleetc0m.ttyl.core.BackgroundService;
import io.github.fleetc0m.ttyl.ui.SummaryFragment;

public class HomeActivity extends Activity {
    private static final String TAG = "HomeActivity";

    private String[] mNavDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mNavDrawerTitles = getResources().getStringArray(R.array.nav_drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer);
        mDrawerListView = (ListView) findViewById(R.id.nav_drawer_list_view);
        mDrawerListView.setAdapter(new ArrayAdapter<String>(
                this, R.layout.nav_drawer_item, mNavDrawerTitles));
        mDrawerListView.setOnItemClickListener(new NavDrawerItemOnClickListener());
        maybeStartBackgroundService();
    }

    private void maybeStartBackgroundService() {
        if (!isBackgroundServiceRunning()) {
            Intent backgroundServiceIntent = new Intent(this, BackgroundService.class);
            startService(backgroundServiceIntent);
        }
    }

    private boolean isBackgroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : activityManager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (info.service.getClassName().equals(BackgroundService.class.getName())) {
                return true;
            }
        }
        return false;
    }

    private class NavDrawerItemOnClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            switch (position) {
                case 0:
                    Fragment summaryFragment = new SummaryFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.main_display,
                            summaryFragment).commit();
            }
            mDrawerListView.setItemChecked(position, true);
            HomeActivity.this.setTitle(mNavDrawerTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerListView);
        }
    }
}
