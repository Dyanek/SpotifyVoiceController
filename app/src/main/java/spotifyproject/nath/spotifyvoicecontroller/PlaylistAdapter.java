package spotifyproject.nath.spotifyvoicecontroller;

import android.content.Context;
import android.content.Intent;
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

    private String _access_token;
    private String _spotify_user_id;

    static class PlaylistHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txt_playlist_name;
        TextView txt_playlist_author;

        String uri;
        String name;
        String id;
        Integer size;

        String holder_access_token;
        String holder_spotify_user_id;

        PlaylistHolder(View item_view, String access_token, String spotify_user_id)
        {
            super(item_view);
            txt_playlist_name = itemView.findViewById(R.id.item_playlist_name);
            txt_playlist_author = itemView.findViewById(R.id.item_playlist_author);
            itemView.setOnClickListener(this);

            holder_access_token = access_token;
            holder_spotify_user_id = spotify_user_id;
        }

        @Override
        public void onClick(View item_view)
        {
            Context context = item_view.getContext();
            Intent intent = new Intent(context, PlaylistTracksActivity.class);
            intent.putExtra("access_token", holder_access_token);
            intent.putExtra("user_id", holder_spotify_user_id);
            intent.putExtra("playlist_name", name);
            intent.putExtra("playlist_id", id);
            intent.putExtra("playlist_size", size);
            context.startActivity(intent);
        }
    }

    PlaylistAdapter(ArrayList<Playlist> p_playlist_list, String access_token, String spotify_user_id)
    {
        playlist_list = p_playlist_list;

        _access_token = access_token;
        _spotify_user_id = spotify_user_id;
    }

    @NonNull
    @Override
    public PlaylistHolder onCreateViewHolder(@NonNull ViewGroup parent, int view_type)
    {
        return new PlaylistHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false),
                _access_token, _spotify_user_id);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistHolder holder, int position)
    {
        holder.txt_playlist_name.setText(defineMaximumSize(playlist_list.get(position).get_name()));
        holder.txt_playlist_author.setText(defineMaximumSize(playlist_list.get(position).get_author()));
        holder.uri = playlist_list.get(position).get_uri();
        holder.name = playlist_list.get(position).get_name();
        holder.id = playlist_list.get(position).get_id();
        holder.size = playlist_list.get(position).get_size();
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