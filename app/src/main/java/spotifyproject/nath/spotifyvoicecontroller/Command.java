package spotifyproject.nath.spotifyvoicecontroller;

class Command
{
    private String _name;
    private String _description;

    String get_name() { return _name; }
    String get_description() { return _description; }

    Command(String name, String description)
    {
        _name = name;
        _description = description;
    }
}
