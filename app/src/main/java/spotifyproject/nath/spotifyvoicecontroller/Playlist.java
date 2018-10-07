package spotifyproject.nath.spotifyvoicecontroller;

public class Playlist
{
    private String _name;
    private String _author;
    private String _url;

    String get_name() { return _name; }
    String get_author() { return _author; }
    String get_url() { return _url; }

    Playlist(String name, String author, String url)
    {
        _name = name;
        _author = author;
        _url = url;
    }
}
