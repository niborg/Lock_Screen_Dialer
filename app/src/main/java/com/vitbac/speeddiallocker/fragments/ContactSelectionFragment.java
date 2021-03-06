package com.vitbac.speeddiallocker.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.vitbac.speeddiallocker.R;
import com.vitbac.speeddiallocker.helpers.ContactsCursorAdapter;

import static android.provider.ContactsContract.Contacts;

//import android.app.LoaderManager;
//import android.content.Loader;

public class ContactSelectionFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {

    /**
     * Defines a constant to identify the columns to be returned from a query
     */
    // TODO Do we need to account for old versions in using PHOTO_THUMBNAIL_URI??
    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION = {
            Contacts._ID,
            Contacts.LOOKUP_KEY,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    Contacts.PHOTO_THUMBNAIL_URI :
                    Contacts.PHOTO_ID,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    Contacts.DISPLAY_NAME_PRIMARY :
                    Contacts.DISPLAY_NAME
    };

    /**
     * A constant to get the data we want.  Combination of text expressions and variables that tell
     * the provider the data columns to search and the values to find.
     */
    @SuppressLint("InlinedApi")
    private static final String SELECTION =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    "(" + Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?) AND " +
                    "(" + Contacts.DISPLAY_NAME_PRIMARY + " IS NOT NULL) AND" +
                    "(" + Contacts.HAS_PHONE_NUMBER + " = 1)" :

                    "(" + Contacts.DISPLAY_NAME + " LIKE ?) AND " +
                            "(" + Contacts.DISPLAY_NAME + " IS NOT NULL) AND " +
                    "(" + Contacts.HAS_PHONE_NUMBER + " = 1)";


