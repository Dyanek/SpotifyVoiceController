package spotifyproject.nath.spotifyvoicecontroller;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
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
import java.util.Objects;

// Classe permettant d'éviter certaines redondances dans le code
class Tools implements OnDownloadCompleteListener
{
    private Context _current_context;

    static final String CLIENT_ID = "7c330b477151476e97eae3ee39758a3f";
    static final String REDIRECT_URI = "nath.spotifyproject.SpotifyVoiceController://callback";

    RequestQueue request_queue;

    SpotifyAppRemote spotify_app_remote;
    private String _access_token;
    private String _spotify_user_id;

    String get_access_token()
    {
        return _access_token;
    }

    void set_access_token(String access_token)
    {
        _access_token = access_token;
    }

    String get_spotify_user_id()
    {
        return _spotify_user_id;
    }

    void set_spotify_user_id(String spotify_user_id)
    {
        _spotify_user_id = spotify_user_id;
    }

    Tools(Context context)
    {
        _current_context = context;
        request_queue = Volley.newRequestQueue(_current_context);
    }

    Intent createOpenMicIntent()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk...");

        return intent;
    }

    // Méthode permettant de se connecter à l'API Spotify pour contrôler le lecteur (play , pause, skip, ...)
    void connectToSpotifyAppRemote(final Button button)
    {
        ConnectionParams connection_params =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.CONNECTOR.connect(_current_context, connection_params,
                new Connector.ConnectionListener()
                {
                    @Override
                    public void onConnected(SpotifyAppRemote p_spotify_app_remote)
                    {
                        spotify_app_remote = p_spotify_app_remote;
                        enableSpeechButtonClick(button);
                    }

                    @Override
                    public void onFailure(Throwable throwable)
                    {
                        Toast.makeText(_current_context.getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Rend le bouton disponible quand la connexion à l'API a été effectuée
    private void enableSpeechButtonClick(Button button)
    {
        button.setEnabled(true);
        button.setBackgroundResource(R.color.primary_btn_bg_color);
        button.setTextColor(_current_context.getColor(R.color.primary_btn_txt_color));
    }

    void disconnectSpotifyAppRemote()
    {
        SpotifyAppRemote.CONNECTOR.disconnect(spotify_app_remote);
    }

    // Méthode appelée à l'issue d'un appel à l'API Google
    void actionToDo(String[] words, @Nullable final ArrayList<Track> track_list, @Nullable final RecyclerView.Adapter rv_adapter,
                    String activity)
    {
        //Le premier permet de déterminer l'action à effectuer.
        switch (words[0])
        {
            // Permet de jouer un titre ou d'ajouter un titre dans la queue
            case "play":
            case "next":
                // Si la phrase n'est pas constituée que de "Play" ou de "Next", comme "Play Bohemian RHapsody"
                if (words.length > 1)
                {
                    StringBuilder string_builder = new StringBuilder();
                    string_builder.append("https://api.spotify.com/v1/search?type=track&limit=1&q=");

                    for (int i = 1; i <= words.length - 1; i++)
                        string_builder.append(words[i]).append("%20");

                    String url = string_builder.toString();

                    url = url.substring(0, url.length() - 3);

                    trackJsonRequest(url, words[0], track_list, rv_adapter, activity);
                }
                else if (words[0].equals("play"))
                    spotify_app_remote.getPlayerApi().resume();
                else
                    spotify_app_remote.getPlayerApi().skipNext();
                break;

            case "restart":
                spotify_app_remote.getPlayerApi().skipPrevious();
                break;

            case "previous":
                spotify_app_remote.getPlayerApi().skipPrevious();
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

            //Permet de créer une playlist
            case "create":
                createPlaylist(words);
                break;

            case "add":
                Toast.makeText(_current_context.getApplicationContext(), "Command not available in the current context", Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(_current_context.getApplicationContext(), "I didn't understand", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    // Permet de récupérer un titre demandé
    private void trackJsonRequest(String url, final String instruction, @Nullable final ArrayList<Track> track_list,
                                  @Nullable final RecyclerView.Adapter rv_adapter, final String activity)
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

                            // Si l'activity actuelle est l'historique alors ajoute le titre dans la liste
                            if(activity.equals("main"))
                            {
                                String name = response.getJSONObject("tracks").getJSONArray("items")
                                        .getJSONObject(0).getString("name");
                                String album = response.getJSONObject("tracks").getJSONArray("items")
                                        .getJSONObject(0).getJSONObject("album").getString("name");
                                String artist = response.getJSONObject("tracks").getJSONArray("items")
                                        .getJSONObject(0).getJSONObject("album").getJSONArray("artists").getJSONObject(0).getString("name");

                                Objects.requireNonNull(track_list).add(0, new Track(name, album, artist, uri));
                                Objects.requireNonNull(rv_adapter).notifyItemInserted(0);
                            }

                            if (instruction.equals("play"))
                                spotify_app_remote.getPlayerApi().play(uri);
                            else
                                spotify_app_remote.getPlayerApi().queue(uri);
                        }
                        catch (JSONException ex)
                        {
                            Toast.makeText(_current_context.getApplicationContext(), "Error while trying to get the track", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Toast.makeText(_current_context.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + _access_token);

                return headers;
            }
        };

        request_queue.add(request);
    }

    private void createPlaylist(String[] words)
    {
        StringBuilder string_builder = new StringBuilder();
        for (int i = 1; i < words.length; i++)
            string_builder.append(words[i]).append(" ");

        String playlist_name = string_builder.toString();

        CreatePlaylistAsync create_playlist_request = new CreatePlaylistAsync(_spotify_user_id, _access_token, playlist_name);
        create_playlist_request.setOnDownloadCompleteListener(this);
        create_playlist_request.execute();
    }

    @Override
    public void onDownloadComplete(Boolean is_successful, Integer request_code)
    {
        if (request_code == 1 && is_successful)
            Toast.makeText(_current_context.getApplicationContext(), "Playlist created successfully", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(_current_context.getApplicationContext(), "Error while creating the playlist", Toast.LENGTH_SHORT).show();
    }
}