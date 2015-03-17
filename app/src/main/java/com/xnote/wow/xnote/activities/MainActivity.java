package com.xnote.wow.xnote.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.content.Intent;

import com.xnote.wow.xnote.Constants;
import com.xnote.wow.xnote.ParseArticleManager;
import com.xnote.wow.xnote.ParseCallback;
import com.xnote.wow.xnote.R;
import com.xnote.wow.xnote.fragments.ArticleListFragment;
import com.xnote.wow.xnote.fragments.SearchFragment;
import com.xnote.wow.xnote.fragments.SearchResultsFragment;
import com.xnote.wow.xnote.fragments.SettingsFragment;
import com.xnote.wow.xnote.models.ParseArticle;

/**
 * Created by koopuluri on 2/28/15.
 */
public class MainActivity extends Activity implements SearchResultsFragment.OnItemDeleted,
                                                      ArticleListFragment.ArticleListListener {

    public static final String TAG = "MainActivity";
    public static final String ARTICLES_TAB = "Articles";
    public static final String SEARCH_TAB = "Search";
    public static final String SETTINGS_TAB = "Settings";
    public static final String CURRENT_POSITION = "CurrentPosition";
    public static final String[] TABS = {ARTICLES_TAB, SEARCH_TAB, SETTINGS_TAB};

    PagerAdapter mPagerAdapter;
    ViewPager mViewPager;
    ArticleListFragment mArticleListFrag;
    SearchFragment mSearchFrag;
    int currentPosition;
    String newArticleId;
    ParseArticleManager mArticleManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        newArticleId = getIntent().getStringExtra(Constants.NEW_ARTICLE_ID);
        Log.d(TAG, "value of intent.ExtraText: " + String.valueOf(getIntent()
                .getStringExtra(Intent.EXTRA_TEXT)));
        mPagerAdapter = new PagerAdapter(
                getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.main_view_pager);
        mViewPager.setAdapter(mPagerAdapter);
        mArticleManager = ParseArticleManager.getInstance();
        if (savedInstanceState != null)
            currentPosition = savedInstanceState.getInt(CURRENT_POSITION);  // so the fragment that was last viewed can be given focus.
        // action bar shii:
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                        currentPosition = position;
                    }
                });
        // specifying that tabs should be displayed in actionbar:
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // creating a Tab listener called when user changes tabs:
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };
        // add 3 tabs specifying tab's next and tabListener??
        for (String tabName : TABS) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(tabName)
                            .setTabListener(tabListener));
        }
        Log.d(TAG, "end of onCreate()");
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_POSITION, currentPosition);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "resumed()!!: " + getIntent().getStringExtra(Intent.EXTRA_TEXT));
        Log.d(TAG, "resumed()!!: intent action: " + getIntent().getAction());
    }


    @Override
    public void onStop() {
        super.onStop();
        ParseArticleManager.cancelAll();
        Log.d(TAG, "onStop().");
    }


    private class PagerAdapter extends android.support.v13.app.FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Log.d(TAG, "getItem() in PageAdapter");
            if (i == 0) {
                mArticleListFrag = ArticleListFragment.newInstance(newArticleId);
                Log.d(TAG, "ArticleListFragment selected.");
                return mArticleListFrag;
            } else if (i == 1) {
                mSearchFrag = new SearchFragment();
                Log.d(TAG, "SearchFragment selected.");
                return mSearchFrag;
            } else if (i == 2) {
                Fragment frag = new SettingsFragment();
                Log.d(TAG, "SettingsFragment selected.");
                return frag;
            } else {
                Log.e(TAG, "no fragment chosen, this state should not be possible!");
                return null;
            }
        }

        @Override
        public int getCount() {
            return 3;  // total
        }

        @Override
        public CharSequence getPageTitle(int pos) {
            return String.valueOf(pos);  // TODO: change this to display TAB contents.
        }
    }

    @Override
    public void onItemDeleted() {
        // need to refresh ArticleListFragment:
        Log.d(TAG, "Refresh the article list fragment");
        mArticleListFrag.refresh();
    }

    @Override
    public void parseArticle(ParseArticle article) {
        final String articleId = article.getId();
        ParseArticleManager.startParsing(articleId, new ParseCallback() {
            @Override
            public void run(ParseArticle updatedArticle) {
                mArticleListFrag.onParseArticleCompleted(updatedArticle);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // do nothing.
    }
}