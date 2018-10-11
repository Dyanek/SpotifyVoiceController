package spotifyproject.nath.spotifyvoicecontroller;

class Playlist
{
    private String _name;
    private String _author;
    private String _uri;
    private String _id;
    private Integer _size;

    String get_name() { return _name; }
    String get_author() { return _author; }
    String get_uri() { return _uri; }
    String get_id() { return _id; }
    Integer get_size() { return _size; }

    Playlist(String name, String author, String uri, String id, Integer size)
    {
        _name = name;
        _author = author;
        _uri = uri;
        _id = id;
        _size = size;
    }
}
