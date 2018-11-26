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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlaylistTracksActivity extends AppCompatActivity implements OnDownloadCompleteListener
{
    private static final int SPEECH_OUTPUT_REQUEST_CODE = 100;

    private RecyclerView.Adapter rv_adapter;
    private ArrayList<Track> track_list;

    private String playlist_name;
    private String playlist_id;
    private Integer playlist_size;

    private Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_tracks);

        tools = new Tools(getApplicationContext());

        Bundle bundle = getIntent().getExtras();

        if (bundle != null)
        {
            tools.set_access_token(bundle.getString("access_token"));
            tools.set_spotify_user_id(bundle.getString("user_id"));
            playlist_name = bundle.getString("playlist_name");
            playlist_id = bundle.getString("playlist_id");
            playlist_size = bundle.getInt("playlist_size");
        }

        setTitle(playlist_name);

        RecyclerView rv_track_list = findViewById(R.id.rv_playlist_track_list);

        rv_track_list.setLayoutManager(new LinearLayoutManager(this));

        track_list = new ArrayList<>();

        rv_adapter = new TrackAdapter(track_list, false, tools);
        rv_track_list.setAdapter(rv_adapter);

        final Button btn_open_microphone = findViewById(R.id.playlist_tracks_btn_open_mic);

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
                    case R.id.history:
                        Intent history_intent = new Intent(getApplicationContext(), MainActivity.class);
                        history_intent.putExtra("access_token", tools.get_access_token());
                        history_intent.putExtra("user_id", tools.get_spotify_user_id());
                        startActivity(history_intent);
                        break;

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

        if(playlist_size != 0)
            getPlaylistTracks();

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
            case SPEECH_OUTPUT_REQUEST_CODE:
                speechOutputResult(result_code, intent);
                break;
        }
    }

    private void getPlaylistTracks()
    {
        track_list.clear();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://api.spotify.com/v1/playlists/"
                + playlist_id + "/tracks?limit=" + playlist_size, null,
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
                            Toast.makeText(getApplicationContext(), "Error while trying to get the playlist tracks", Toast.LENGTH_SHORT).show();
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

            /* Si le mot est égal à "Add", permet d'ajouter un titre dans la playlist, sinon c'est une autre
               commande qui est utilisée */
            if(words[0].equals("add"))
            {
                if (words.length > 1)
                {
                    StringBuilder string_builder = new StringBuilder();
                    string_builder.append("https://api.spotify.com/v1/search?type=track&limit=1&q=");

                    for (int i = 1; i <= words.length - 1; i++)
                        string_builder.append(words[i]).append("%20");

                    String url = string_builder.toString();

                    url = url.substring(0, url.length() - 3);

                    trackJsonRequest(url);
                }
                else
                    Toast.makeText(getApplicationContext(), "You must say a title after saying \"Add\"", Toast.LENGTH_SHORT).show();
            }
            else
                tools.actionToDo(words, null, null, "other");
        }
        else
            Toast.makeText(getApplicationContext(), "No text said", Toast.LENGTH_SHORT).show();
    }

    private void trackJsonRequest(String url)
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

                            addTrackToPlaylist(uri);
                        }
                        catch (JSONException ex)
                        {
                            Toast.makeText(getApplicationContext(), "Error while trying to get the track", Toast.LENGTH_SHORT).show();
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

    private void addTrackToPlaylist(String track_uri)
    {
        playlist_size++;
        AddTrackToPlaylistAsync add_track = new AddTrackToPlaylistAsync(playlist_id, track_uri, tools.get_access_token());
        add_track.setOnDownloadCompleteListener(this);
        add_track.execute();
    }

    @Override
    public void onDownloadComplete(Boolean is_successful, Integer request_code)
    {
        if (request_code == 1 && is_successful)
        {
            getPlaylistTracks();
            Toast.makeText(getApplicationContext(), "Action performed with success", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Error while trying to perform the action", Toast.LENGTH_SHORT).show();
    }
}