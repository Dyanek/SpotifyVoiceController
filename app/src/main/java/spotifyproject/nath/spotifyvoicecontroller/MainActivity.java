package spotifyproject.nath.spotifyvoicecontroller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
{
    private static final int SPOTIFY_REQUEST_CODE = 1337;
    private static final int SPEECH_OUTPUT_REQUEST_CODE = 100;

    private RecyclerView.Adapter rv_adapter;
    private ArrayList<Track> track_list;

    Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tools = new Tools(getApplicationContext());

        Bundle bundle = getIntent().getExtras();

        if (bundle != null)
        {
            tools.set_access_token(bundle.getString("access_token"));
            tools.set_spotify_user_id(bundle.getString("user_id"));
        }

        RecyclerView rv_track_list = findViewById(R.id.rv_main_track_list);

        rv_track_list.setLayoutManager(new LinearLayoutManager(this));

        track_list = new ArrayList<>();

        rv_adapter = new TrackAdapter(track_list, true, tools);
        rv_track_list.setAdapter(rv_adapter);

        Button btn_open_microphone = findViewById(R.id.main_btn_open_mic);

        btn_open_microphone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openMicrophoneButtonPressed();
            }
        });

        BottomNavigationView bottom_navigation_view = findViewById(R.id.bottom_nav_view);

        bottom_navigation_view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.documentation:
                        Intent documentation_intent = new Intent(getApplicationContext(), DocumentationActivity.class);
                        documentation_intent.putExtra("access_token", tools.get_access_token());
                        documentation_intent.putExtra("user_id", tools.get_spotify_user_id());
                        startActivity(documentation_intent);
                        break;

                    case R.id.playlists:
                        Intent playlists_intent = new Intent(getApplicationContext(), PlaylistsActivity.class);
                        playlists_intent.putExtra("access_token", tools.get_access_token());
                        playlists_intent.putExtra("user_id", tools.get_spotify_user_id());
                        startActivity(playlists_intent);
                        break;
                }
                return false;
            }
        });

        if (tools.get_access_token() == null)
        {
            AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(Tools.CLIENT_ID,
                    AuthenticationResponse.Type.TOKEN, Tools.REDIRECT_URI);

            builder.setScopes(new String[]{"user-read-recently-played", "playlist-read-collaborative",
                    "playlist-read-private", "playlist-modify-public", "playlist-modify-private"});
            AuthenticationRequest request = builder.build();

            AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);
        }
        else
            getTracksHistory();

        tools.connectToSpotifyAppRemote(btn_open_microphone);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        tools.disconnectSpotifyAppRemote();
    }

    private void openMicrophoneButtonPressed()
    {
        try
        {
            startActivityForResult(tools.createOpenMicIntent(), SPEECH_OUTPUT_REQUEST_CODE);
        }
        catch (ActivityNotFoundException tim)
        {
            Toast.makeText(getApplicationContext(), "No voice input detected on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int request_code, int result_code, Intent intent)
    {
        super.onActivityResult(request_code, result_code, intent);

        switch (request_code)
        {
            case SPOTIFY_REQUEST_CODE:
                spotifyConnectionResult(result_code, intent);
                break;

            case SPEECH_OUTPUT_REQUEST_CODE:
                speechOutputResult(result_code, intent);
                break;
        }
    }

    private void getTracksHistory()
    {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://api.spotify.com/v1/me/player/recently-played?type=track&limit=10", null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            JSONArray tracks_array = response.getJSONArray("items");

                            for (int i = tracks_array.length() - 1; i >= 0; i--)
                            {
                                JSONObject track = (JSONObject) tracks_array.get(i);

                                String uri = track.getJSONObject("track").getString("uri");
                                String name = track.getJSONObject("track").getString("name");
                                String album = track.getJSONObject("track").getJSONObject("album").getString("name");
                                String artist = track.getJSONObject("track").getJSONObject("album").getJSONArray("artists").getJSONObject(0).getString("name");

                                track_list.add(0, new Track(name, album, artist, uri));
                            }

                            rv_adapter.notifyItemInserted(0);
                        }
                        catch (JSONException ex)
                        {
                            Toast.makeText(getApplicationContext(), "Error while trying to get the tracks history", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + tools.get_access_token());

                return headers;
            }
        };

        tools.request_queue.add(request);
    }

    private void speechOutputResult(int result_code, Intent intent)
    {
        if (result_code == RESULT_OK && intent != null)
        {
            String[] words = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0).split("\\s+");

            tools.actionToDo(words, track_list, rv_adapter, "main");
        }
        else
            Toast.makeText(getApplicationContext(), "No text said", Toast.LENGTH_SHORT).show();
    }

    private void getSpotifyUserId()
    {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://api.spotify.com/v1/me", null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            tools.set_spotify_user_id(response.getString("id"));
                        }
                        catch (JSONException ex)
                        {
                            Toast.makeText(getApplicationContext(), "Error while trying to get the user id.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + tools.get_access_token());

                return headers;
            }
        };

        tools.request_queue.add(request);
    }

    private void spotifyConnectionResult(int result_code, Intent intent)
    {
        AuthenticationResponse response = AuthenticationClient.getResponse(result_code, intent);

        switch (response.getType())
        {
            // Response was successful and contains auth token
            case TOKEN:
                tools.set_access_token(response.getAccessToken());
                getTracksHistory();
                getSpotifyUserId();

                Toast.makeText(getApplicationContext(), "Connected to Spotify", Toast.LENGTH_SHORT).show();
                break;

            // Auth flow returned an error
            case ERROR:
                Toast.makeText(getApplicationContext(), "Error while connecting to Spotify", Toast.LENGTH_SHORT).show();
                break;

            // Most likely auth flow was cancelled
            default:
                tools.set_access_token(response.getAccessToken());
                getTracksHistory();
                getSpotifyUserId();

                Toast.makeText(getApplicationContext(), "Handle other case", Toast.LENGTH_SHORT).show();
        }
    }
}