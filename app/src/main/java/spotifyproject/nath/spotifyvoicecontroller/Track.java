package spotifyproject.nath.spotifyvoicecontroller;

// Item représentant un titre des activities Main et PlaylistTracks
class Track
{
    private String _name;
    private String _album;
    private String _artist;
    private String _uri;

    String get_name() { return _name; }
    String get_album() { return _album; }
    String get_artist() { return _artist; }
    String get_uri() { return _uri; }

    Track(String name, String album, String artist, String uri)
    {
        _name = name;
        _album = album;
        _artist = artist;
        _uri = uri;
    }
}