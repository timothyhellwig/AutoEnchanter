import org.sikuli.script.*;
import java.util.Comparator;

/**
 * Class that implements the compare() method for the Sikuli Match object
 * Compares a bunch of matches, and sorts them first by Y, and then by X
 */
public class MatchComparator implements Comparator<Match>{
    /**
     * Compares two matches, and ranks them first by Y, and then by X if both Y are equal
     *
     * @param m1 Match #1
     * @param m2 Match #2
     * @return The integer value returned from Integer.compare()
     */
    public int compare(Match m1, Match m2) {
        int value = Integer.compare(m1.y, m2.y);
        if (value == 0) {
            return Integer.compare(m1.x, m2.x);
        }
        return value;
    }
}