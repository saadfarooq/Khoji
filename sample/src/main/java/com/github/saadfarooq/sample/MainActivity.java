package com.github.saadfarooq.sample;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.saadfarooq.sample.draweritem.DrawerItem;
import com.github.saadfarooq.sample.draweritem.DrawerItemCollection;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.openDrawer(GravityCompat.START);

        ListView drawerList = (ListView) findViewById(R.id.drawer_list);
        drawerList.setAdapter(new DrawerListAdapter(new DrawerItemCollection("Main title").getCollectedItems()));
    }

    private class DrawerListAdapter extends BaseAdapter {
        private final List<DrawerItem> drawerItemsList;

        public DrawerListAdapter(List<DrawerItem> drawerItemsList) {
            this.drawerItemsList = drawerItemsList;
        }

        @Override
        public int getCount() {
            return drawerItemsList.size();
        }

        @Override
        public Object getItem(int position) {
            return drawerItemsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.activity_list_item, parent, false);
            }
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(drawerItemsList.get(position).getTitle());
            return convertView;
        }
    }
}
