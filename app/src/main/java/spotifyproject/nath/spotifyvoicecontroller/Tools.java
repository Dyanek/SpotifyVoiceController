package spotifyproject.nath.spotifyvoicecontroller;

import android.content.Context;
import android.widget.Button;

class Tools
{
    private Context _current_context;

    Tools(Context context)
    {
        _current_context = context;
    }

    void enableSpeechButtonClick(Button button)
    {
        button.setEnabled(true);
        button.setBackgroundResource(R.color.primary_btn_bg_color);
        button.setTextColor(_current_context.getColor(R.color.primary_btn_txt_color));
    }
}
