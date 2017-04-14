package proto.group8.cs465.groceryhelper;

import android.app.SearchManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.util.List;

import proto.group8.cs465.groceryhelper.model.Item;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    ItemFragment.OnListFragmentInteractionListener,
                    MapFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private boolean favsIsDirty = false, listIsDirty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // fab measurements used in translation
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final ViewGroup.MarginLayoutParams fabLp = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // translate the fab a proportional amount on/off screen
                int width = getWindowManager().getDefaultDisplay().getWidth();
                float totalDxToOffscreen =
                        width - (fab.getX() - fab.getTranslationX()) + fab.getWidth() / 2 + fabLp.rightMargin / 2;
                float translationX;
                if (position == 1) {
                    translationX = totalDxToOffscreen * positionOffset; // moving offscreen
                } else {
                    translationX = totalDxToOffscreen * (1f - positionOffset); // moving back onscreen
                }
                fab.setTranslationX(translationX);
            }

            @Override
            public void onPageSelected(int position) {
                // refresh the list pages every time
                if (position != 2) {
                    ItemFragment listFragment = (ItemFragment) getCurrentFragment();
                    if (position == 0 && favsIsDirty) {
                        listFragment.refresh();
                        favsIsDirty = false;
                    } else if (position == 1 && listIsDirty) {
                        listFragment.refresh();
                        listIsDirty = false;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currFragment = getCurrentFragment();
                if (currFragment instanceof ItemFragment && mViewPager.getCurrentItem() == 1) {
                    ItemFragment itemFragment = (ItemFragment) currFragment;
                    if (!itemFragment.hasBlankItem()) {
                        itemFragment.addBlankItem();
                    }
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.search);
        Log.e("MainActivity", "searchItem: " + searchItem);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
        } else {
            Log.e("MainActivity", "SearchView is null!");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onListFragmentInteraction(Item item) {

    }

    @Override
    public void onListItemFinalized() {
        // user has finished editing a new item...
        // ...so we add a new blank item to the bottom of the list
        Fragment currFragment = getCurrentFragment();
        if (currFragment instanceof ItemFragment) {
            ItemFragment itemFragment = (ItemFragment) currFragment;
            itemFragment.addBlankItem();
        }
        listIsDirty = true;
    }

    @Override
    public void onListItemFavoriteToggled(Item item) {
        // update the favorites list
        List<Item> favoritesList = ((MyApplication) getApplication()).getFavoriteItemsList();
        if (item.isFavorited()) {
            favoritesList.add(item);
        } else {
            favoritesList.remove(item);
        }
        favsIsDirty = true;

        // show snackbar to re-assure user that something happened
        String msg = String.format("%s '%s' %s favorites",
                item.isFavorited() ? "Added" : "Removed", item.getName(), item.isFavorited() ? "to" : "from");
        Snackbar.make(findViewById(R.id.coord_layout), msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return ItemFragment.newInstance(1, ItemFragment.TYPE_FAVORITES_LIST);
                case 1:
                    return ItemFragment.newInstance(1, ItemFragment.TYPE_LIST);
                case 2:
                default:
                    return MapFragment.newInstance("Map", "Fragment");
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "My Favorites";
                case 1:
                    return "My List";
                case 2:
                    return "My Map";
            }
            return null;
        }


    }
}
