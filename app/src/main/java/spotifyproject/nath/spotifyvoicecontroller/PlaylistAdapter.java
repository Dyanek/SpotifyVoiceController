package spotifyproject.nath.spotifyvoicecontroller;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder>
{
    private ArrayList<Playlist> playlist_list;

    static class PlaylistHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txt_playlist_name;
        TextView txt_playlist_author;

        String uri;

        PlaylistHolder(View item_view)
        {
            super(item_view);
            txt_playlist_name = itemView.findViewById(R.id.item_playlist_name);
            txt_playlist_author = itemView.findViewById(R.id.item_playlist_author);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View item_view)
        {
            PlaylistsActivity.spotify_app_remote.getPlayerApi().play(uri);
        }
    }

    PlaylistAdapter(ArrayList<Playlist> p_playlist_list)
    {
        playlist_list = p_playlist_list;
    }

    @NonNull
    @Override
    public PlaylistHolder onCreateViewHolder(@NonNull ViewGroup parent, int view_type)
    {
        return new PlaylistHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistHolder holder, int position)
    {
        holder.txt_playlist_name.setText(defineMaximumSize(playlist_list.get(position).get_name()));
        holder.txt_playlist_author.setText(defineMaximumSize(playlist_list.get(position).get_author()));
        holder.uri = playlist_list.get(position).get_uri();
    }

    @Override
    public int getItemCount()
    {
        return playlist_list.size();
    }

    private String defineMaximumSize(String string_to_check)
    {
        return (string_to_check.length() > 40) ? string_to_check.substring(0, 36) + "..." : string_to_check;
    }
}