/*                    "(("
                            + ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " NOTNULL) AND ("
                            + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                            + ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " != '' ))" :
                    // Use DISPLAY_NAME otherwise
                    "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                            + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                            + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))"; //" LIKE ?";*/
    /**
     * Defines an array that contains column names to move from the Cursor to the ListView
     */
    private final static String[] FROM_COLUMNS = {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    Contacts.PHOTO_THUMBNAIL_URI :
                    Contacts.PHOTO_ID, // Thumbnail-sized photo
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    Contacts.DISPLAY_NAME_PRIMARY :
                    Contacts.DISPLAY_NAME
    };
    /**
     * Defines an array that contains resource ids for the layout views
     * that get inflated with the Cursor column contents.
     */
    private final static int[] TO_IDS = { R.id.contact_selector_list_item_pic,
            R.id.contact_selector_list_item_name };
    // The column indices for the columns you substantiated in the PROJECTION
    private static final int CONTACT_ID_INDEX = 0;
    private static final int CONTACT_LOOKUP_KEY_INDEX = 1;
    private static final int PHOTO_THUMBNAIL_URI_INDEX = 2;
    private static final int CONTACT_DISPLAY_NAME_INDEX = 3;
    // Define the ListView object that will bind to the cursorAdapter
    ListView mContactsList;
    // Define variables for the contact the user selects
    // The contact's _ID value
    long mContactId;
    // The contact's LOOKUP_KEY
    String mContactKey;
    // A content URI for the selected contact
    Uri mContactUri;
    // Defines an array to hold values that replace the "?" in the SELECTION
    private String[] mSelectionArgs;
    // The search filter to weed through the contacts
    private String mSearchString;
    // The view encapsulating the search text
    private EditText mEditText;
    // An adapter that binds the result Cursor to the ListView
    private ContactsCursorAdapter mCursorAdapter;

    private OnContactSelectedListener mListener;  // Requires implementation in parent activity

    public ContactSelectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static ContactSelectionFragment newInstance() {
        ContactSelectionFragment fragment = new ContactSelectionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Fragment", "onCreate() entered.");
        mSelectionArgs = new String[] { mSearchString };
        /*
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.hello_blank_fragment);
        return textView;
        */

        // Inflate the fragment layout
        Log.d("Fragment", "onCreateView executed");
        return inflater.inflate(R.layout.contact_selector_list_view, container, false);
    }

    /*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(int phonenum) {
        if (mListener != null) {
            mListener.onContactSelected(phonenum);
        }
    } */

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnContactSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Gets the ListView from the parent activity (even though it was returned from here)
        mContactsList = (ListView) getActivity().findViewById(android.R.id.list);

        // Gets the EditText view
        mEditText = (EditText) getActivity().findViewById(R.id.contact_search_bar);
        // Set the text changing listener
        mEditText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after){

            }

            public void onTextChanged(CharSequence s, int start, int count, int after){
                mSearchString = s.toString();

                // This if statement functions to avoid a contact list update when search
                // string is null or just a blank space.
                // TODO: blank space situation doesn't appear to be working
                if (mSearchString.length() == 0 || mSearchString == " "){
                    return;
                }
                // Reload the query
                restartLoaderManager();  // Can't directly restart here, because "this" variable incorrect in this context
            }

        });

        // Get CursorAdapter
        mCursorAdapter = new ContactsCursorAdapter(
                getActivity(),
                R.layout.contact_selector_list_item,
                null, // cursor not available yet
                FROM_COLUMNS,
                TO_IDS,
                0);
        // Set the adapter for the ListView
        mContactsList.setAdapter(mCursorAdapter);
        // Set the item click listener to be the current fragment
        mContactsList.setOnItemClickListener(this);
        // Initialize the loader
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onItemClick (AdapterView<?> parent, View item, int position, long rowID) {
        Log.d("listener", "onItemClick() reached.");
        // Get the Cursor
        Cursor cursor = ((ContactsCursorAdapter) parent.getAdapter()).getCursor();
        // Move to teh selected contact
        cursor.moveToPosition(position);
        // Get the _ID value
        mContactId = cursor.getLong(CONTACT_ID_INDEX);
        // Get the selected LOOKUP_KEY
        mContactKey = cursor.getString(CONTACT_LOOKUP_KEY_INDEX);
        // Create the contact's content Uri
        mContactUri = Contacts.getLookupUri(mContactId, mContactKey);

        String thumbnailUriString = cursor.getString(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                        PHOTO_THUMBNAIL_URI_INDEX :
                        CONTACT_ID_INDEX
        );
        // The INDEX for the display name is the same regardless of android version
        String contactName = cursor.getString(CONTACT_DISPLAY_NAME_INDEX);

        //mListener.onContactSelected(mContactId, mContactKey, mContactUri);
//        mListener.onContactSelected(mContactKey);  // Returns Lookup Key to Activity
        mListener.onContactSelected(mContactKey, contactName, thumbnailUriString);
    }

    /**
     * Implementation of the onCreateLoader() method.  Use "%" to represent a sequence of zero
     * or more characters; "_" to represent a single character.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        mSelectionArgs[0] = "%" + mSearchString + "%";

        // Start the query

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new CursorLoader(
                    getActivity(),
                    Contacts.CONTENT_URI,
                    PROJECTION,
                    SELECTION,
                    mSelectionArgs,
                    Contacts.DISPLAY_NAME_PRIMARY);
        }
        return new CursorLoader(
                getActivity(),
                Contacts.CONTENT_URI,
                PROJECTION,
                SELECTION,
                mSelectionArgs,
                Contacts.DISPLAY_NAME);
    }

    /**
     * Implement onLoadFinished, which is loaded when the Contacts Provider returns the results
     * of a query.  In this method, put the result of Cursor in teh ContactsCursorAdapter to
     * automatically update the ListView with the search results
     */
    public void onLoadFinished (Loader<Cursor> loader, Cursor cursor) {
        // Put the result Cursor in the adapter for the ListView
        mCursorAdapter.swapCursor(cursor);
    }

    /**
     * This method is invoked when the loader framework detects that the result Cursor contains
     * stale data.  If you don't delete the ContactsCursorAdapter reference to the existing Cursor
     * the loader framework will not recycle the Cursor, which causes a memory leak.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Delete the reference to the existing Cursor
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Private simple class to handle text change listener
     */
    private void restartLoaderManager() {
        getLoaderManager().restartLoader(0, null, this);
    }

    /**
     * Private method that returns the Uri for a photo thumbnail, handling the dichotomy between
     * post-Honeycomb versions
     * TODO: parsing best to be placed in new fragment
     */
    private Uri getPhotoThumbnailUri (String data) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return Uri.parse(data);
        }

        return Uri.withAppendedPath(
                Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, data),
                ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
            );
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
    public interface OnContactSelectedListener {
        public void onContactSelected(String lookupKey);

        public void onContactSelected(String lookupKey, String displayName, String thumbnailUriString);
    }


}
