import org.sikuli.script.*;
import java.util.*;
import java.lang.reflect.*;

public class Item{
    AutoEnchanter ae;

    // Sikuli Match object for the item we're going to enchant
    final Pattern itemStack;

    // Counters and trackers
    int totalEnchanted = 0; // keeps track of total number of enchanted mats made
    private int invStacks = 0; // keeps tracks of how many unenchanted stacks are in inventory
    int stacksToMake = 1; // number of enchanted stacks to make
    int stacksPerEnchant = 5; // stacks required per enchant

    /**
     * Constructor
     *
     * @param ae AutoEnchanter object which created this Item instance
     * @param itemStack The item pattern that this Item object will search for
     */
    Item(AutoEnchanter ae, Pattern itemStack) {
        this.ae = ae;
        this.itemStack = itemStack;
    }

    /**
     * Checks whether the enchanting job has completed, and if so, pops up a message
     *
     * @return True if complete, False if not complete yet
     */
    boolean finishedEnchanting() {
        if(totalEnchanted / 64 >= stacksToMake) {
            Sikulix.popup("Enchanting Completed");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Enchant loop, checks to ensure correct screen is open, and then enchants
     *
     * @param menu Menu object in which to do the enchanting
     */
    void doEnchant(Menu menu) {
        if (!menu.verifyCraftingScreen()) {
            ae.stop();
            return;
        }
        int counter = 0;
        invStacks = 0;
        Iterator<Match> mm = menu.findInvStacks();
        if (mm == null) return;
        List mmList = new ArrayList();
        while (mm.hasNext()) {
            mmList.add(mm.next());
            invStacks += 1;
        }
        mmList.sort(new MatchComparator());
        Iterator<Match> mmSorted = mmList.iterator();
        if (invStacks < stacksPerEnchant) {
            return;
        }
        while (mmSorted.hasNext()) {
            ae.checkHotKeys();
            ae.doShiftClick(menu.s, mmSorted.next());
            counter += 1;
            if (counter == stacksPerEnchant) {
                ae.doShiftClick(menu.s, menu.ref);
                totalEnchanted += 2;
                invStacks -= counter;
                counter = 0;
                if (invStacks < stacksPerEnchant) {
                    return;
                }
            }
        }
    }

    /**
     * Buy loop, checks to ensure correct screen is open, and then buys
     *
     * @param menu Menu object in which to search
     */
    void doBuy(Menu menu) {
        if (!menu.verifyBuyScreen()) {
            ae.stop();
            return;
        }
        int stacksToBuy = stacksNeeded() < menu.emptySlots() ? stacksNeeded() : menu.emptySlots();
        for (int i = 0; i < stacksToBuy; i++) {
            ae.checkHotKeys();
            ae.doLeftClick(menu.stack64);
        }
    }

    /**
     * Searches the open chest for the itemStack, and moves as many stacks as needed/possible to inventory
     *
     * @param menu Menu object on which to perform actions
     */
    void getFromChest(Menu menu) {
        if (!menu.verifyNetherStar()) {
            ae.stop();
            return;
        }
        int emptyMenuSlots = menu.emptySlots();
        int stacksToGet = stacksNeeded() < emptyMenuSlots ? stacksNeeded() : emptyMenuSlots;
        Iterator<Match> mm = menu.findChestStacks();
        if (mm == null) return;
        List mmList = new ArrayList();
        while (mm.hasNext()) {
            mmList.add(mm.next());
        }
        mmList.sort(new MatchComparator());
        Iterator<Match> mmSorted = mmList.iterator();
        while (mmSorted.hasNext() && stacksToGet > 0) {
            ae.checkHotKeys();
            ae.doShiftClick(menu.s, mmSorted.next());

            stacksToGet--;
        }
    }

    /**
     * Calculates the number of item stacks that need to be bought
     *
     * @return The number of item stacks that need to be bought
     */
    private int stacksNeeded() {
        return ((stacksToMake * 64 - totalEnchanted) / 2) * stacksPerEnchant - invStacks;
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

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for ( Field field : fields  ) {
            result.append("  ");
            try {
                result.append( field.getName() );
                result.append(": ");
                //requires access to private field:
                result.append( field.get(this) );
            } catch ( IllegalAccessException e ) {
                continue;
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }
}