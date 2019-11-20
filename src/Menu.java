import org.sikuli.script.*;
import java.util.Iterator;
import java.lang.reflect.*;
import java.io.*;
import javax.imageio.ImageIO;

public class Menu extends AutoEnchanter {
    private Item item;
    final Screen s = new Screen();

    // The ratio that was used to scale barrier.png down for a successful match
    private final float invScale;

    // Images used in searches
    final Pattern barrier = new Pattern("barrier.png").similar(0.7);
    private final Pattern emptySlot = new Pattern("empty.png").similar(0.95);
    private final Pattern netherStar = new Pattern("netherStar.png").similar(0.7);

    // Main points of reference, which we use to find everything else on the screen
    Match ref;
    private Match neth;

    // Various static locations on the screen
    final Location offMenu;
    final Location merchant;
    final Location craftingBench;
    final Region buyRegion;
    private final Region invRegion;
    private final Region refRegion;
    private final Region nethRegion;

    // Constructor
    public Menu() {
        // Initial search for barrier block
        ref  = tryFind(s, barrier);

        // Scale factor for the Pattern.resize() method
        float scale = (float) 1.0;

        // While we're not finding the barrier, scale down the image a little, and try to find it again
        while(ref == null && scale > 0.05) {
            scale -= 0.02;
            ref = tryFind(s, barrier.resize(scale));
        }

        // If ref == null, then barrier wasn't found at any size, so exit
        if (ref == null) {
            Sikulix.popup("Error: Pattern not found on screen");
            System.exit(1);
        }

        // Resize emptySlot to the same scale as barrier
        emptySlot.resize(scale);

        // Save the scale value, for future calculations
        invScale = scale;
        scale = (float) 1.0;

        // Various static locations on the screen
        offMenu = new Location(ref.x + slot(3), ref.y);
        merchant = new Location(ref.x - slot(2), ref.y + slot(3));
        craftingBench = new Location(ref.x - slot(2), ref.y + slot(1));
        buyRegion = new Region(ref.x - slot(7), ref.y - slot(2), slot(10), slot(6));
        invRegion = new Region(ref.x - slot(7), ref.y + slot(4), slot(10), slot(5));
        refRegion = new Region(ref.x - 1, ref.y - 1, ref.w + 1, ref.h + 1);

        // Initial search for nether star
        neth = tryFind(invRegion, netherStar);

        // While we're not finding the nether star, scale down the image a little, and try to find it again
        while(neth == null && scale > 0.05) {
            scale -= 0.02;
            neth = tryFind(invRegion, netherStar.resize(scale));
        }

        // If neth == null, then netherStar wasn't found at any size, so exit
        if (neth == null) {
            tryWait(invRegion, netherStar);
        }

        nethRegion = new Region(neth.x - 1, neth.y - 1, neth.w + 1, neth.h + 1);
    }

    // Single function for calculating pixel location of any given inventory slot
    private int slot(int slots) {
        return (int) (slots * 90 * invScale);
    }

    // Encapsulation of screen search for buy screen
    Match findBuy(Item item) {
        return tryWait(buyRegion, item.itemStack);
    }

    /**
     * Verify that the nether star is visible
     *
     * @return Match object of the nether star
     */
    Match verifyNetherStar() {
        return tryWait(invRegion, netherStar);
    }

    /**
     * Verify that the buy screen is open.
     */
    void verifyBuyScreen() {
        tryWait(refRegion, item.itemStack);
    }

    /**
     * Verify that the crafting screen is open
     */
    void verifyCraftingScreen() {
        tryWait(refRegion, barrier);
    }

    // Encapsulation of screen search for inventory stacks
    Iterator<Match> findInvStacks(Item item) {
        return tryFindAll(invRegion, item.itemStack);
    }

    // Returns the number of empty slots in inventory
    int emptySlots() {
        int sum = 0;
        Iterator<Match> mm = tryFindAll(invRegion, emptySlot);
        while(mm.hasNext()) {
            mm.next();
            sum++;
        }
        return sum;
    }

    // Create and return a new Item instance
    Item createItem() {
        // Pulls the item to enchant from the inventory slot next to the menu star
        ScreenImage imgCap = s.capture(neth.x - slot(1) + (int) (neth.w * 0.1), neth.y, (int) (neth.w / 1.3), neth.h / 2);
        Pattern itemStack = new Pattern(imgCap);
        item = new Item(itemStack);
        return item;
    }

    // Saves the working patterns to files, for debugging analysis
    private void imgDump() {
        try {
            ImageIO.write(item.itemStack.getBImage(), "png", new File("AE_Log/itemStack.png"));

            Pattern buyPattern = new Pattern(s.capture(buyRegion));
            ImageIO.write(buyPattern.getBImage(), "png", new File("AE_Log/buyRegion.png"));

            Pattern invPattern = new Pattern(s.capture(invRegion));
            ImageIO.write(invPattern.getBImage(), "png", new File("AE_Log/invRegion.png"));

            Pattern refPattern = new Pattern(s.capture(refRegion));
            ImageIO.write(refPattern.getBImage(), "png", new File("AE_Log/refRegion.png"));

            Pattern nethPattern = new Pattern(s.capture(nethRegion));
            ImageIO.write(nethPattern.getBImage(), "png", new File("AE_Log/nethRegion.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append( this.getClass().getName() );
        result.append( " Object {" );
        result.append(newLine);

        // Determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        // Print field names paired with their values
        for ( Field field : fields  ) {
            result.append("  ");
            try {
                result.append( field.getName() );
                result.append(": ");
                // Requires access to private field:
                result.append( field.get(this) );
            } catch ( IllegalAccessException e ) {
                continue;
            }
            result.append(newLine);
        }
        result.append("}");

        imgDump();

        return result.toString();
    }
}