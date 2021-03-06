package com.xnote.lol.xnote.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.xnote.lol.xnote.Controller;
import com.xnote.lol.xnote.DB;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.Search;
import com.xnote.lol.xnote.Util;
import com.xnote.lol.xnote.XnoteLogger;
import com.xnote.lol.xnote.adapters.SearchResultAdapter;
import com.xnote.lol.xnote.models.SearchResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by koopuluri on 2/28/15.
 */
public class SearchFragment extends ListFragment {
    public static final String TAG = "SearchFragment";

    SearchView mSearchView;
    SearchResultAdapter mAdapter;
    boolean mInitialized;
    SearchListener mListener;
    ListView mListView;
    ProgressBar mSpinner;
    String mQuery;

    public interface SearchListener {
        public void onSearchItemDeleted();
        public boolean keyboardVisible();
        public XnoteLogger getLogger();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SearchListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement onItemDeleted().");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_list, container, false);

        // search related views:
        mSpinner = (ProgressBar) view.findViewById(R.id.search_progress_bar);
        mSpinner.setVisibility(View.GONE);

        mSearchView = (SearchView) view.findViewById(R.id.search_view);

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setVisibility(View.GONE);
        // setting search functionality:
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mQuery = query;
                new UpdateSearchResultsTask(query).execute();
                if (mListener.keyboardVisible())
                    Util.hideKeyboard(getActivity());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newQuery) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("query", newQuery);
                } catch (JSONException e) {
                    // do nothing.
                }
                mListener.getLogger().log("SearchFragment.newQuery", obj);

                mQuery = newQuery;
                new UpdateSearchResultsTask(newQuery).execute();
                return true;
            }
        });

        mAdapter = new SearchResultAdapter(getActivity(), new ArrayList<Object>(), this);
        // initializing with an empty list.
        List<SearchResult> retainedResults = getRetainedResults();
        if (retainedResults != null) {
            mAdapter.addAll(retainedResults);
        }
        setListAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroy() {
        mListener.getLogger().log("SearchFramgent.onDestroy", null);
        mListener.getLogger().flush();
        super.onDestroy();
    }



    //----------------------------------------------------------------------------------------------

    private void setRetainedResults(List<SearchResult> results) {
        FragmentManager fm = getFragmentManager();
        SearchRetainedFragment searchRetainedFragment =
                (SearchRetainedFragment) fm.findFragmentByTag(SearchRetainedFragment.TAG);
        if (searchRetainedFragment != null) { // TODO: make sure this is always true! (it must be...)
            searchRetainedFragment.setResults(results);
        }
    }

    private List<SearchResult> getRetainedResults() {
        FragmentManager fm = getFragmentManager();
        SearchRetainedFragment searchRetainedFragment =
                (SearchRetainedFragment) fm.findFragmentByTag(SearchRetainedFragment.TAG);
        if (searchRetainedFragment != null) { // TODO: make sure this is always true! (it must be...)
            return searchRetainedFragment.getSearchResults();
        } else {
            return null;
        }
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // when an article is selected from the Article List:
        SearchResult result = (SearchResult) getListAdapter().getItem(position);
        if (result.type.equals(DB.ARTICLE)) {
            // launching ArticleActivity with the note-id as an EXTRA for the intent:
            Controller.launchArticleActivity(getActivity(), result.id);
        } else if (result.type.equals(DB.NOTE)) {
            Controller.launchNoteActivity(getActivity(),
                    result.articleId,
                    result.id,
                    SearchFragment.TAG);
        } else {
            // do nothing.
        }
    }


    public void updateResults(String newQuery) {
        new UpdateSearchResultsTask(newQuery).execute();
    }

    private class UpdateSearchResultsTask extends AsyncTask<Void, Void, Void> {
        private final String TAG = "SearchResultsFragment.UpdateSearchResultsTask";
        private String query;

        public UpdateSearchResultsTask(String newQuery) {
            query = newQuery;
        }

        private List<SearchResult> resultList;
        @Override
        protected void onPreExecute() {
            mSpinner.setVisibility(View.VISIBLE);
            mAdapter.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            resultList = new ArrayList<SearchResult>();
            if (!query.equals("")) {
                resultList.addAll(Search.search(query));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            super.onPostExecute(_);
            mSpinner.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mAdapter.addAll(resultList);
            mAdapter.notifyDataSetChanged();
            if (!mInitialized) {
                ListView listView = getListView();
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                listView.setMultiChoiceModeListener(new ModeListener());
                mInitialized = true;
            }
        }
    }


    private class DeleteSelectedItemsTask extends AsyncTask<Void, Void, Void> {
        private final String TAG = "DeleteSelectedItemsTask";
        List<Integer> selectedPositions;
        List<SearchResult> copiedItemList;
        @Override
        protected void onPreExecute() {
            selectedPositions = mAdapter.getSelectedPositions();
            copiedItemList = new ArrayList<SearchResult>();
            List<SearchResult> list = mAdapter.getSearchResultList();
            for (SearchResult r : list) {
                copiedItemList.add(r.clone());
            }
            mAdapter.removeItemsAtIndices(selectedPositions);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int pos : selectedPositions) {
                SearchResult result = copiedItemList.get(pos);
                if (result.type.equals(DB.ARTICLE)) {
                    DB.deleteArticle(result.id);
                } else if (result.type.equals(DB.NOTE)) {
                    DB.deleteNote(result.id);
                } else {
                    // do nothing.
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            super.onPostExecute(_);
            mListener.onSearchItemDeleted();
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mSearchView.isIconified())
                mSearchView.setIconified(false);
            
            if (mQuery != null) {  // only run if user has submitted a query previously.
                new UpdateSearchResultsTask(mQuery).execute();
            }
        } else {
            // hiding keyboard:
            if (getActivity() != null) {
                if (mListener.keyboardVisible())
                    Util.hideKeyboard(getActivity());
            } else {
                // do nothing. (Why would the activity be null at this point?
            }
        }
    }

    public List<SearchResult> getCurrentSearchResultList() {
        return mAdapter.getSearchResultList();
    }


    /**
     * Code obtained from: http://developer.android.com/guide/topics/ui/menus.html#CAB.
     */
    private class ModeListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            // Here you can do something when items are selected/de-selected,
            // such as update the title in the CAB
            if (checked) {
                mAdapter.addSelection(position);
            } else {
                mAdapter.removeSelection(position);
            }

            // notifiying change in views:
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Respond to clicks on the actions in the CAB
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    new DeleteSelectedItemsTask().execute();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate the menu for the CAB
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.list_menu_selection, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Here you can make any necessary updates to the activity when
            // the CAB is removed. By default, selected items are deselected/unchecked.
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here you can perform updates to the CAB due to
            // an invalidate() request
            return false;
        }
    }
}