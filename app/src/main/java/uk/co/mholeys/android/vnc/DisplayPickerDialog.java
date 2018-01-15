package uk.co.mholeys.android.vnc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.media.MediaRouter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Matthew on 12/01/2018.
 */

public class DisplayPickerDialog extends Dialog {

    HashMap<String, Object> displays;
    ArrayList<DisplayItem> displayItems = new ArrayList<DisplayItem>();
    DisplayListAdapter listAdapter;
    OnClickListener onClickListener;
    Activity activity;

    ListView mListView;

    public DisplayPickerDialog(@NonNull Context context, Activity activity, HashMap<String, Object> displays, OnClickListener onClickListener) {
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

        mListView = findViewById(R.id.dialog_list);

        Display builtin = getWindow().getWindowManager().getDefaultDisplay();

        for (String name : displays.keySet()) {
            Object d = displays.get(name);
            if (d instanceof MediaRouter.RouteInfo) {
                // Cast
                MediaRouter.RouteInfo route = (MediaRouter.RouteInfo) d;
                DisplayItem di = new DisplayItem(R.drawable.quantum_ic_cast_white_36, route.getName(), "Chromecast device");
                displayItems.add(di);
            } else if (d instanceof Display) {
                Display display = (Display) d;
                DisplayItem di = new DisplayItem(R.drawable.quantum_ic_cast_white_36, display.getName(), "External Display");
                if (display.equals(builtin)) {
                    di = new DisplayItem(R.drawable.quantum_ic_cast_white_36, display.getName(), "Built-In screen");
                }
                // Detect built in
                //DisplayItem di = new DisplayItem(R.drawable.hdmi_display, display.getName(), "External Display");
                displayItems.add(di);
            }
        }

        listAdapter = new DisplayListAdapter(activity, displayItems);
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

        public DisplayItem(int image, String name, String extra) {
            this.image = image;
            this.name = name;
            this.extra = extra;
        }
    }

    class DisplayListAdapter implements ListAdapter {

        Activity activity;
        ArrayList<DisplayItem> displays = new ArrayList<DisplayItem>();

        public DisplayListAdapter(Activity activity, ArrayList<DisplayItem> displays) {
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = activity.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.display_list_item_view, null,true);

            TextView nameText = rowView.findViewById(R.id.item_name_text);
            ImageView imageView = rowView.findViewById(R.id.icon);
            TextView extraText = rowView.findViewById(R.id.extra_text);

            nameText.setText(displays.get(i).name);
            imageView.setImageResource(displays.get(i).image);
            extraText.setText(displays.get(i).extra);
            return rowView;
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

}
