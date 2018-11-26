package spotifyproject.nath.spotifyvoicecontroller;

// Item représentant une commande de l'activity Documentation
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
