package me.djnwzs.bysj;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DailogAdapter extends BaseAdapter {
    private Activity activity;

    public DailogAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return MainActivity.address_list.size();
    }

    @Override
    public Object getItem(int i) {
        return MainActivity.address_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = View.inflate(activity, R.layout.text_dialog, null);
        TextView textView = (TextView) view.findViewById(R.id.dialog_text);
        textView.setText(MainActivity.name_list.get(i) + ": " + MainActivity.address_list.get(i));
        return view;
    }
}