package spotifyproject.nath.spotifyvoicecontroller;

public class Track
{
    private String _name;
    private String _album;
    private String _artist;
    private String _uri;

    public String get_name() { return _name; }
    public String get_album() { return _album; }
    public String get_artist() { return _artist; }
    public String get_uri() { return _uri; }

    public Track(String name, String album, String artist, String uri)
    {
        _name = name;
        _album = album;
        _artist = artist;
        _uri = uri;
    }
}