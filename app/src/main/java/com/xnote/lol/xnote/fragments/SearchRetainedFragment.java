package com.xnote.lol.xnote.fragments;

import android.app.Fragment;
import android.os.Bundle;
import com.xnote.lol.xnote.models.SearchResult;
import java.util.List;

/**
 * Created by koopuluri on 3/21/15.
 */
public class SearchRetainedFragment extends Fragment {
    public static final String TAG = "SearchRetainedFragment";

    // data object we want to retain
    List<SearchResult> mResults;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setResults(List<SearchResult> results) {
        mResults = results;
    }

    public List<SearchResult> getSearchResults() {
        return mResults;
    }
}
