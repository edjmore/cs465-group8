package proto.group8.cs465.groceryhelper;

import android.app.SearchManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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

    private int mCurrentPosition = 0;
    // track instantiated fragments for easy access
    private final Fragment[] mActiveFragments = new Fragment[3];

    // flags indicating if we need to refresh a fragment list view
    private boolean favsIsDirty = false, listIsDirty = false;

    private final Handler mHandler = new Handler();

    private boolean mIsInEditMode = false;

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
                mCurrentPosition = position;

                if (position != 2) {
                    // hide the slideup panel on map view
                    View panel = findViewById(R.id.slideup_panel);
                    if (panel != null) panel.setTranslationY(dpToPx(396));
                }

                // disengage edit mode for any previous fragments we were just in
                for (int i = 0; i < 2; i++) {
                    if (i == position) continue; // skip current

                    ItemFragment frag = (ItemFragment) getFragment(i);
                    if (frag != null) {
                        frag.getAdapter().disengageEditMode();
                    }
                }

                // update menu
                mIsInEditMode = false;
                invalidateOptionsMenu();
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
                onListItemFinalized();
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

    private float dpToPx(float dp) {
        DisplayMetrics metric = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metric);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        // options menu items are context dependent
        menu.findItem(R.id.search).setVisible(!mIsInEditMode);
        menu.findItem(R.id.cancel).setVisible(mIsInEditMode);
        menu.findItem(R.id.delete).setVisible(mIsInEditMode && mCurrentPosition == 1);
        menu.findItem(R.id.transfer).setVisible(mIsInEditMode && mCurrentPosition == 0);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        final ItemFragment frag = mIsInEditMode && getCurrentFragment() instanceof ItemFragment ?
                (ItemFragment) getCurrentFragment() : null;

        switch (id) {
            case R.id.cancel:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIsInEditMode = false;
                        invalidateOptionsMenu();
                        if (frag != null) frag.getAdapter().disengageEditMode();
                    }
                });
                return true;

            case R.id.delete:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // delete all selected items
                        if (frag != null) {
                            MyItemRecyclerViewAdapter adapter = frag.getAdapter();
                            if (!adapter.getSelectedItems().isEmpty()) {
                                int removeCount = adapter.getSelectedItems().size();

                                Iterator<Item> iter = adapter.getSelectedItems().iterator();
                                while (iter.hasNext()) {
                                    Item toRemove = iter.next();
                                    int idx = adapter.getValues().indexOf(toRemove);

                                    toRemove.setIsInCart(false); // impossible now (might still be in favorites)

                                    iter.remove();                        // remove from selected set
                                    adapter.getValues().remove(toRemove); // remove from global list
                                    adapter.notifyItemRemoved(idx);       // remove from UI
                                }

                                adapter.disengageEditMode();
                                Snackbar.make(findViewById(R.id.coord_layout),
                                        String.format(Locale.US, "Deleted %d item(s)", removeCount), Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                return true;

            case R.id.transfer:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // transfer selected items to shopping cart
                        if (frag != null) {
                            MyItemRecyclerViewAdapter adapter = frag.getAdapter();
                            if (!adapter.getSelectedItems().isEmpty()) {
                                int xferCount = adapter.getSelectedItems().size();

                                List<Item> itemsList = ((MyApplication) getApplication()).getItemsList();

                                Iterator<Item> iter = adapter.getSelectedItems().iterator();
                                while (iter.hasNext()) {
                                    Item toAdd = iter.next();
                                    int idx = adapter.getValues().indexOf(toAdd);

                                    // add to shopping list (if necessary)
                                    if (!itemsList.contains(toAdd)) {
                                        itemsList.add(Math.max(itemsList.size() - 1, 0), toAdd); // before editable item
                                    } else {
                                        xferCount--;
                                    }

                                    iter.remove();                  // remove from selected set
                                    adapter.notifyItemChanged(idx); // deselect in UI
                                }

                                adapter.disengageEditMode();
                                if (xferCount > 0) {
                                    // need to refresh UI in shopping list w/ new items
                                    ItemFragment listFrag = (ItemFragment) getFragment(1);
                                    listFrag.getAdapter()
                                            .notifyItemRangeChanged(itemsList.size() - xferCount - 1, xferCount + 1);

                                    Snackbar.make(findViewById(R.id.coord_layout),
                                            String.format(Locale.US, "Transferred %d new item(s) to shopping cart", xferCount), Snackbar.LENGTH_SHORT).show();
                                } else {
                                    // no items were transferred (already in list)
                                    Snackbar.make(findViewById(R.id.coord_layout),
                                            "Selected item(s) already in shopping cart!", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
                return true;
        }

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
        Log.e("MainActivity", String.format("List item finalized"));
        // user has finished editing a new item...
        // ...so we add a new blank item to the bottom of the list
        final ItemFragment frag = (ItemFragment) getFragment(1);
        if (frag != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    frag.addBlankItem();
                }
            });
        }
        //listIsDirty = true;
    }

    @Override
    public void onListItemFavoriteToggled(Item item) {
        // update the favorites list
        List<Item> favoritesList = ((MyApplication) getApplication()).getFavoriteItemsList(),
                    itemsList = ((MyApplication) getApplication()).getItemsList();
        if (item.isFavorited()) {
            favoritesList.add(item);
            final int idx = favoritesList.size() - 1;

            // notify new item added
            final ItemFragment frag = (ItemFragment) getFragment(0);
            if (frag != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        frag.getAdapter().notifyDataSetChanged();
                        Log.e("MainActivity", String.format("Item inserted at idx: %d", idx));
                    }
                });
            }

        } else {
            final int favIdx = favoritesList.indexOf(item),
                        listIdx = itemsList.indexOf(item);
            favoritesList.remove(item);

            // just need to remove one item from the favorites fragment, and update the item in the list fragment
            final ItemFragment favFrag = (ItemFragment) getFragment(0),
                                listFrag = (ItemFragment) getFragment(1);
            if (favFrag != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        favFrag.getAdapter().notifyItemRemoved(favIdx);
                        Log.e("MainActivity", String.format("Item removed at idx: %d", favIdx));
                    }
                });
            }
            if (listFrag != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listFrag.getAdapter().notifyItemChanged(listIdx);
                        Log.e("MainActivity", String.format("Item updated at idx: %d", listIdx));
                    }
                });
            }
        }
        //favsIsDirty = true;

        // show snackbar to re-assure user that something happened
        String msg = String.format("%s '%s' %s favorites",
                item.isFavorited() ? "Added" : "Removed", item.getName(), item.isFavorited() ? "to" : "from");
        Snackbar.make(findViewById(R.id.coord_layout), msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onListItemChecked(final Item item) {
        Log.e("MainActivity", "Item checked!!");
        final ItemFragment frag = (ItemFragment) getFragment(1);
        if (frag != null && frag != getCurrentFragment()) {
            MyApplication app = (MyApplication) getApplication();
            final int idx = app.getItemsList().indexOf(item);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    frag.getAdapter().notifyItemChanged(idx);
                    Log.e("MainActivity", String.format("Item checked at idx: %d", idx));
                }
            });
        }
        Snackbar.make(findViewById(R.id.coord_layout),
                String.format("%s '%s' %s shopping cart",
                        item.isInCart() ? "Added" : "Removed", item.getName(), item.isInCart() ? "to" : "from"),
                Snackbar.LENGTH_SHORT).show();
        //listIsDirty = true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onListItemLongClicked(Item item, MyItemRecyclerViewAdapter adapter) {
        if (mCurrentPosition == 2) return; // don't care in the map

        if (adapter.isInEditMode()) {
            // already in edit mode, treat like a regular click
            onListItemClicked(item, adapter);
        } else {
            // engage edit mode and select this item
            adapter.engageEditMode();
            adapter.toggleSelection(item);

            mIsInEditMode = true;
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onListItemClicked(Item item, MyItemRecyclerViewAdapter adapter) {
        // toggle selection if in edit mode
        if (adapter.isInEditMode()) {
            boolean allItemsDeselected = adapter.toggleSelection(item);

            if (allItemsDeselected) {
                // time to disengage edit mode
                adapter.disengageEditMode();

                mIsInEditMode = false;
                invalidateOptionsMenu();
            }
        }
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
    }

    private Fragment getFragment(int position) {
        return mActiveFragments[position];
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
            Fragment newFrag;
            switch (position) {
                case 0:
                    newFrag =  ItemFragment.newInstance(1, ItemFragment.TYPE_FAVORITES_LIST);
                    break;
                case 1:
                    newFrag =  ItemFragment.newInstance(1, ItemFragment.TYPE_LIST);
                    break;
                case 2:
                default:
                    newFrag = MapFragment.newInstance("Map", "Fragment");
            }
            // keep reference to fragment
            if (position >= 0 && position < mActiveFragments.length) mActiveFragments[position] = newFrag;
            return newFrag;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // I think this is only called in orientation change, but it's implemented just in case
            Fragment oldFrag = (Fragment) super.instantiateItem(container, position);
            mActiveFragments[position] = oldFrag;
            return oldFrag;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            // remove reference so the GC can free memory
            mActiveFragments[position] = null;
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
