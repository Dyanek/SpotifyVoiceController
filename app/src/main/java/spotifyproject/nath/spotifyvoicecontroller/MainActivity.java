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

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
{
    private static final String CLIENT_ID = "7c330b477151476e97eae3ee39758a3f";
    private static final String REDIRECT_URI = "nath.spotifyproject.SpotifyVoiceController://callback";
    private static final int SPOTIFY_REQUEST_CODE = 1337;
    public static SpotifyAppRemote spotifyAppRemote;

    public static RequestQueue requestQueue;

    private String accessToken;

    private final int SPEECH_OUTPUT_REQUEST_CODE = 100;
    private Button btnOpenMicrophone;

    private RecyclerView rvTrackList;
    private RecyclerView.Adapter rvAdapter;
    private RecyclerView.LayoutManager rvLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        rvTrackList = findViewById(R.id.rvTrackList);

        rvLayoutManager = new LinearLayoutManager(this);
        rvTrackList.setLayoutManager(rvLayoutManager);

        rvAdapter = new TrackAdapter(new String[] { "Ceci est", "Un test pour", "Le RecyclerView"});
        rvTrackList.setAdapter(rvAdapter);

        btnOpenMicrophone = findViewById(R.id.btnOpenMic);

        btnOpenMicrophone.setOnClickListener(new View.OnClickListener()
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

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.CONNECTOR.connect(this, connectionParams,
                new Connector.ConnectionListener()
                {
                    @Override
                    public void onConnected(SpotifyAppRemote mSpotifyAppRemote)
                    {
                        spotifyAppRemote = mSpotifyAppRemote;

                        enableSpeechButton();
                    }

                    @Override
                    public void onFailure(Throwable throwable)
                    {
                        Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void enableSpeechButton()
    {
        btnOpenMicrophone.setEnabled(true);
        btnOpenMicrophone.setBackgroundResource(R.color.colorButtonBackground);
        btnOpenMicrophone.setTextColor(getColor(R.color.colorButtonText));
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        SpotifyAppRemote.CONNECTOR.disconnect(spotifyAppRemote);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        switch(requestCode)
        {
            case SPOTIFY_REQUEST_CODE:
            {
                SpotifyConnectionResult(resultCode, intent);

                break;
            }

            case SPEECH_OUTPUT_REQUEST_CODE:
            {
                SpeechOutputResult(resultCode, intent);

                break;
            }
        }
    }

    private void SpeechOutputResult(int resultCode, Intent intent)
    {
        if(resultCode == RESULT_OK && intent != null)
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
            if(words[0].equals("skip"))
                spotifyAppRemote.getPlayerApi().skipNext();
        }
        else
            Toast.makeText(this, "No text said", Toast.LENGTH_SHORT).show();
    }

    private void trackJsonRequest(String url, final String instruction)
    {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            String uri = response.getJSONObject("tracks").getJSONArray("items")
                                    .getJSONObject(0).getString("uri");

                            if(instruction.equals("play"))
                                spotifyAppRemote.getPlayerApi().play(uri);
                            else
                                spotifyAppRemote.getPlayerApi().queue(uri);
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
                headers.put("Authorization", "Bearer " + accessToken);

                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void SpotifyConnectionResult(int resultCode, Intent intent)
    {
        AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

        switch (response.getType())
        {
            // Response was successful and contains auth token
            case TOKEN:
                // Handle successful response
                Toast.makeText(getApplicationContext(), "Connected to Spotify", Toast.LENGTH_SHORT).show();

                accessToken = response.getAccessToken();
                break;

            // Auth flow returned an error
            case ERROR:
                // Handle error response
                Toast.makeText(getApplicationContext(), "Error while connecting to Spotify", Toast.LENGTH_SHORT).show();
                break;

            // Most likely auth flow was cancelled
            default:
                // Handle other cases
                accessToken = response.getAccessToken();

                Toast.makeText(getApplicationContext(), "Handle other case", Toast.LENGTH_SHORT).show();
        }
    }
}