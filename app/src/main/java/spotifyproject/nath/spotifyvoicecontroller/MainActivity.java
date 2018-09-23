package spotifyproject.nath.spotifyvoicecontroller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnDownloadCompleteListener
{
    private static final String CLIENT_ID = "7c330b477151476e97eae3ee39758a3f";
    private static final String REDIRECT_URI = "nath.spotifyproject.SpotifyVoiceController://callback";
    private static final int SPOTIFY_REQUEST_CODE = 1337;
    public static SpotifyAppRemote mSpotifyAppRemote;

    private String accessToken;

    private final int SPEECH_OUPUT_REQUEST_CODE = 100;
    private Button btnOpenMicrophone;

    private TextView txtHomePage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenMicrophone = findViewById(R.id.btnOpenMic);

        btnOpenMicrophone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                OpenMicrophoneButtonPressed();
            }
        });

        txtHomePage = findViewById(R.id.txtHomePage);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);

        //-------------------------------------------------------------------------------------------
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.CONNECTOR.connect(this, connectionParams,
                new Connector.ConnectionListener()
                {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote)
                    {
                        mSpotifyAppRemote = spotifyAppRemote;

                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable)
                    {
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                    }
                });
    }

    public void connected()
    {

    }

    @Override
    protected void onStop()
    {
        super.onStop();

        SpotifyAppRemote.CONNECTOR.disconnect(mSpotifyAppRemote);
    }

    private void OpenMicrophoneButtonPressed()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk...");

        try
        {
            startActivityForResult(intent, SPEECH_OUPUT_REQUEST_CODE);
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

        // Spotify auth check
        if (requestCode == SPOTIFY_REQUEST_CODE)
        {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType())
            {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    Toast.makeText(getApplicationContext(), "Handle successful", Toast.LENGTH_LONG).show();

                    accessToken = response.getAccessToken();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Toast.makeText(getApplicationContext(), "Handle error", Toast.LENGTH_LONG).show();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    Toast.makeText(getApplicationContext(), "Handle other case", Toast.LENGTH_LONG).show();
            }
        }

        //Voice input check
        if (requestCode == SPEECH_OUPUT_REQUEST_CODE)
        {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String url = "https://api.spotify.com/v1/search";
            //String url = "http://www.google.com";

            //JSONObject authorizationJSON = new JSONObject();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            txtHomePage.setText("Response: " + response);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    });

            requestQueue.add(jsonObjectRequest);

            /*WebService webService = new WebService(getApplicationContext(), songSearch);
            webService.setOnDownloadCompleteListener(this);
            webService.execute();*/
        }
    }

    @Override
    public void onDownloadComplete(String content, Integer requestCode)
    {
        try
        {
            Gson gson = new Gson();
            JsonTrack obj = gson.fromJson(content, JsonTrack.class);

            mSpotifyAppRemote.getPlayerApi().play(obj.getUri());
        }
        catch (Exception ex)
        {
            Toast toast = Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
    }
}