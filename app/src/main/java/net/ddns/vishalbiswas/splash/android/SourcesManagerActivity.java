package net.ddns.vishalbiswas.splash.android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.ddns.vishalbiswas.splash.R;
import net.ddns.vishalbiswas.splash.classes.GlobalFunctions;

public class SourcesManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sources_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final SourcesAdapter sourcesAdapter = new SourcesAdapter();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabNewSource);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText serverEdit = new EditText(SourcesManagerActivity.this);
                serverEdit.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                new AlertDialog.Builder(SourcesManagerActivity.this).setTitle(R.string.strEnterUrl)
                        .setView(serverEdit).setPositiveButton(getString(R.string.strSave), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GlobalFunctions.servers.add(serverEdit.getText().toString());
                        GlobalFunctions.updateServerList(SourcesManagerActivity.this);
                        sourcesAdapter.notifyDataSetChanged();
                    }
                }).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((ListView) findViewById(R.id.sourcesList)).setAdapter(sourcesAdapter);
    }

    private class SourcesAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return GlobalFunctions.servers.size();
        }

        @Override
        public Object getItem(int i) {
            return GlobalFunctions.servers.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            SourceViewHolder vh;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_source, null);
                assert view != null;
                vh = new SourceViewHolder(view);
                vh.itemSource.setText(GlobalFunctions.servers.get(i));
                vh.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GlobalFunctions.servers.remove(i);
                        GlobalFunctions.updateServerList(SourcesManagerActivity.this);
                        notifyDataSetChanged();
                    }
                });
                view.setTag(vh);
            }
            vh = (SourceViewHolder) view.getTag();
            vh.itemSource.setText(GlobalFunctions.servers.get(i));
            return view;
        }

        private class SourceViewHolder {
            TextView itemSource;
            ImageButton imageButton;

            SourceViewHolder(View view) {
                itemSource = (TextView)view.findViewById(R.id.itemSource);
                imageButton = (ImageButton)view.findViewById(R.id.imageButton);
            }
        }
    }

}
