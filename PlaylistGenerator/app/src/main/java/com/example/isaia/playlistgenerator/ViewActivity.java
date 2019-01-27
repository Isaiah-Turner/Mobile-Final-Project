package com.example.isaia.playlistgenerator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class ViewActivity extends AppCompatActivity  implements MyRecyclerViewAdapter.ItemClickListener{
    private RecyclerView mRecycler;
    private MyRecyclerViewAdapter adapter;
    private Button mSaveButton;
    private EditText mNameText;
    ArrayList<Track> mSongList;
    private String mAccessToken;
    private CheckBox mPublic;
    private String[] mURIs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        Intent i = getIntent();
        mSongList = i.getParcelableArrayListExtra("TRACKS");
        mAccessToken = i.getStringExtra("ACCESS_TOKEN");
        ArrayList<String> songStrings = new ArrayList<>();
        mURIs = new String[mSongList.size()];
        int index = 0;
        for (Track t : mSongList) {
            mURIs[index++] = t.uri;
            ArrayList<String> artistString = new ArrayList<>();
            for(ArtistSimple a: t.artists)
                artistString.add(a.name);
            songStrings.add(t.name + " by " + TextUtils.join(", ", artistString));
        }

        mRecycler = (RecyclerView) findViewById(R.id.song_text_list);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, songStrings);
        adapter.setClickListener(this);
        mRecycler.setAdapter(adapter);
        mSaveButton = (Button)findViewById(R.id.save_button);
        mNameText = (EditText)findViewById(R.id.name_text);
        mPublic = (CheckBox)findViewById(R.id.public_checkbox);

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNameText.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(), "Please enter a playlist name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SpotifyApi api = new SpotifyApi();
                    api.setAccessToken(mAccessToken);

                    SpotifyService spotify = api.getService();
                    HashMap<String, Object> playlistMap = new HashMap<>();
                    playlistMap.put("name", mNameText.getText().toString());
                    playlistMap.put("public", mPublic.isChecked());
                    UserPrivate user = spotify.getMe();
                    Playlist createdPlaylist = spotify.createPlaylist(user.id, playlistMap);
                    HashMap<String, Object> queryParam = new HashMap<>();
                    //queryParam.put("position", "0");
                    HashMap<String, Object> bodyParam = new HashMap<>();
                    bodyParam.put("uris", mURIs);
                    spotify.addTracksToPlaylist(user.id, createdPlaylist.id, queryParam, bodyParam);
                    Toast.makeText(getApplicationContext(), "Playlist Created", Toast.LENGTH_SHORT).show();
                    mSaveButton.setClickable(false);
                }
            }
        });
    }
    @Override
    public void onItemClick(View view, int position)
    {
        int min = (int)((mSongList.get(position).duration_ms/1000) / 60);
        int sec = (int)(mSongList.get(position).duration_ms/1000) % 60;
        String duration = String.format("%d:%02d", min, sec);
        Toast.makeText(this, adapter.getItem(position) + " - popularity: " +
                mSongList.get(position).popularity + " - duration: " + duration, Toast.LENGTH_SHORT).show();
    }
}
