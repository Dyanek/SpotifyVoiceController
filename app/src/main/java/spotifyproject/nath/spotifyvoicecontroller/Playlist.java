package spotifyproject.nath.spotifyvoicecontroller;

class Playlist
{
    private String _name;
    private String _author;
    private String _uri;
    private String _id;

    String get_name() { return _name; }
    String get_author() { return _author; }
    String get_uri() { return _uri; }
    String get_id() { return _id; }

    Playlist(String name, String author, String uri, String id)
    {
        _name = name;
        _author = author;
        _uri = uri;
        _id = id;
    }
}
