package gc.garcol.caferaft.core.log;

import lombok.Data;

/**
 * @author thaivc
 * @since 2025
 */
@Data
public class Segment implements Comparable<Segment> {

    private String name;

    private long term;

    public static Segment of(long term) {
        var segment = new Segment();
        segment.term = term;
        return segment;
    }

    @Override
    public int compareTo(Segment otherSegment) {
        return Long.compare(this.term, otherSegment.term);
    }
}
