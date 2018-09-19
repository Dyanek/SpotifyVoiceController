package spotifyproject.nath.spotifyvoicecontroller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

public class MainActivity extends AppCompatActivity
{
    private static final String CLIENT_ID = "7c330b477151476e97eae3ee39758a3f";
    private static final String REDIRECT_URI = "nath.spotifyproject.SpotifyVoiceController://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    // Spotify ClientSecret = 517bac5fcbec4886a872022117f3d4cf;

    private final int REQ_CODE_SPEECH_OUTPUT = 100;
    private Button btnOpenMicrophone;
    private TextView txtShowVoiceOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenMicrophone = findViewById(R.id.button);
        txtShowVoiceOutput = findViewById(R.id.showVoiceOutput);

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

        // Set the connection parameters
        ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
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
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable)
                    {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    private void connected()
    {
        Toast toast = Toast.makeText(getApplicationContext(), "Connected to Spotify", Toast.LENGTH_SHORT);
        toast.show();
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
            startActivityForResult(intent, REQ_CODE_SPEECH_OUTPUT);
        } catch (ActivityNotFoundException tim)
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Cet appareil ne supporte pas d'entrée vocale.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == REQ_CODE_SPEECH_OUTPUT)
        {
            if (resultCode == RESULT_OK && data != null)
            {
                ArrayList<String> voiceInText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                txtShowVoiceOutput.setText(voiceInText.get(0));
            }
            else
                txtShowVoiceOutput.setText("Action incomplète");
        }
    }
}