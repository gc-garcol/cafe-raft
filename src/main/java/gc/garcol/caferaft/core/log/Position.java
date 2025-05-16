package gc.garcol.caferaft.core.log;

/**
 * @author thaivc
 * @since 2025
 */
public record Position(
    long term,
    long index
) implements Comparable<Position> {
    public static boolean equals(Position thisPosition, Position thatPosition) {
        if (thisPosition == null && thatPosition == null) {
            return false;
        }

        if (thisPosition == null || thatPosition == null) {
            return false;
        }
        return thisPosition.compareTo(thatPosition) == 0;
    }

    @Override
    public int compareTo(Position other) {
        if (this.term == other.term) {
            return Long.compare(this.index, other.index);
        }
        return Long.compare(this.term, other.term);
    }

    public Position copy() {
        return new Position(this.term, this.index);
    }
}
