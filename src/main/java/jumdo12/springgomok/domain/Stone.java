package jumdo12.springgomok.domain;

public enum Stone {
    BLACK,
    WHITE;

    public Stone opposite() {
        return switch (this) {
            case BLACK -> WHITE;
            case WHITE -> BLACK;
        };
    }
}
