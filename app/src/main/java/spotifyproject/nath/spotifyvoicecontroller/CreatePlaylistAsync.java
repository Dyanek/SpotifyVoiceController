package spotifyproject.nath.spotifyvoicecontroller;

import android.os.AsyncTask;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CreatePlaylistAsync extends AsyncTask<Void, Void, Boolean>
{
    private String _spotify_user_id;
    private String _access_token;
    private String _playlist_name;

    private ArrayList<OnDownloadCompleteListener> _listeners = new ArrayList<>();

    CreatePlaylistAsync(String spotify_user_id, String access_token, String playlist_name)
    {
        _spotify_user_id = spotify_user_id;
        _access_token = access_token;
        _playlist_name = playlist_name;
    }

    void setOnDownloadCompleteListener(OnDownloadCompleteListener listener) {
        _listeners.add(listener);
    }

    @Override
    protected Boolean doInBackground(Void... arg0)
    {
        int result_code = 0;

        try
        {
            URL url = new URL("https://api.spotify.com/v1/users/" + _spotify_user_id + "/playlists");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");

            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + _access_token);

            conn.setDoOutput(true);
            conn.setDoInput(true);

            String postData = "{\"name\":\"" + _playlist_name + "\",\"description\":\"New playlist created with Spotify Voice Controller\",\"public\":true}";
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(postData);
            out.close();

           result_code = conn.getResponseCode();

           conn.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return (result_code == 200 || result_code == 201);
    }

    @Override
    protected void onPostExecute(Boolean is_successful)
    {
        for(OnDownloadCompleteListener listener : _listeners)
            listener.onDownloadComplete(is_successful, 1);
    }
}
