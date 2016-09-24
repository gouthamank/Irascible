package app.drool.irascible.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import app.drool.irascible.activities.ChatActivity;

public class UserListAdapter extends BaseAdapter {
    private Context context;
    private String[] names;

    public UserListAdapter(Context context, String[] names) {
        this.context = context;
        this.names = names;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return names[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView != null) {
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(names[position]);
            return convertView;
        } else {
            TextView view = (TextView) LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            view.setText(names[position]);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = ((TextView) v).getText().toString();
                    ((ChatActivity) context).appendToEditText(username);
                }
            });
            return view;
        }
    }


}
