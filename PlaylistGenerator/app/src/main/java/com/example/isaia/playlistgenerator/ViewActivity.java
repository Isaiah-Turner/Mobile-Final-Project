package com.example.isaia.playlistgenerator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

public class ViewActivity extends AppCompatActivity  implements MyRecyclerViewAdapter.ItemClickListener{
    private RecyclerView mSongList;
    private MyRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Intent i = getIntent();
        ArrayList<Track> songList = i.getParcelableArrayListExtra("TRACKS");
        ArrayList<String> songStrings = new ArrayList<>();
        for (Track t : songList) {
            ArrayList<String> artistString = new ArrayList<>();
            for(ArtistSimple a: t.artists)
                artistString.add(a.name);
            songStrings.add(t.name + " by " + TextUtils.join(", ", artistString));
        }
        mSongList = (RecyclerView) findViewById(R.id.song_text_list);
        mSongList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, songStrings);
        adapter.setClickListener(this);
        mSongList.setAdapter(adapter);
    }
    @Override
    public void onItemClick(View view, int position)
    {
        Toast.makeText(this, "You clicked " + adapter.getItem(position), Toast.LENGTH_SHORT).show();
    }
}
