package spotifyproject.nath.spotifyvoicecontroller;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackHolder>
{
    private ArrayList<Track> trackList;

    static class TrackHolder extends RecyclerView.ViewHolder
    {
        TextView txtTrackName;
        TextView txtAlbumName;
        TextView txtArtistName;

        TrackHolder(View itemView)
        {
            super(itemView);
            txtTrackName = itemView.findViewById(R.id.item_track_name);
            txtAlbumName = itemView.findViewById(R.id.item_album_name);
            txtArtistName = itemView.findViewById(R.id.item_artist_name);
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
        holder.txtTrackName.setText(trackList.get(position).get_name());
        holder.txtAlbumName.setText(trackList.get(position).get_album());
        holder.txtArtistName.setText(trackList.get(position).get_artist());
    }

    @Override
    public int getItemCount()
    {
        return trackList.size();
    }
}