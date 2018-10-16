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

public class PlaylistsActivity extends AppCompatActivity
{
    private static final int SPEECH_OUTPUT_REQUEST_CODE = 100;

    private RecyclerView.Adapter rv_adapter;
    private ArrayList<Playlist> playlist_list;

    private Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        tools = new Tools(getApplicationContext());

        Bundle bundle = getIntent().getExtras();

        if (bundle != null)
        {
            tools.set_access_token(bundle.getString("access_token"));
            tools.set_spotify_user_id(bundle.getString("user_id"));
        }

        RecyclerView rv_track_list = findViewById(R.id.rv_playlist_list);

        rv_track_list.setLayoutManager(new LinearLayoutManager(this));

        playlist_list = new ArrayList<>();

        rv_adapter = new PlaylistAdapter(playlist_list, tools.get_access_token(), tools.get_spotify_user_id());
        rv_track_list.setAdapter(rv_adapter);

        final Button btn_open_microphone = findViewById(R.id.playlists_btn_open_mic);

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
                    case R.id.historic:
                        Intent historic_intent = new Intent(getApplicationContext(), MainActivity.class);
                        historic_intent.putExtra("access_token", tools.get_access_token());
                        historic_intent.putExtra("user_id", tools.get_spotify_user_id());
                        startActivity(historic_intent);
                        break;

                    case R.id.documentation:
                        Intent documentation_intent = new Intent(getApplicationContext(), DocumentationActivity.class);
                        documentation_intent.putExtra("access_token", tools.get_access_token());
                        documentation_intent.putExtra("user_id", tools.get_spotify_user_id());
                        startActivity(documentation_intent);
                        break;
                }
                return false;
            }
        });

        getUserPlaylists();

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

    private void speechOutputResult(int result_code, Intent intent)
    {
        if (result_code == RESULT_OK && intent != null)
        {
            String[] words = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0).split("\\s+");

            tools.actionToDo(words, null, null, "other");
        }
        else
            Toast.makeText(getApplicationContext(), "No text said", Toast.LENGTH_SHORT).show();
    }

    private void getUserPlaylists()
    {
        playlist_list.clear();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "https://api.spotify.com/v1/users/" + tools.get_spotify_user_id() + "/playlists", null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            JSONArray playlists_array = response.getJSONArray("items");

                            for (int i = playlists_array.length() - 1; i >= 0; i--)
                            {
                                JSONObject playlist = (JSONObject) playlists_array.get(i);

                                String uri = playlist.getString("uri");
                                String id = playlist.getString("id");
                                String name = playlist.getString("name");
                                String author = playlist.getJSONObject("owner").getString("display_name");
                                Integer size = playlist.getJSONObject("tracks").getInt("total");

                                playlist_list.add(0, new Playlist(name, author, uri, id, size));
                            }

                            rv_adapter.notifyItemInserted(0);
                        }
                        catch (JSONException ex)
                        {
                            Toast.makeText(getApplicationContext(), "Error while trying to get the user's playlists", Toast.LENGTH_SHORT).show();
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
}