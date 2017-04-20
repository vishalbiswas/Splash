package vishal.chetan.splash.android;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.ServerList;

public class SourcesManagerActivity extends BaseActivity {
    private final SourcesAdapter sourcesAdapter = new SourcesAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sources_manager);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabNewSource);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modifySource(-1, "", "");
            }
        });
        final ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setDisplayHomeAsUpEnabled(true);

        RecyclerView sourcesList = (RecyclerView) findViewById(R.id.sourcesList);
        sourcesList.setLayoutManager(new LinearLayoutManager(this));
        sourcesList.setAdapter(sourcesAdapter);

        sourcesList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    class SourcesAdapter extends RecyclerView.Adapter<SourcesAdapter.SourceViewHolder> {
        @Override
        public SourceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SourceViewHolder(getLayoutInflater().inflate(R.layout.list_item_source, parent, false));
        }

        @Override
        public void onBindViewHolder(final SourceViewHolder holder, final int position) {
            final ServerList.SplashSource source = GlobalFunctions.servers.get(position);
            holder.sourceEnabled.setChecked(source.isEnabled());
            holder.sourceEnabled.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = holder.getAdapterPosition();
                    if (position >= 0) {
                        GlobalFunctions.servers.setDisabled(position, !isChecked);
                        if (isChecked) {
                            new GlobalFunctions.CheckSource(SourcesManagerActivity.this).execute(position);
                        }
                    }
                }
            });
            GlobalFunctions.servers.addListener(new ServerList.OnServerDisabledListener() {
                @Override
                public void onDisabled() {
                    holder.sourceEnabled.setChecked(source.isEnabled());
                }
            });
            holder.itemSourceName.setText(source.getName());
            holder.itemSourceUrl.setText(source.getUrl());
            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    modifySource(holder.getAdapterPosition(), source.getName(), source.getUrl());
                }
            });
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GlobalFunctions.servers.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getItemCount() {
            return GlobalFunctions.servers.size();
        }

        class SourceViewHolder extends RecyclerView.ViewHolder {
            final SwitchCompat sourceEnabled;
            final TextView itemSourceName;
            final TextView itemSourceUrl;
            final ImageButton editButton;
            final ImageButton deleteButton;

            SourceViewHolder(View view) {
                super(view);
                sourceEnabled = (SwitchCompat) view.findViewById(R.id.sourceEnabled);
                itemSourceName = (TextView) view.findViewById(R.id.itemSourceName);
                itemSourceUrl = (TextView) view.findViewById(R.id.itemSourceUrl);
                editButton = (ImageButton) view.findViewById(R.id.editButton);
                deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
            }
        }
    }

    private void modifySource(final int index, final CharSequence initialName, final CharSequence initialUrl) {
        final LinearLayout layout = new LinearLayout(SourcesManagerActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText nameEdit = new EditText(SourcesManagerActivity.this);
        nameEdit.setHint("Name");
        nameEdit.setText(initialName);
        layout.addView(nameEdit);

        final EditText serverEdit = new EditText(SourcesManagerActivity.this);
        serverEdit.setHint("URL");
        serverEdit.setText(initialUrl);
        layout.addView(serverEdit);

        new AlertDialog.Builder(SourcesManagerActivity.this).setTitle(R.string.strEnterUrl)
                .setView(layout).setPositiveButton(getString(R.string.strSave), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String name = nameEdit.getText().toString();
                final String url = serverEdit.getText().toString();
                if (!name.isEmpty() && !name.isEmpty()) {
                    int correctIndex = index;
                    ServerList.SplashSource source = new ServerList.SplashSource(name, url);
                    if (index == -1) {
                        correctIndex = GlobalFunctions.servers.size();
                        GlobalFunctions.servers.add(source);
                        sourcesAdapter.notifyItemInserted(correctIndex);
                    } else {
                        GlobalFunctions.servers.set(index, source);
                        sourcesAdapter.notifyItemChanged(index);
                    }
                    new GlobalFunctions.CheckSource(SourcesManagerActivity.this).execute(correctIndex);
                }
            }
        }).show();
    }

}
