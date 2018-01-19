package uk.co.mholeys.android.vnc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * Created by Matthew on 12/01/2018.
 */

public class DisplayPickerDialog extends Dialog {

    private ArrayList<Object> displays;
    private ArrayList<DisplayItem> displayItems = new ArrayList<DisplayItem>();
    private OnClickListener onClickListener;
    private Activity activity;

    DisplayPickerDialog(@NonNull Context context, Activity activity, ArrayList<Object> displays, OnClickListener onClickListener) {
        super(context);
        this.activity = activity;
        this.displays = displays;
        this.onClickListener = onClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_display_layout);
        setTitle(R.string.display_list_select_title);

        ListView mListView = findViewById(R.id.dialog_list);

        Window window = getWindow();
        Display builtin = null;
        if (window != null) {
            builtin = window.getWindowManager().getDefaultDisplay();
        }

        for (Object d : displays) {
            if (d instanceof String) {
                DisplayItem di = new DisplayItem(R.drawable.ic_cast_black_48dp, "Current cast device", "");
                displayItems.add(di);
            } else if (d instanceof MediaRouter.RouteInfo) {
                // Cast
                MediaRouter.RouteInfo route = (MediaRouter.RouteInfo) d;
                DisplayItem di = new DisplayItem(R.drawable.ic_cast_black_48dp, route.getName(), activity.getString(R.string.display_device_label_chromecast));
                displayItems.add(di);
            } else if (d instanceof Display) {
                Display display = (Display) d;
                Display.Mode mode = display.getMode();
                String resolution = " " + mode.getPhysicalWidth() + "x" + mode.getPhysicalHeight();
                DisplayItem di = new DisplayItem(R.drawable.ic_tv_black_48dp, display.getName(), activity.getString(R.string.display_device_label_external) + resolution);
                if (display.equals(builtin)) {
                    di = new DisplayItem(R.drawable.ic_smartphone_black_48dp, display.getName(), activity.getString(R.string.display_device_label_builtin) + resolution);
                }
                displayItems.add(di);
            }
        }

        DisplayListAdapter listAdapter = new DisplayListAdapter(activity, displayItems);
        mListView.setAdapter(listAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onClickListener.onClick(DisplayPickerDialog.this, i);
                dismiss();
            }
        });
    }

    class DisplayItem {
        int image;
        String name;
        String extra;

        DisplayItem(int image, String name, String extra) {
            this.image = image;
            this.name = name;
            this.extra = extra;
        }
    }

    class DisplayListAdapter implements ListAdapter {

        Activity activity;
        ArrayList<DisplayItem> displays = new ArrayList<DisplayItem>();

        DisplayListAdapter(Activity activity, ArrayList<DisplayItem> displays) {
            this.activity = activity;
            this.displays = displays;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public int getCount() {
            return displays.size();
        }

        @Override
        public Object getItem(int i) {
            return displays.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DisplayHolder mDisplayHolder;

            if (convertView == null) {
                mDisplayHolder = new DisplayHolder();

                LayoutInflater inflater = activity.getLayoutInflater();

                convertView = inflater.inflate(R.layout.display_list_item_view, parent,false);
                mDisplayHolder.nameText = convertView.findViewById(R.id.item_name_text);
                mDisplayHolder.imageView = convertView.findViewById(R.id.icon);
                mDisplayHolder.extraText = convertView.findViewById(R.id.extra_text);

                convertView.setTag(mDisplayHolder);
            } else {
                mDisplayHolder = (DisplayHolder) convertView.getTag();
            }
            mDisplayHolder.nameText.setText(displays.get(position).name);
            mDisplayHolder.imageView.setImageResource(displays.get(position).image);
            mDisplayHolder.extraText.setText(displays.get(position).extra);

            return convertView;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return displayItems.isEmpty();
        }
    }

    static class DisplayHolder {
        TextView nameText;
        ImageView imageView;
        TextView extraText;
    }

}
