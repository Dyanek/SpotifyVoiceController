package spotifyproject.nath.spotifyvoicecontroller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
{
    private static final String CLIENT_ID = "7c330b477151476e97eae3ee39758a3f";
    private static final String REDIRECT_URI = "nath.spotifyproject.SpotifyVoiceController://callback";
    private static final int SPOTIFY_REQUEST_CODE = 1337;
    public static SpotifyAppRemote spotify_app_remote;
    public static String spotify_user_id;

    public static RequestQueue request_queue;

    public String access_token;

    private final int SPEECH_OUTPUT_REQUEST_CODE = 100;
    private Button btn_open_microphone;

    private RecyclerView rv_track_list;
    private RecyclerView.Adapter rv_adapter;
    private RecyclerView.LayoutManager rv_layout_manager;

    private ArrayList<Track> track_list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        request_queue = Volley.newRequestQueue(this);

        rv_track_list = findViewById(R.id.rv_track_list);

        rv_layout_manager = new LinearLayoutManager(this);
        rv_track_list.setLayoutManager(rv_layout_manager);

        track_list = new ArrayList<>();

        rv_adapter = new TrackAdapter(track_list);
        rv_track_list.setAdapter(rv_adapter);

        btn_open_microphone = findViewById(R.id.btn_open_mic);

        btn_open_microphone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenMicrophoneButtonPressed();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[] { "user-read-recently-played", "playlist-read-collaborative", "playlist-read-private" });
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);

        ConnectionParams connection_params =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.CONNECTOR.connect(this, connection_params,
                new Connector.ConnectionListener()
                {
                    @Override
                    public void onConnected(SpotifyAppRemote p_spotify_app_remote)
                    {
                        spotify_app_remote = p_spotify_app_remote;

                        enableSpeechButtonClick();
                    }

                    @Override
                    public void onFailure(Throwable throwable)
                    {
                        Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void enableSpeechButtonClick()
    {
        btn_open_microphone.setEnabled(true);
        btn_open_microphone.setBackgroundResource(R.color.colorButtonBackground);
        btn_open_microphone.setTextColor(getColor(R.color.colorButtonText));
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        SpotifyAppRemote.CONNECTOR.disconnect(spotify_app_remote);
    }

    private void OpenMicrophoneButtonPressed()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk...");

        try
        {
            startActivityForResult(intent, SPEECH_OUTPUT_REQUEST_CODE);
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

        switch(request_code)
        {
            case SPOTIFY_REQUEST_CODE:
            {
                spotifyConnectionResult(result_code, intent);
                break;
            }

            case SPEECH_OUTPUT_REQUEST_CODE:
            {
                speechOutputResult(result_code, intent);
                break;
            }
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

                            for(int i = tracks_array.length()-1; i >= 0; i--)
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
                            Toast.makeText(MainActivity.this, "Erreur lors de la récupération de l'objet", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + access_token);

                return headers;
            }
        };

        request_queue.add(request);
    }

    private void speechOutputResult(int result_code, Intent intent)
    {
        if(result_code == RESULT_OK && intent != null)
        {
            String[] words = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0).split("\\s+");

            if(words[0].equals("play") || words[0].equals("next"))
            {
                String url = "https://api.spotify.com/v1/search?type=track&limit=1&q=";

                for(int i = 1; i <= words.length-1; i++)
                    url += words[i] + "%20";

                url = url.substring(0, url.length()-3);

                trackJsonRequest(url, words[0]);
            }
            else if(words[0].equals("skip"))
                spotify_app_remote.getPlayerApi().skipNext();
            else if(words[0].equals("pause") || words[0].equals("stop"))
                spotify_app_remote.getPlayerApi().pause();
            else if(words[0].equals("resume"))
                spotify_app_remote.getPlayerApi().resume();
        }
        else
            Toast.makeText(this, "No text said", Toast.LENGTH_SHORT).show();
    }

    private void trackJsonRequest(String url, final String instruction)
    {
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            String uri = response.getJSONObject("tracks").getJSONArray("items")
                                    .getJSONObject(0).getString("uri");

                            String name = response.getJSONObject("tracks").getJSONArray("items")
                                    .getJSONObject(0).getString("name");

                            String album = response.getJSONObject("tracks").getJSONArray("items")
                                    .getJSONObject(0).getJSONObject("album").getString("name");

                            String artist = response.getJSONObject("tracks").getJSONArray("items")
                                    .getJSONObject(0).getJSONObject("album").getJSONArray("artists").getJSONObject(0).getString("name");

                            track_list.add(0, new Track(name, album, artist, uri));
                            rv_adapter.notifyItemInserted(0);

                            if(instruction.equals("play"))
                                spotify_app_remote.getPlayerApi().play(uri);
                            else
                                spotify_app_remote.getPlayerApi().queue(uri);
                        }
                        catch (JSONException ex)
                        {
                            Toast.makeText(MainActivity.this, "Erreur lors de la récupération de l'objet", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + access_token);

                return headers;
            }
        };

        request_queue.add(request);
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
                            spotify_user_id = response.getString("id");
                        }
                        catch (JSONException ex)
                        {
                            Toast.makeText(MainActivity.this, "Erreur lors de la récupération de l'objet", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + access_token);

                return headers;
            }
        };

        request_queue.add(request);
    }

    private void spotifyConnectionResult(int result_code, Intent intent)
    {
        AuthenticationResponse response = AuthenticationClient.getResponse(result_code, intent);

        switch (response.getType())
        {
            // Response was successful and contains auth token
            case TOKEN:
                access_token = response.getAccessToken();
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
                access_token = response.getAccessToken();
                getTracksHistory();
                getSpotifyUserId();

                Toast.makeText(getApplicationContext(), "Handle other case", Toast.LENGTH_SHORT).show();
        }
    }
}