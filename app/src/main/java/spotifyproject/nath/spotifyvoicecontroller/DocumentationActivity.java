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

import java.util.ArrayList;

public class DocumentationActivity extends AppCompatActivity
{
    private Button btn_open_microphone;

    private final int SPEECH_OUTPUT_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

        btn_open_microphone = findViewById(R.id.doc_btn_open_mic);

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
                        Intent documentation_intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(documentation_intent);
                        break;
                }
                return false;
            }
        });
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
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    private ArrayList<Command> setCommandList()
    {
        ArrayList<Command> command_list = new ArrayList<>();

        command_list.add(new Command("Play a song", "To play a song, say \"Play\" or \"Next\" followed by the title of the song."));

        return command_list;
    }
}
