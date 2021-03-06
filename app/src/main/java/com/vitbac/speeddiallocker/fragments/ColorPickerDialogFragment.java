package com.vitbac.speeddiallocker.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vitbac.speeddiallocker.R;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerDialogFragment extends DialogFragment
        implements AdapterView.OnItemClickListener {

    private List<ColorListItem> mItems;
    private OnColorSelectedListener mListener;
    private OnNoColorSelectedListener mOtherListener;
    private int mColorSelected;
    private ColorListItem mColorItem;
    private boolean mNoColorSelected;
    private int mKey;
    private View mFragView;
    private String TAG = "ColorPickerDialogFragment";


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ColorPickerDialogFragment() {
    }

    public static ColorPickerDialogFragment newInstance(int key) {
        ColorPickerDialogFragment frag = new ColorPickerDialogFragment();
        Bundle bundle = new Bundle(3);
        bundle.putInt("key", key);
        bundle.putInt("colorSelected", 0);
        bundle.putBoolean("noColorSelected", true);
        frag.setArguments(bundle);
        return frag;
    }

    public static ColorPickerDialogFragment newInstance(int colorSelected, int key) {
        ColorPickerDialogFragment frag = new ColorPickerDialogFragment();
        Bundle bundle = new Bundle(3);
        bundle.putInt("key", key);
        bundle.putInt("colorSelected", colorSelected);
        bundle.putBoolean("noColorSelected", false);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        mKey = bundle.getInt("key");
        mColorSelected = bundle.getInt("colorSelected");
        mNoColorSelected = bundle.getBoolean("noColorSelected");

        mItems = new ArrayList<ColorListItem>();
        TypedArray colors = getResources().obtainTypedArray(R.array.color_value_list);
        TypedArray names = getResources().obtainTypedArray(R.array.color_name_list);
        int length;

        if (colors.length() != names.length()) {
            Log.e(TAG, "Resource array of names and colors do not match");
            // In this weird case, just constrain list to the smaller of the two
            length = colors.length() < names.length() ? colors.length() : names.length();
        } else {
            length = colors.length();
        }

        for (int i = 0; i < length; i++) {
            ColorListItem cli = new ColorListItem(colors.getColor(i, 0), names.getString(i));
            mItems.add(cli);
            if (colors.getColor(i, 0) == mColorSelected) {
                mColorItem = cli;
            }
        }
        colors.recycle();
        names.recycle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d(TAG, "in oncreateview");
        mFragView = inflater.inflate(R.layout.fragment_color_list_dialog, container, false); // Appears that calling correct layout on "show()" moots this
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return mFragView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        // Scroll to the selected color to indicate the window's scrollability
        final ListView colorList = (ListView) mFragView.findViewById(R.id.color_list);
        final int position = ((ColorListAdapter)colorList.getAdapter()).getPosition(mColorItem);
        colorList.post(new Runnable() {
            @Override
            public void run() {
                colorList.smoothScrollToPosition(position);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnColorSelectedListener) activity;
            // the OnNoColorSelectedListener is not mandatory!
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnColorSelectedListener");
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

        ListView colorList = (ListView) mFragView.findViewById(R.id.color_list);
        colorList.setOnItemClickListener(this);
        colorList.setAdapter(new ColorListAdapter(getActivity(), mItems));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ColorListAdapter adapter = (ColorListAdapter) parent.getAdapter();
        ColorListItem item = adapter.getItem(position);

        if (mListener != null) {
            mListener.onColorSelected(item.color, mKey);
            dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        //Log.d(TAG, "onCancel() called");
        if (mOtherListener != null) {
            mOtherListener.onNoColorSelected(mKey);
        }
        super.onCancel(dialog);
    }

    public void setOnNoColorSelectedListener(OnNoColorSelectedListener listener) {
        mOtherListener = listener;
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color, int key);
    }

    public interface OnNoColorSelectedListener {
        void onNoColorSelected(int key);
    }

    public class ColorListItem {
        public int color;
        public String name;

        public ColorListItem(int color, String name) {
            this.color = color;
            this.name = name;
        }
    }

    public class ColorListAdapter extends ArrayAdapter<ColorListItem> {
        private static final int mListItemLayout = R.layout.fragment_color_list_item;

        public ColorListAdapter(Context context, List<ColorListItem> items) {
            super(context, mListItemLayout, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                // means a new view should be created

                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(mListItemLayout, parent, false);

                // initialize the view holder
                viewHolder = new ViewHolder();
                viewHolder.colorView = (TextView) convertView.findViewById(R.id.color_list_item);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            ColorListItem item = getItem(position);
            viewHolder.colorView.setText(item.name);
            viewHolder.colorView.setBackgroundColor(item.color);
            viewHolder.colorView.setTextColor(getResources().getColor(R.color.white));
            viewHolder.colorView.setShadowLayer(3, 1, 1, getResources().getColor(R.color.black_cow));

            if (!mNoColorSelected && item.color == mColorSelected) {
                viewHolder.colorView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD_ITALIC);
                viewHolder.colorView.setText(item.name + " (*)");

            } else {
                // To prevent recycling of views with remnants of bold italic
                viewHolder.colorView.setTypeface(null, Typeface.NORMAL);
            }

            return convertView;
        }

        private class ViewHolder {
            TextView colorView;
        }
    }

}
