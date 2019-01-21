
package com.example.isaia.playlistgenerator;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.example.isaia.playlistgenerator.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Recommendations;
import kaaes.spotify.webapi.android.models.SavedAlbum;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GenerateActivity extends AppCompatActivity {
    private SeekBar mPopularityBar;
    private SeekBar mSimilarityBar;
    private EditText mSimilarityText;
    private EditText mPopularityText;
    private Button mGenerateButton;
    private String mAccessToken;
    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify;
    private String userId = "";
    private Playlist ret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        Intent i = getIntent();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mAccessToken = i.getStringExtra("ACCESS_TOKEN");
        api.setAccessToken(mAccessToken);
        spotify = api.getService();
        mPopularityBar = (SeekBar)findViewById(R.id.popularity_bar);
        mPopularityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPopularityText.setText(String.format(getString(R.string.number), progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        mSimilarityBar = (SeekBar) findViewById(R.id.similarity_bar);
        mSimilarityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSimilarityText.setText(String.format(getString(R.string.number), progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        mPopularityText = (EditText)findViewById(R.id.popularity_text);
        mPopularityText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                {
                    mPopularityBar.setProgress(Integer.parseInt(s.toString().replaceAll("\\s", "").trim()));
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
        mSimilarityText = (EditText)findViewById(R.id.similarity_text);
        mSimilarityText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                {
                    mSimilarityBar.setProgress(Integer.parseInt(s.toString().replaceAll("\\s", "").trim()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mGenerateButton = (Button)findViewById(R.id.final_generate_button);
        mGenerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Recommendations r = generateRecommendations(mPopularityBar.getProgress(), mSimilarityBar.getProgress());
                ArrayList<Track> toPass = new ArrayList<Track>(r.tracks);
                Intent i = new Intent(GenerateActivity.this, ViewActivity.class);
                i.putParcelableArrayListExtra("TRACKS", toPass);
                startActivity(i);
            }
        });



    }

    private Recommendations generateRecommendations(int pop, int sim)
    {
        int tracksToUse = (sim-1)/20 + 1;
        Pager<PlaylistSimple> playlistSimplePager = spotify.getMyPlaylists();
        List<PlaylistSimple> allMyPlaylists = playlistSimplePager.items;
        ArrayList<String> allIds = new ArrayList<>();
        if(allMyPlaylists.size() > 0) {
            for(PlaylistSimple p: playlistSimplePager.items) {
                Playlist temp = getPlaylistFromSimple(p);
                for(PlaylistTrack t: temp.tracks.items)
                {
                    allIds.add(t.track.id);
                }
            }
        }
        ArrayList<String> idToUse = new ArrayList<>();
        Collections.shuffle(allIds); //randomize for more representative sample
        for(int i = 0; i < Math.min(tracksToUse, allIds.size()); i++)
        {
            idToUse.add(allIds.get(i)); //get tracks, list of tracks, individual track, track object, track id
        }
        HashMap<String, Object> recommendMap = new HashMap<>();
        recommendMap.put("seed_tracks", TextUtils.join(",", idToUse));
        recommendMap.put("target_popularity", pop);
        Recommendations recommendations = spotify.getRecommendations(recommendMap);
        return recommendations;
    }

    private Playlist getPlaylistFromSimple(PlaylistSimple in)
    {
        Playlist ret;
        if(userId.equals("")) {
            userId = spotify.getMe().id;
        }
        ret = spotify.getPlaylist(userId, in.id);
        return ret;
    }
}
