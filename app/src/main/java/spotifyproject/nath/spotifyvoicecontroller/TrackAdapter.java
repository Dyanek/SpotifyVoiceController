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
    private ArrayList<Track> _track_list;

    private Boolean _is_main_activity;
    private Tools _tools;

    static class TrackHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txt_track_name;
        TextView txt_album_name;
        TextView txt_artist_name;

        String uri;
        Boolean is_main_activity;

        Tools tools;

        TrackHolder(View item_view, Boolean p_is_main_activity, Tools p_tools)
        {
            super(item_view);
            txt_track_name = itemView.findViewById(R.id.item_track_name);
            txt_album_name = itemView.findViewById(R.id.item_album_name);
            txt_artist_name = itemView.findViewById(R.id.item_artist_name);
            itemView.setOnClickListener(this);

            is_main_activity = p_is_main_activity;

            tools = p_tools;
        }

        @Override
        public void onClick(View item_view)
        {
            if (is_main_activity)
                tools.spotify_app_remote.getPlayerApi().play(uri);
            else
                tools.spotify_app_remote.getPlayerApi().play(uri);
        }
    }

    TrackAdapter(ArrayList<Track> track_list, Boolean is_main_activity, Tools tools)
    {
        _track_list = track_list;
        _is_main_activity = is_main_activity;
        _tools = tools;
    }

    @NonNull
    @Override
    public TrackHolder onCreateViewHolder(@NonNull ViewGroup parent, int view_type)
    {
        return new TrackHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false), _is_main_activity, _tools);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackHolder holder, int position)
    {
        holder.txt_track_name.setText(defineMaximumSize(_track_list.get(position).get_name(), 40));
        holder.txt_album_name.setText(defineMaximumSize(_track_list.get(position).get_album(), 20));
        holder.txt_artist_name.setText(defineMaximumSize(holder.txt_artist_name.getText() + " " + _track_list.get(position).get_artist(), 20));
        holder.uri = _track_list.get(position).get_uri();
    }

    @Override
    public int getItemCount()
    {
        return _track_list.size();
    }

    private String defineMaximumSize(String string_to_check, int max_size)
    {
        return (string_to_check.length() > max_size) ? string_to_check.substring(0, max_size - 4) + "..." : string_to_check;
    }
}