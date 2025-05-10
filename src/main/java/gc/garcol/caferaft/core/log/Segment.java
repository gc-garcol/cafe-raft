package gc.garcol.caferaft.core.log;

import lombok.Getter;
import lombok.Setter;

import java.io.RandomAccessFile;

/**
 * @author thaivc
 * @since 2025
 */
@Getter
@Setter
public class Segment implements Comparable<Segment> {

    private long term;
    private long size;
    private RandomAccessFile rwFile;
    private RandomAccessFile indexFile;

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
