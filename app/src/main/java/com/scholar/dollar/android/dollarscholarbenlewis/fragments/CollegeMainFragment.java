package com.scholar.dollar.android.dollarscholarbenlewis.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.scholar.dollar.android.dollarscholarbenlewis.R;
import com.scholar.dollar.android.dollarscholarbenlewis.activities.DetailActivity;
import com.scholar.dollar.android.dollarscholarbenlewis.adapter.CollegeAdapter;
import com.scholar.dollar.android.dollarscholarbenlewis.data.CollegeContract;
import com.scholar.dollar.android.dollarscholarbenlewis.utility.Utility;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by bplewis5 on 10/3/16.
 */

public class CollegeMainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = CollegeMainFragment.class.getSimpleName();
//    OnStateSelectedListener mCallback;

    private String mSelection;
    private String[] mSelectionArgs;
    private ArrayList<String> mSelectionList;
    private ArrayList<String> mSelectionArgList;
    private FirebaseAnalytics mFirebaseAnalytics;
    public static final String OWNERSHIP_SELECTION =
            CollegeContract.CollegeMainEntry.OWNERSHIP + " = ? ";
    public static final String FAVORITES_SELECTION =
            CollegeContract.CollegeMainEntry.IS_FAVORITE + " = ? ";
    public static final String STATES_SELECTION =
            CollegeContract.CollegeMainEntry.STATE + " = ? ";
    public static final String SORT_HIGHEST_EARNINGS = CollegeContract.CollegeMainEntry.MED_EARNINGS_2012 + " DESC";
    private boolean mOnlyPublic;

    private Uri mUri;
    private static final int COLLEGE_LOADER = 1000;
    private CollegeAdapter mCollegeAdapter;


    private LoaderManager mLoaderManager;

    public CollegeMainFragment() {
    }

    public void updateStateSelection(String state) {
        Log.i(LOG_TAG, "STATE INPUT UPDATE_STATE_SELECTION: " + state);
        getStateSelection(state);
        mLoaderManager.restartLoader(COLLEGE_LOADER, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, container, false);
        mCollegeAdapter = new CollegeAdapter(getContext(), new CollegeAdapter.CollegeAdapterOnClickHandler() {
            @Override
            public void onClick(int collegeId, boolean isPublic, CollegeAdapter.CollegeAdapterViewHolder vh) {
                Log.i(LOG_TAG, "COLLEGE ID: " + collegeId);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Integer.toString(collegeId));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                startActivity(new Intent(getContext(), DetailActivity.class)
                        .putExtra(Utility.COLLEGE_ID_KEY, collegeId)
                        .putExtra(Utility.PUBLIC_COLLEGE_KEY, isPublic));
            }
        });
        recyclerView.setAdapter(mCollegeAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return recyclerView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        mSelectionList = new ArrayList<>();
        mSelectionArgList = new ArrayList<>();
        if (args != null) {
            mSelectionArgs = new String[]{"1"};
            mSelectionArgList.add("1");
            if (args.getBoolean(Utility.PUBLIC_COLLEGE_KEY, false)) {
                mOnlyPublic = true;
                mSelectionList.add(OWNERSHIP_SELECTION);
                mSelection = OWNERSHIP_SELECTION;
            } else {
                mSelection = FAVORITES_SELECTION;
                mSelectionList.add(FAVORITES_SELECTION);
                mOnlyPublic = false;
            }
        } else {
            mOnlyPublic = false;
            mSelection = null;
            mSelectionArgs = null;
        }
        Log.i(LOG_TAG, "SELECTION: " + mSelection);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoaderManager = getLoaderManager();
        mLoaderManager.initLoader(COLLEGE_LOADER, null, this);
    }


    @Override
    public void onLoaderReset(Loader loader) {
        mCollegeAdapter.swapCursor(null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "SELECTION FROM ON_CREATE_LOADER: " + mSelection);
        Log.i(LOG_TAG, "SELECTION ARGS FROM ON_CREATE_LOADER: " + Arrays.toString(mSelectionArgs));
        return new CursorLoader(getContext(), CollegeContract.CollegeMainEntry.COLLEGE_MAIN_CONTENT_URI,
                Utility.COLLEGE_COLUMNS, mSelection, mSelectionArgs, SORT_HIGHEST_EARNINGS);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCollegeAdapter.swapCursor(data);
    }

    private void getStateSelection(String state) {
        if (state.equals("All")) {
            if (mOnlyPublic) {
                mSelection = OWNERSHIP_SELECTION;
                mSelectionArgs = new String[]{"1"};
            } else {
                mSelection = null;
                mSelectionArgs = null;
            }
            return;
        }
        if (!mOnlyPublic) {
            mSelection = STATES_SELECTION;
            mSelectionArgs = new String[]{state};
            Log.i(LOG_TAG, "GET_STATE_SELECTION: " + mSelection);
        } else {
            mSelection = OWNERSHIP_SELECTION + " AND " + STATES_SELECTION;
            mSelectionArgs = new String[]{"1", state};
            Log.i(LOG_TAG, "GET_STATE_SELECTION: " + mSelection);
        }
    }

}