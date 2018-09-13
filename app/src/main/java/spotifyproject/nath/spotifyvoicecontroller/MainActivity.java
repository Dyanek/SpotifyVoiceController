package spotifyproject.nath.spotifyvoicecontroller;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    private Button btnOpenMic;
    private TextView txtShowVoiceOutput;
    private final int REQ_CODE_SPEECH_OUTPUT = 143;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenMic = findViewById(R.id.button);
        txtShowVoiceOutput = findViewById(R.id.showVoiceOutput);

        btnOpenMic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ButtonToOpenMic();
            }
        });
    }

    private void ButtonToOpenMic()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez...");

        try
        {
            startActivityForResult(intent, REQ_CODE_SPEECH_OUTPUT);
        }
        catch (ActivityNotFoundException tim)
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Erreur micro ne fonctionnant pas: " + tim.getMessage(), Toast.LENGTH_LONG);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            case REQ_CODE_SPEECH_OUTPUT: {
                if(resultCode == RESULT_OK && data != null)
                {
                    ArrayList<String> voiceInText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtShowVoiceOutput.setText(voiceInText.get(0));
                }
                break;
            }
        }
    }
}