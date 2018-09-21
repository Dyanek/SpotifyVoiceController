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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements OnDownloadCompleteListener
{
    private static final String CLIENT_ID = "7c330b477151476e97eae3ee39758a3f";
    private static final String REDIRECT_URI = "nath.spotifyproject.SpotifyVoiceController://callback";
    private static final int SPOTIFY_REQUEST_CODE = 1337;
    public static SpotifyAppRemote mSpotifyAppRemote;

    public static final String PREFS_NAME = "PrefFile";

    private final int SPEECH_OUPUT_REQUEST_CODE = 100;
    private Button btnOpenMicrophone;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenMicrophone = findViewById(R.id.button);

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

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request);

        // Set the connection parameters---------------------------------------------------------------------
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.CONNECTOR.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
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
            Toast toast = Toast.makeText(getApplicationContext(), "No voice input detected on this device.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == SPOTIFY_REQUEST_CODE)
        {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType())
            {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    Toast toast = Toast.makeText(getApplicationContext(), "Handle successful", Toast.LENGTH_LONG);
                    toast.show();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Toast toast2 = Toast.makeText(getApplicationContext(), "Handle error", Toast.LENGTH_LONG);
                    toast2.show();
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
                    Toast toast3 = Toast.makeText(getApplicationContext(), "Handle other case", Toast.LENGTH_LONG);
                    toast3.show();
            }
        }

        if(requestCode == SPEECH_OUPUT_REQUEST_CODE)
        {

            HashMap<String, String> songSearch = new HashMap<>();
            songSearch.put("q", "fade%20to%20black");
            songSearch.put("type", "track");

            WebService webService = new WebService(getApplicationContext(), songSearch);
            webService.setOnDownloadCompleteListener(this);
            webService.execute();
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