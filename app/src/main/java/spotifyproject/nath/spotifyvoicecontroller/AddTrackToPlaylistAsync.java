package spotifyproject.nath.spotifyvoicecontroller;

import android.os.AsyncTask;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AddTrackToPlaylistAsync extends AsyncTask<Void, Void, Boolean>
{
    private String _playlist_id;
    private String _track_uri;
    private String _access_token;

    private ArrayList<OnDownloadCompleteListener> _listeners = new ArrayList<>();

    AddTrackToPlaylistAsync(String playlist_id, String track_uri, String access_token)
    {
        _playlist_id = playlist_id;
        _track_uri = track_uri;
        _access_token = access_token;
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
            URL url = new URL("https://api.spotify.com/v1/playlists/" + _playlist_id + "/tracks?uris=" + _track_uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");

            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + _access_token);

            conn.setDoOutput(true);
            conn.setDoInput(true);

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