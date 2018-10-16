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

public class DocumentationActivity extends AppCompatActivity implements OnDownloadCompleteListener
{
    private static final int SPEECH_OUTPUT_REQUEST_CODE = 100;

    private Tools tools;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentation);

        tools = new Tools(getApplicationContext());

        Bundle bundle = getIntent().getExtras();

        if (bundle != null)
        {
            tools.set_access_token(bundle.getString("access_token"));
            tools.set_spotify_user_id(bundle.getString("user_id"));
        }

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
                        historic_intent.putExtra("access_token", tools.get_access_token());
                        historic_intent.putExtra("user_id", tools.get_spotify_user_id());
                        startActivity(historic_intent);
                        break;

                    case R.id.playlists:
                        Intent playlists_intent = new Intent(getApplicationContext(), PlaylistsActivity.class);
                        playlists_intent.putExtra("access_token", tools.get_access_token());
                        playlists_intent.putExtra("user_id", tools.get_spotify_user_id());
                        startActivity(playlists_intent);
                        break;
                }
                return false;
            }
        });

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

            tools.actionToDo(words, null, null, "other");
        }
        else
            Toast.makeText(getApplicationContext(), "No text said", Toast.LENGTH_SHORT).show();
    }

    private ArrayList<Command> setCommandList()
    {
        ArrayList<Command> command_list = new ArrayList<>();

        command_list.add(new Command("Play", "To play a song, say \"Play\" followed by " +
                "the title of the song."));
        command_list.add(new Command("Add a track to the queue", "To put a song in the " +
                "play queue, say \"Next\" followed by the title of the song."));
        command_list.add(new Command("Skip", "To skip a song, say \"Skip\" or \"Next\"."));
        command_list.add(new Command("Restart", "To restart a song, say \"Restart\""));
        command_list.add(new Command("Previous", "To go to the previous song, say \"Previous\"."));
        command_list.add(new Command("Pause", "To pause a song, say \"Pause\" or \"Stop\"."));
        command_list.add(new Command("Resume", "To resume a song, say \"Resume\"."));
        command_list.add(new Command("Create playlist", "To create a playlist, say \"Create\" " +
                "followed by the playlist name."));
        command_list.add(new Command("Add a track to a playlist", "To add a track to a " +
                "playlist, you must select the playlist on the playlists view. Then say \"Add\" followed " +
                "by the title of the song"));

        return command_list;
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