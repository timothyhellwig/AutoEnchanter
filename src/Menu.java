import org.sikuli.script.*;
import java.util.Iterator;
import java.lang.reflect.*;
import java.io.*;
import javax.imageio.ImageIO;

public class Menu {
    private AutoEnchanter ae;
    private Item item;
    final Screen s = new Screen();
    int itemSource;

    // The ratio that was used to scale barrier.png down for a successful match
    private float invScale = 1;

    // Images used in searches
    final Pattern barrier = new Pattern("barrier.png").similar(0.7);
    private final Pattern emptySlot = new Pattern("empty.png").similar(0.95);
    private final Pattern netherStar = new Pattern("netherStar.png").similar(0.7);

    // Main points of reference, which we use to find everything else on the screen
    Match ref;
    private Match neth;

    // Various static locations on the screen
    Location offMenu = null;
    Location craftingBench = null;
    Location nethStar = null;
    Region buyRegion = null;
    Location stack64 = null;
    private Region invRegion = null;
    private Region refRegion = null;
    private Region nethRegion = null;
    private Region merchantRegion = null;

    /**
     * Constructor
     *
     * @param ae The AutoEnchanter object that has called this constructor
     */
    public Menu(AutoEnchanter ae) {
        this.ae = ae;

        // Initial search for barrier block
        ref  = ae.tryFind(s, barrier);

        // Scale factor for the Pattern.resize() method
        float scale = (float) 1.0;

        // While we're not finding the barrier, scale down the image a little, and try to find it again
        while(ref == null && scale > 0.2) {
            scale -= 0.02;
            ref = ae.tryFind(s, barrier.resize(scale));
        }

        // If ref == null, then barrier wasn't found at any size, so exit
        if (ref == null) {
            Sikulix.popError("Error: Pattern not found on screen");
            ae.run = false;
            return;
        }

        // Resize emptySlot to the same scale as barrier
        emptySlot.resize(scale);

        // Save the scale value, for future calculations
        invScale = scale;
        scale = (float) 1.0;

        // Various static locations on the screen
        offMenu = new Location(ae.location(ref).x + slot(4), ae.location(ref).y);
        craftingBench = new Location(ae.location(ref).x - slot(1), ae.location(ref).y + slot(1));
        buyRegion = new Region(ref.x - slot(5), ref.y - slot(2), slot(9), slot(6));
        invRegion = new Region(ref.x - slot(5), ref.y + slot(4), slot(9), slot(5));
        refRegion = new Region(ref.x - 1, ref.y - 1, ref.w + 1, ref.h + 1);
        stack64 = ae.location(ref);
        stack64.x += slot(1);

        // Initial search for nether star
        neth = ae.tryFind(invRegion, netherStar);

        // While we're not finding the nether star, scale down the image a little, and try to find it again
        while(neth == null && scale > 0.05) {
            scale -= 0.02;
            neth = ae.tryFind(invRegion, netherStar.resize(scale));
        }

        // If neth == null, then netherStar wasn't found at any size, so exit
        if (neth == null) {
            ae.run = false;
        }

        nethRegion = new Region(neth.x - 1, neth.y - 1, neth.w + 1, neth.h + 1);
        nethStar = new Location(ae.location(neth).x, ae.location(neth).y - slot(1.5));
    }

    /**
     * Single function for calculating pixel location of any given inventory slot
     *
     * @param slots The number of slots to offset the calculation by
     * @return The pixel location of desired inventory slot
     */
    private int slot(double slots) {
        return (int) (slots * 90 * invScale);
    }

    /**
     * Finds the item to buy on the merchant screen
     *
     * @param item The item to be found
     * @return The match object of the successful find
     */
    Match findBuy(Item item) {
        return ae.tryWait(buyRegion, item.itemStack);
    }

    /**
     * Verify that the nether star is visible
     *
     * @return True if visible, else false
     */
    boolean verifyNetherStar() {
        return ae.tryWait(invRegion, netherStar) == null ? false : true;
    }

    /**
     * Verify that the buy screen is open
     *
     * @return True if open, else false
     */
    boolean verifyBuyScreen() {
        return ae.tryWait(refRegion, item.itemStack) == null ? false : true;
    }

    /**
     * Verify that the crafting screen is open
     *
     * @return True if open, else false
     */
    boolean verifyCraftingScreen() {
        return ae.tryWait(refRegion, barrier.resize(invScale)) == null ? false : true;
    }

    /**
     * Finds all stacks of the item to enchant in the inventory region
     *
     * @return A match iterator of all found matches
     */
    Iterator<Match> findInvStacks() {
        return ae.tryFindAll(invRegion, item.itemStack);
    }

    /**
     * Finds all stacks of the item to enchant in the buy region
     *
     * @return A Match iterator of all found matches
     */
    Iterator<Match> findChestStacks() {
        return ae.tryFindAll(buyRegion, item.itemStack);
    }

    /**
     * Returns the number of empty slots in inventory
     *
     * @return The number of empty slots in inventory
     */
    int emptySlots() {
        int sum = 0;
        Iterator<Match> mm = ae.tryFindAll(invRegion, emptySlot);
        if(mm == null) return sum;
        while(mm.hasNext()) {
            mm.next();
            sum++;
        }
        return sum;
    }

    /**
     * Create and return a new Item instance
     *
     * @return Instance of a new item
     */
    Item createItem() {
        // Pulls the item to enchant from the inventory slot next to the menu star
        ScreenImage imgCap = s.capture(neth.x - slot(1) + (int) (neth.w * 0.1), neth.y, (int) (neth.w / 1.3), neth.h / 2);
        Pattern itemStack = new Pattern(imgCap);
        item = new Item(ae, itemStack);
        return item;
    }

    /**
     * Saves the working patterns to files, for debugging analysis
     */
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

    /**
     * Converts all variables of this file to strings, for debugging purposes
     *
     * @return A string containing all the variables associated with this object and their values
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append( this.getClass().getName() );
        result.append( " Object {" );
        result.append(newLine);

        // Determine fields declared in this class only (no fields of aeclass)
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