package com.xnote.lol.xnote.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.xnote.lol.xnote.Constants;
import com.xnote.lol.xnote.R;
import com.xnote.lol.xnote.Util;
import com.xnote.lol.xnote.buffers.ReadBuffer;
import com.xnote.lol.xnote.fragments.ArticleFragment;
import com.xnote.lol.xnote.fragments.ArticleRetainedFragment;
import com.xnote.lol.xnote.models.ParseArticle;
import com.xnote.lol.xnote.models.ParseNote;

import java.util.List;

/**
 * Created by koopuluri on 2/22/15.
 */
public class ArticleActivity extends Activity implements ArticleFragment.ArticleFragmentInterface {
    public static final String TAG = "ArticleActivity";

    ArticleFragment mArticleFrag;
    ArticleRetainedFragment mRetained;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poop);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_xnote_navigation_up_colored);

        // setting the custom toolbar as the activity's action bar:
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        android.widget.Toolbar toolbar = (android.widget.Toolbar) findViewById(R.id.toolbar);
//        toolbar.setNavigationIcon(R.drawable.ic_xnote_cancel);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        // initializing articleFragment with tis articleText:
        FragmentManager fm = getFragmentManager();
//        setActionBar(toolbar);
        // initializing the retained buffer if it doesn't exist:
        mRetained = (ArticleRetainedFragment) fm.findFragmentByTag(ArticleRetainedFragment.TAG);
        if (mRetained == null) {
            mRetained = new ArticleRetainedFragment();
            fm.beginTransaction()
                    .add(mRetained, ArticleRetainedFragment.TAG)
                    .commit();
        }
        // now this retained data can be passed to underlying article fragment!
        // adding the article fragment:
        mArticleFrag = (ArticleFragment) ArticleFragment.newInstance(
                getIntent().getStringExtra(Constants.ARTICLE_ID));
        fm.beginTransaction()
                .add(R.id.poop_fragment_container, mArticleFrag, ArticleFragment.TAG)
                        // .add(R.id.poop_fragment_container, mLoadingFrag, ArticleLoadingFragment.TAG)
                .commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.NOTE_REQUEST) {
            // time to update buffers:
            if (resultCode == RESULT_OK) {
                // unpack the data received:
                ParseNote note = new ParseNote();
                note.setId(data.getStringExtra(Constants.NOTE_ID));
                note.setContent(data.getStringExtra(Constants.NOTE_CONTENT));
                note.setTimestamp(data.getLongExtra(Constants.NOTE_TIMESTAMP, System.currentTimeMillis()));
                note.setStartIndex(data.getIntExtra(Constants.START_INDEX, 0));
                note.setEndIndex(data.getIntExtra(Constants.END_INDEX, 0));
                note.setArticleId(data.getStringExtra(Constants.ARTICLE_ID));
                note.setSelectedText(data.getStringExtra(Constants.NOTE_SELECTED_TEXT));
                int noteState = data.getIntExtra(Constants.NOTE_STATE, 0);
                // ---------------------------------- PUT IN BACKGROUND!!--------------------------
                if (noteState != 0)  // TODO: remove this check, needs to save note regardless of noteState!
                    mArticleFrag.addNoteFromNoteActivity(note, noteState);
                // ---------------------------------------------------------------------------------
                // "Result from NoteActivity OK. buffers have been updated!");
            }
            else {
                // do nothing
            }
        }
        else {
            // do nothing
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.poop_activity_actions, menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // respond to action-up button:
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // this means that this activity is not part of this app's task,
                    // so create new task when navigating up, with synthesized (???) back stack.
                    android.support.v4.app.TaskStackBuilder.create(this)
                            // Add all of activity's parents to back stack:
                            .addNextIntentWithParentStack(upIntent)
                                    // navigate up to closest parent:
                            .startActivities();
                    // else just go to the parent activity that this was called from:
                } else {
                    Intent intent = NavUtils.getParentActivityIntent(this);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra(Constants.ARTICLE_ID, getIntent().getStringExtra(
                            Constants.ARTICLE_ID));
                    NavUtils.navigateUpTo(this, intent);
                }
                return true;

            case R.id.action_share:
                Util.share(mArticleFrag.getArticleTitle(), mArticleFrag.getArticleShareMessage(),
                        getResources().getString(R.string.article_share_message), this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public String toString() {
        return TAG;
    }

    // -------------------------- implementing ArticleFragmentInterface ----------------------------

    public ReadBuffer getRetainedBuffer() {
        mRetained = (ArticleRetainedFragment) getFragmentManager().
                findFragmentByTag(ArticleRetainedFragment.TAG);
        return mRetained.getArticleBuffer();
    }

    public void setRetainedBuffer(ReadBuffer buffer) {
        mRetained.setArticleBuffer(buffer);
    }

    public ParseArticle getRetainedArticle() {
        return mRetained.getArticle();
    }

    public void setRetainedArticle(ParseArticle article) {
        mRetained.setArticle(article);
    }

    public List<ParseNote> getRetainedNotes() {
        return mRetained.getNotes();
    }

    public void setRetainedNotes(List<ParseNote> notes) {
        mRetained.setNotes(notes);
    }
}