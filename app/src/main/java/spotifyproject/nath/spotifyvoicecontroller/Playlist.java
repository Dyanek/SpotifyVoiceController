package spotifyproject.nath.spotifyvoicecontroller;

public class Playlist
{
    private String _name;
    private String _author;
    private String _uri;

    String get_name() { return _name; }
    String get_author() { return _author; }
    String get_uri() { return _uri; }

    Playlist(String name, String author, String uri)
    {
        _name = name;
        _author = author;
        _uri = uri;
    }
}
