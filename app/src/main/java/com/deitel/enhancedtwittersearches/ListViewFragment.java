package com.deitel.enhancedtwittersearches;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnItemListSelectedListener}
 * interface.
 */
public class ListViewFragment extends ListFragment {

    private static final String TAG = "ListViewFragment";

    private OnItemListSelectedListener mListener;

    // name of SharedPreferences XML file that stores the saved searches
    private static final String SEARCHES = "searches";

    private SharedPreferences savedSearches; // user's favorite searches
    private ArrayList<String> tags; // list of tags for saved searches
    private ArrayAdapter<String> adapter; // binds tags to ListView

    public static ListViewFragment newInstance() {
        ListViewFragment fragment = new ListViewFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // setup listener for a item long click
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // get the tag that the user long touched
            final String tag = ((TextView) view).getText().toString();

            // create a new AlertDialog
            AlertDialog.Builder builder =
               new AlertDialog.Builder(getActivity());

            // set the AlertDialog's title
            builder.setTitle(
               getString(R.string.shareEditDeleteTitle, tag));

            // set list of items to display in dialog
            builder.setItems(R.array.dialog_items,
               new DialogInterface.OnClickListener()
               {
                  // responds to user touch by sharing, editing or
                  // deleting a saved search
                  @Override
                  public void onClick(DialogInterface dialog, int which)
                  {
                     switch (which)
                     {
                        case 0: // share
                           shareSearch(tag);
                           break;
                        case 1: // edit
                           // set EditTexts to match chosen tag and query
                            mListener.editTaggedSearch(tag, savedSearches.getString(tag,""));
                           break;
                        case 2: // delete
                           deleteSearch(tag);
                           break;
                     }
                  }
               } // end DialogInterface.OnClickListener
            ); // end call to builder.setItems

            // set the AlertDialog's negative Button
            builder.setNegativeButton(getString(R.string.cancel),
               new DialogInterface.OnClickListener()
               {
                 // called when the "Cancel" Button is clicked
                  public void onClick(DialogInterface dialog, int id)
                  {
                     dialog.cancel(); // dismiss the AlertDialog
                  }
               }
            ); // end call to setNegativeButton

            builder.create().show(); // display the AlertDialog
            return true;
         } // end method onItemLongClickreturn false;
        });
    }

    // allows user to choose an app for sharing a saved search's URL
    private void shareSearch(String tag) {
        // create the URL representing the search
        String urlString = getString(R.string.searchURL) +
                Uri.encode(savedSearches.getString(tag, ""), "UTF-8");

        Log.d(TAG, "In shareSearch for " + urlString);

        // create Intent to share urlString
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.shareSubject));
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.shareMessage, urlString));
        shareIntent.setType("text/plain");

        // display apps that can share text
        startActivity(Intent.createChooser(shareIntent,
                getString(R.string.shareSearch)));
    }

    // deletes a search after the user confirms the delete operation
    private void deleteSearch(final String tag) {
        Log.d(TAG, "In deleteSearch for tag:" + tag);
        // create a new AlertDialog
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(getActivity());

        // set the AlertDialog's message
        confirmBuilder.setMessage(
                getString(R.string.confirmMessage, tag));

        // set the AlertDialog's negative Button
        confirmBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    // called when "Cancel" Button is clicked
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // dismiss dialog
                    }
                }
        ); // end call to setNegativeButton

        // set the AlertDialog's positive Button
        confirmBuilder.setPositiveButton(getString(R.string.delete),
                new DialogInterface.OnClickListener() {
                    // called when "Cancel" Button is clicked
                    public void onClick(DialogInterface dialog, int id) {
                        tags.remove(tag); // remove tag from tags

                        // get SharedPreferences.Editor to remove saved search
                        SharedPreferences.Editor preferencesEditor =
                                savedSearches.edit();
                        preferencesEditor.remove(tag); // remove search
                        preferencesEditor.apply(); // saves the changes

                        // rebind tags ArrayList to ListView to show updated list
                        adapter.notifyDataSetChanged();
                    }
                } // end OnClickListener
        ); // end call to setPositiveButton

        confirmBuilder.create().show(); // display AlertDialog
    } // end method deleteSearch

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnItemListSelectedListener) activity;

            setupListView(activity);

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemListSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            String tag = ((TextView) v).getText().toString();
            String uriString = mListener.buildSearchString(savedSearches.getString(tag, ""));

            Log.d(TAG, "In onListItemClick() for:" + uriString);

            mListener.onItemSelectedInteraction(uriString);
        }
    }

    // setup adapter for the listview
    private void setupListView(Activity activity) {
        // get the SharedPreferences containing the user's saved searches
        savedSearches = activity.getSharedPreferences(SEARCHES, Context.MODE_PRIVATE);

        // store the saved tags in an ArrayList then sort them
        tags = new ArrayList<String>(savedSearches.getAll().keySet());
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);

        // create ArrayAdapter and use it to bind tags to the ListView
        adapter = new ArrayAdapter<String>(activity, R.layout.list_item, tags);
        setListAdapter(adapter);

    }

    // add new search to the save file, then refresh all Buttons
    public void addTaggedSearch(String query, String tag)
    {
        Log.d(TAG,"In addTaggedSearch: " + tag + " query:" + query);

        // get a SharedPreferences.Editor to store new tag/query pair
        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query); // store current search
        preferencesEditor.apply(); // store the updated preferences

        // if tag is new, add to and sort tags, then display updated list
        if (!tags.contains(tag))
        {
            tags.add(tag); // add new tag
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged(); // rebind tags to ListView
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnItemListSelectedListener {
        void onItemSelectedInteraction(String id);
        String buildSearchString(String topic);
        void editTaggedSearch(String tag, String topic);
        }

}
