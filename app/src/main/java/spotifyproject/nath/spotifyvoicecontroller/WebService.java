package spotifyproject.nath.spotifyvoicecontroller;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class WebService extends AsyncTask<Void, Void, String>
{
    private Context _myContext;
    private HashMap<String, String> _paramsToSearch;
    public ArrayList<OnDownloadCompleteListener> listeners = new ArrayList<>();

    public WebService(Context context, HashMap<String, String> paramsToSearch)
    {
        _paramsToSearch = paramsToSearch;
        _myContext = context;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Void... values)
    {
        super.onProgressUpdate(values);
    }

    @Override
    protected String doInBackground(Void... arg0)
    {
        return performPostCall("https://api.spotify.com/v1/search?token=", _paramsToSearch);
    }

    public void setOnDownloadCompleteListener(OnDownloadCompleteListener listener)
    {
        listeners.add(listener);
    }

    @Override
    protected void onPostExecute(String result)
    {
        for (OnDownloadCompleteListener listener : listeners)
        {
            listener.onDownloadComplete(result, 0);
        }
    }

    public String performPostCall(String requestURL, HashMap<String, String> postDataParams)
    {
        URL url;
        String response = "";
        try
        {
            url = new URL(requestURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));
            writer.flush();
            writer.close();
            os.close();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK)
            {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null)
                {
                    response += line;
                }
            } else
            {
                response = "";
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
