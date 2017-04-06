package vishal.chetan.splash.android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
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

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.ServerList;

public class SourcesManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sources_manager);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        final SourcesAdapter sourcesAdapter = new SourcesAdapter();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabNewSource);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layout = new LinearLayout(SourcesManagerActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText nameEdit = new EditText(SourcesManagerActivity.this);
                nameEdit.setHint("Name");
                layout.addView(nameEdit);

                final EditText serverEdit = new EditText(SourcesManagerActivity.this);
                serverEdit.setHint("URL");
                layout.addView(serverEdit);
                new AlertDialog.Builder(SourcesManagerActivity.this).setTitle(R.string.strEnterUrl)
                        .setView(layout).setPositiveButton(getString(R.string.strSave), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = nameEdit.getText().toString();
                        String url = serverEdit.getText().toString();
                        if (!name.isEmpty() && !url.isEmpty()) {
                            GlobalFunctions.servers.add(new ServerList.SplashSource(name, url));
                            sourcesAdapter.notifyDataSetChanged();
                        }
                    }
                }).show();
            }
        });
        ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setDisplayHomeAsUpEnabled(true);

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
        public View getView(final int index, View view, ViewGroup viewGroup) {
            SourceViewHolder vh;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_source, null);
                assert view != null;
                vh = new SourceViewHolder(view);
                vh.itemSourceName.setText(GlobalFunctions.servers.get(index).getName());
                vh.itemSourceUrl.setText(GlobalFunctions.servers.get(index).getUrl());
                view.findViewById(R.id.itemSource).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ServerList.SplashSource source = GlobalFunctions.servers.get(index);
                        LinearLayout layout = new LinearLayout(SourcesManagerActivity.this);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        final EditText nameEdit = new EditText(SourcesManagerActivity.this);
                        nameEdit.setHint("Name");
                        nameEdit.setText(source.getName());
                        layout.addView(nameEdit);

                        final EditText serverEdit = new EditText(SourcesManagerActivity.this);
                        serverEdit.setHint("URL");
                        serverEdit.setText(source.getUrl());
                        layout.addView(serverEdit);

                        new AlertDialog.Builder(SourcesManagerActivity.this).setTitle(R.string.strEnterUrl)
                                .setView(layout).setPositiveButton(getString(R.string.strSave), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = nameEdit.getText().toString();
                                String url = serverEdit.getText().toString();
                                if (!name.isEmpty() && !name.isEmpty()) {
                                    source.setName(name);
                                    source.setUrl(url);
                                    notifyDataSetChanged();
                                }
                            }
                        }).show();
                    }
                });
                vh.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GlobalFunctions.servers.remove(index);
                        notifyDataSetChanged();
                    }
                });
                view.setTag(vh);
            }
            vh = (SourceViewHolder) view.getTag();
            vh.itemSourceName.setText(GlobalFunctions.servers.get(index).getName());
            vh.itemSourceUrl.setText(GlobalFunctions.servers.get(index).getUrl());
            return view;
        }

        private class SourceViewHolder {
            final TextView itemSourceName;
            final TextView itemSourceUrl;
            final ImageButton imageButton;

            SourceViewHolder(View view) {
                itemSourceName = (TextView)view.findViewById(R.id.itemSourceName);
                itemSourceUrl = (TextView)view.findViewById(R.id.itemSourceUrl);
                imageButton = (ImageButton)view.findViewById(R.id.imageButton);
            }
        }
    }

}
