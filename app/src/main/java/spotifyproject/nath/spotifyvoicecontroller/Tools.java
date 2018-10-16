package spotifyproject.nath.spotifyvoicecontroller;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

class Tools
{
    private Context _current_context;

    RequestQueue request_queue;

    SpotifyAppRemote spotify_app_remote;

    final String CLIENT_ID = "7c330b477151476e97eae3ee39758a3f";
    final String REDIRECT_URI = "nath.spotifyproject.SpotifyVoiceController://callback";

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

    private void enableSpeechButtonClick(Button button)
    {
        button.setEnabled(true);
        button.setBackgroundResource(R.color.primary_btn_bg_color);
        button.setTextColor(_current_context.getColor(R.color.primary_btn_txt_color));
    }
}