package gc.garcol.caferaft.core.log;

/**
 * @author thaivc
 * @since 2025
 */
public record Position(
    long term,
    long index
) implements Comparable<Position> {
    @Override
    public int compareTo(Position other) {
        if (this.term == other.term) {
            return Long.compare(this.index, other.index);
        }
        return Long.compare(this.term, other.term);
    }
}
