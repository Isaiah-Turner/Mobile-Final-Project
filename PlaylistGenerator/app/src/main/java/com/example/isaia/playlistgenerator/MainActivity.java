package com.example.isaia.playlistgenerator;

import android.content.Intent;
import android.os.StrictMode;
import android.os.Trace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;


import java.io.IOException;
import java.net.URI;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {
    private static final String CLIENT_ID = "c7c395c23d4d45f6adb7dc8dab207f5d";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";
    private static final String CLIENT_SECRET = "5ae0c07d51a840c698828b9327a4ce81";
    private SpotifyAppRemote mSpotifyAppRemote;
    private final int REQUEST_CODE = 1337;
    private static String accessToken = "";

    private Button mGenerate;
    private Button mView;
    private TextView mGreeting;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getSupportActionBar().hide();

    }
    @Override
    protected void onStart(){
        super.onStart();
        //Web service authentication and user login
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming", "playlist-read-private", "playlist-modify-private", "playlist-modify-public"});
        builder.setShowDialog(true);
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);



    }
    private void authenticated(){

        SpotifyApi api = new SpotifyApi();
        api.setAccessToken(accessToken);

        SpotifyService spotify = api.getService();
        UserPrivate temp = spotify.getMe();
        mGreeting = (TextView)findViewById(R.id.greeting_text);
        mGreeting.setText(String.format(getString(R.string.greeting), temp.display_name ));
        mGenerate = (Button)findViewById(R.id.generate_button);
        mGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, GenerateActivity.class);
                i.putExtra("ACCESS_TOKEN", accessToken);
                startActivity(i);
            }
        });

        spotify.getAlbum("2dIGnmEIy1WZIcZCFSj6i8", new Callback<Album>() {
            @Override
            public void success(Album album, Response response) {
                Log.d("Album success", album.name);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Album failure", error.toString());
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode,resultCode, intent);
        if(requestCode == REQUEST_CODE){
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType())
            {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    accessToken = response.getAccessToken();
                    authenticated();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.e("onActivityResult", "Error in authentication");
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }
}

