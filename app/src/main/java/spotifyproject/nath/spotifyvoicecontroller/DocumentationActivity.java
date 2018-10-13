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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DocumentationActivity extends AppCompatActivity implements OnDownloadCompleteListener
{
    private final int SPEECH_OUTPUT_REQUEST_CODE = 100;

    private String access_token;
    private RequestQueue request_queue;
    private SpotifyAppRemote spotify_app_remote;
    private String spotify_user_id;

    private Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null)
        {
            access_token = bundle.getString("access_token");
            spotify_user_id = bundle.getString("user_id");
        }

        tools = new Tools(getApplicationContext());

        request_queue = Volley.newRequestQueue(this);

        final Button btn_open_microphone = findViewById(R.id.doc_btn_open_mic);

        btn_open_microphone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openMicrophoneButtonPressed();
            }
        });

        RecyclerView rv_command_list = findViewById(R.id.rv_command_list);

        rv_command_list.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView.Adapter rv_adapter = new CommandAdapter(setCommandList());
        rv_command_list.setAdapter(rv_adapter);


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
                        historic_intent.putExtra("access_token", access_token);
                        historic_intent.putExtra("user_id", spotify_user_id);
                        startActivity(historic_intent);
                        break;

                    case R.id.playlists:
                        Intent playlists_intent = new Intent(getApplicationContext(), PlaylistsActivity.class);
                        playlists_intent.putExtra("access_token", access_token);
                        playlists_intent.putExtra("user_id", spotify_user_id);
                        startActivity(playlists_intent);
                        break;
                }
                return false;
            }
        });

        ConnectionParams connection_params =
                new ConnectionParams.Builder(MainActivity.CLIENT_ID)
                        .setRedirectUri(MainActivity.REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.CONNECTOR.connect(this, connection_params,
                new Connector.ConnectionListener()
                {
                    @Override
                    public void onConnected(SpotifyAppRemote p_spotify_app_remote)
                    {
                        spotify_app_remote = p_spotify_app_remote;
                        tools.enableSpeechButtonClick(btn_open_microphone);
                    }

                    @Override
                    public void onFailure(Throwable throwable)
                    {
                        Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        SpotifyAppRemote.CONNECTOR.disconnect(spotify_app_remote);
    }

    private void openMicrophoneButtonPressed()
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

            switch (words[0])
            {
                case "play":
                case "next":
                    if (words.length > 1)
                    {
                        StringBuilder string_builder = new StringBuilder();
                        string_builder.append("https://api.spotify.com/v1/search?type=track&limit=1&q=");

                        for (int i = 1; i <= words.length - 1; i++)
                            string_builder.append(words[i]).append("%20");

                        String url = string_builder.toString();

                        url = url.substring(0, url.length() - 3);

                        trackJsonRequest(url, words[0]);
                    }
                    else if (words[0].equals("play"))
                        spotify_app_remote.getPlayerApi().resume();
                    else
                        spotify_app_remote.getPlayerApi().skipNext();
                    break;

                case "previous":
                    spotify_app_remote.getPlayerApi().skipPrevious();
                    break;

                case "skip":
                    spotify_app_remote.getPlayerApi().skipNext();
                    break;

                case "pause":
                case "stop":
                    spotify_app_remote.getPlayerApi().pause();
                    break;

                case "resume":
                    spotify_app_remote.getPlayerApi().resume();
                    break;

                case "create":
                    createPlaylist(words);
                    break;
            }
        }
        else
            Toast.makeText(getApplicationContext(), "No text said", Toast.LENGTH_SHORT).show();
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

                            if (instruction.equals("play"))
                                spotify_app_remote.getPlayerApi().play(uri);
                            else
                                spotify_app_remote.getPlayerApi().queue(uri);
                        }
                        catch (JSONException ex)
                        {
                            Toast.makeText(getApplicationContext(), "Erreur lors de la récupération de l'objet", Toast.LENGTH_SHORT).show();
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
                headers.put("Authorization", "Bearer " + access_token);

                return headers;
            }
        };

        request_queue.add(request);
    }

    private ArrayList<Command> setCommandList()
    {
        ArrayList<Command> command_list = new ArrayList<>();

        command_list.add(new Command("Play", "To play a song, say \"Play\" followed by the title of the song."));
        command_list.add(new Command("Add a track to the queue", "To put a song in the play queue, say \"Next\" followed by the title of the song."));
        command_list.add(new Command("Skip", "To skip a song, say \"Skip\" or \"Next\"."));
        command_list.add(new Command("Previous", "To go to the previous song, say \"Previous\"."));
        command_list.add(new Command("Pause", "To pause a song, say \"Pause\" or \"Stop\"."));
        command_list.add(new Command("Resume", "To resume a song, say \"Resume\"."));
        command_list.add(new Command("Create playlist", "To create a playlist, say \"Create\" followed by the playlist name."));

        return command_list;
    }

    void createPlaylist(String[] words)
    {
        StringBuilder string_builder = new StringBuilder();
        for(int i = 1; i < words.length; i++)
            string_builder.append(words[i]).append(" ");

        String playlist_name = string_builder.toString();

        CreatePlaylistAsync create_playlist_request = new CreatePlaylistAsync(spotify_user_id, access_token, playlist_name);
        create_playlist_request.setOnDownloadCompleteListener(this);
        create_playlist_request.execute();
    }

    @Override
    public void onDownloadComplete(Boolean is_successful, Integer request_code)
    {
        if (request_code == 1 && is_successful)
            Toast.makeText(getApplicationContext(), "Playlist created successfully", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Error while creating the playlist", Toast.LENGTH_SHORT).show();
    }
}