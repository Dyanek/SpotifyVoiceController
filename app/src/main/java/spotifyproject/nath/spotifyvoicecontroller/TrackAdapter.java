package spotifyproject.nath.spotifyvoicecontroller;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackHolder>
{
    private ArrayList<Track> trackList;

    static class TrackHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txtTrackName;
        TextView txtAlbumName;
        TextView txtArtistName;

        ImageView imgPlayPause;
        String uri;

        TrackHolder(View itemView)
        {
            super(itemView);
            txtTrackName = itemView.findViewById(R.id.item_track_name);
            txtAlbumName = itemView.findViewById(R.id.item_album_name);
            txtArtistName = itemView.findViewById(R.id.item_artist_name);
            imgPlayPause = itemView.findViewById(R.id.item_play_pause);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View itemView)
        {
            MainActivity.spotifyAppRemote.getPlayerApi().play(uri);
        }
    }

    TrackAdapter(ArrayList<Track> mTrackList)
    {
        trackList = mTrackList;
    }

    @NonNull
    @Override
    public TrackHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new TrackHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TrackHolder holder, int position)
    {
        holder.txtTrackName.setText(defineMaximumSize(trackList.get(position).get_name(),30));
        holder.txtAlbumName.setText(defineMaximumSize(trackList.get(position).get_album(), 20));
        holder.txtArtistName.setText(defineMaximumSize(holder.txtArtistName.getText() + trackList.get(position).get_artist(), 18));
        holder.uri = trackList.get(position).get_uri();
    }

    @Override
    public int getItemCount()
    {
        return trackList.size();
    }

    private String defineMaximumSize(String strToCheck, int maxSize)
    {
        return (strToCheck.length() > maxSize) ? strToCheck.substring(0,16) + "..." : strToCheck;
    }
}