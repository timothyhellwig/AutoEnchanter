import org.sikuli.script.*;
import java.util.*;
import java.lang.reflect.*;

public class Item extends AutoEnchanter {
    // Sikuli Match object for the item we're going to enchant
    final Pattern itemStack;

    // Counters and trackers
    private int totalEnchanted = 0; // keeps track of total number of enchanted mats made
    private int invStacks = 0; // keeps tracks of how many unenchanted stacks are in inventory
    int stacksToMake = 1; // number of enchanted stacks to make
    int stacksPerEnchant = 5; // stacks required per enchant

    // Constructor
    Item(Pattern itemStack) {
        this.itemStack = itemStack;
    }

    // Enchant loop, checks to ensure correct screen is open, and then enchants
    void doEnchant(Menu menu) {
        menu.verifyCraftingScreen();
        int counter = 0;
        invStacks = 0;
        Iterator<Match> mm = menu.findInvStacks(this);
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
            checkHotKeys();
            doShiftClick(menu.s, mmSorted.next());
            counter += 1;
            if (counter == stacksPerEnchant) {
                doShiftClick(menu.s, menu.ref);
                totalEnchanted += 2;
                invStacks -= counter;
                counter = 0;
                if (invStacks < stacksPerEnchant) {
                    return;
                }
            }
        }
    }

    // Buy loop, checks to ensure correct screen is open, and then buys
    void doBuy(Menu menu) {
        menu.offMenu.hover();
        menu.verifyBuyScreen();
        if(totalEnchanted / 64 >= stacksToMake) {
            Sikulix.popup("Enchanting Completed");
            System.exit(0);
        }
        int stacksToBuy = stacksNeeded() < menu.emptySlots() ? stacksNeeded() : menu.emptySlots();
        for (int i = 0; i < stacksToBuy; i++) {
            checkHotKeys();
            doLeftClick(menu.ref);
        }
    }

    // Calculates the number of item stacks that needs to be bought
    private int stacksNeeded() {
        return ((stacksToMake * 64 - totalEnchanted) / 2) * stacksPerEnchant - invStacks;
    }

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