package spotifyproject.nath.spotifyvoicecontroller;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackHolder>
{
    private ArrayList<Track> track_list;

    static class TrackHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txt_track_name;
        TextView txt_album_name;
        TextView txt_artist_name;

        String uri;

        TrackHolder(View item_view)
        {
            super(item_view);
            txt_track_name = itemView.findViewById(R.id.item_track_name);
            txt_album_name = itemView.findViewById(R.id.item_album_name);
            txt_artist_name = itemView.findViewById(R.id.item_artist_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View item_view)
        {
            MainActivity.spotify_app_remote.getPlayerApi().play(uri);
        }
    }

    TrackAdapter(ArrayList<Track> p_track_list)
    {
        track_list = p_track_list;
    }

    @NonNull
    @Override
    public TrackHolder onCreateViewHolder(@NonNull ViewGroup parent, int view_type)
    {
        return new TrackHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TrackHolder holder, int position)
    {
        holder.txt_track_name.setText(defineMaximumSize(track_list.get(position).get_name(),30));
        holder.txt_album_name.setText(defineMaximumSize(track_list.get(position).get_album(), 20));
        holder.txt_artist_name.setText(defineMaximumSize(holder.txt_artist_name.getText() + track_list.get(position).get_artist(), 18));
        holder.uri = track_list.get(position).get_uri();
    }

    @Override
    public int getItemCount()
    {
        return track_list.size();
    }

    private String defineMaximumSize(String string_to_check, int max_size)
    {
        return (string_to_check.length() > max_size) ? string_to_check.substring(0, max_size - 4) + "..." : string_to_check;
    }
}