package spotifyproject.nath.spotifyvoicecontroller;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.CommandHolder>
{
    private ArrayList<Command> command_list;

    static class CommandHolder extends RecyclerView.ViewHolder
    {
        TextView txt_command_name;
        TextView txt_command_description;

        CommandHolder(View item_view)
        {
            super(item_view);
            txt_command_name = itemView.findViewById(R.id.item_command_name);
            txt_command_description = itemView.findViewById(R.id.item_command_description);
        }
    }

    CommandAdapter(ArrayList<Command> p_command_list)
    {
        command_list = p_command_list;
    }

    @NonNull
    @Override
    public CommandHolder onCreateViewHolder(@NonNull ViewGroup parent, int view_type)
    {
        return new CommandHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_command, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommandAdapter.CommandHolder holder, int position)
    {
        holder.txt_command_name.setText(command_list.get(position).get_name());
        holder.txt_command_description.setText(command_list.get(position).get_description());
    }

    @Override
    public int getItemCount()
    {
        return command_list.size();
    }
}