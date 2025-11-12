package jumdo12.springgomok.domain;

public enum Stone {
    BLACK,
    WHITE,
    EMPTY;

    public Stone opposite() {
        return switch (this) {
            case BLACK -> WHITE;
            case WHITE -> BLACK;
            case EMPTY -> EMPTY;
        };
    }
}
