package spotifyproject.nath.spotifyvoicecontroller;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackHolder>
{
    private String[] trackList;

    static class TrackHolder extends RecyclerView.ViewHolder
    {
        TextView txtTrackName;

        TrackHolder(View itemView)
        {
            super(itemView);
            txtTrackName = itemView.findViewById(R.id.item_track_name);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    TrackAdapter(String[] mTrackList)
    {
        trackList = mTrackList;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public TrackHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new TrackHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull TrackHolder holder, int position)
    {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.txtTrackName.setText(trackList[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return trackList.length;
    }
}