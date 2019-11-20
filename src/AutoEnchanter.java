import org.sikuli.script.*;
import org.sikuli.basics.*;
import java.util.*;
import java.io.*;

public class AutoEnchanter {
    private boolean hotKeyP = false;

    // Constructor
    AutoEnchanter() {
        // Shift + x terminates this program
        HotkeyListener x_SHIFT = new HotkeyListener() {
            @Override
            public void hotkeyPressed(HotkeyEvent e) {
                System.exit(0);
            }
        };
        HotkeyManager.getInstance().addHotkey("x", KeyModifier.SHIFT, x_SHIFT);

        // Shift + p pauses this program
        HotkeyListener p_SHIFT = new HotkeyListener() {
            @Override
            public void hotkeyPressed(HotkeyEvent e) {
                hotKeyP = !hotKeyP;
            }
        };
        HotkeyManager.getInstance().addHotkey("p", KeyModifier.SHIFT, p_SHIFT);
    }

    /**
     * Logs an exception, along with the values of this Menu object
     *
     * @param exception The exception to log
     */
    private void logException(Exception exception) {
        try {
            new File("AE_Log").mkdir();
            FileWriter fw = new FileWriter("AE_Log/log.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("----------------------------------------\n");
            bw.write(new Date().toString() + "\n");
            bw.write(this.toString() + "\n");

            bw.close();

            fw = new FileWriter("AE_Log/log.txt", true);
            PrintWriter pw = new PrintWriter(fw);

            exception.printStackTrace(pw);

            pw.close();

            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Returns the center point of a given match
    private Location location(Match match) {
        return new Location(match.x + match.w / 2, match.y + match.h / 2);
    }

    // Attempts to find given pattern on the whole screen
    Match tryFind(Screen screen, Pattern pattern){
        try {
            return screen.find(pattern);
        }
        catch (Exception e) {
            return null;
        }
    }

    // Attempts to find given pattern in given region
    Match tryFind(Region region, Pattern pattern) {
        try {
            return region.find(pattern);
        }
        catch (Exception e) {
            return null;
        }
    }

    // Finds all matches of a given pattern
    Iterator<Match> tryFindAll(Region region, Pattern pattern) {
        try {
            return region.findAll(pattern);
        }
        catch (Exception e) {
            logException(e);
            Sikulix.popup("Error: Pattern not found in region");
            System.exit(1);
            return null;
        }
    }

    // Try wrapper for Thread.sleep()
    private void trySleep(int ns) {
        try {
            Thread.sleep(ns);
        }
        catch (Exception e) {
            logException(e);
            Sikulix.popup("Error: Sleep error");
            System.exit(1);
        }
    }

    /**
     * Try-Catch wrapper with error logging for Sikuli wait() method
     *
     * @param region Region in which to search for
     * @param pattern Pattern to search for
     * @return The match object resulting from a successful search
     */
    Match tryWait(Region region, Pattern pattern) {
        try {
            return region.wait(pattern, 5);
        } catch (Exception e) {
            logException(e);
            System.exit(1);
            return null;
        }
    }

    /**
     * Random sleep, to avoid sending uniformly spaced packets
     */
    private void randSleep() {
        int r = (int) (Math.random() * 300);
        trySleep(r);
    }

    // Do a shift click, spaced out with sleeps, to keep CPS low
    void doShiftClick(Screen screen, Match match) {
        screen.keyDown(Key.SHIFT);
        trySleep(100);
        location(match).click();
        trySleep(100);
        screen.keyUp();
        randSleep();
    }

    // Overloaded method, for Match object
    void doLeftClick(Match match) {
        doLeftClick(location(match));
    }

    // Do a left click, spaced out with sleeps, to keep CPS low
    private void doLeftClick(Location location) {
        trySleep(200);
        location.click();
        randSleep();
    }

    // Overloaded method, for Match object
    private void doRightClick(Match match) {
        doRightClick(location(match));
    }

    // Do a right click, spaced out with sleeps, to keep CPS low
    private void doRightClick(Location location) {
        trySleep(200);
        location.rightClick();
        randSleep();

    }

    // Do a key press, spaced out with sleeps, to keep CPS low
    private void typeE(Screen screen) {
        trySleep(100);
        screen.type("e");
        randSleep();
    }

    // Checks the quit or pause hotkeys, and takes appropriate action
    void checkHotKeys() {
        while (hotKeyP) {
            trySleep(1000);
        }
    }

    public static void main(String[] args) {
        double minSimilarity = 0.99;
        // Get match similarity from user
        try {
            String s = Sikulix.input("Match Sensitivity:", "0.99");
            minSimilarity = Double.parseDouble(s);
        }
        catch (Exception e) {
            Sikulix.popup("Error: Must input one number between 0 and 1 exclusive. Resorting to default value.");
        }

        // Mouse move speed and match similarity
        Settings.MoveMouseDelay = (float) 0.0;
        Settings.MinSimilarity = minSimilarity;

        // File where images are stored
        ImagePath.add("AutoEnchanter/images");

        // Create instances necessary for this program
        AutoEnchanter ae = new AutoEnchanter();
        Menu menu = new Menu();
        Item item = menu.createItem();

        // Get options from user
        try {
            String options = Sikulix.input("Enchanted Stacks to make:", "1");
            String[] optionArr = options.split(" ", 0);
            item.stacksToMake = Integer.parseInt(optionArr[0]);
            if (optionArr.length == 2) {
                item.stacksPerEnchant = Integer.parseInt(optionArr[1]);
            }
        }
        catch (Exception e) {
            Sikulix.popup("Error: Must input only 1 or 2 integers");
            System.exit(1);
        }

        // Click on the app window to focus it
        ae.doLeftClick(menu.offMenu);

        // Main loop, that runs through all the steps until interrupt sequence is detected, or enchant limit is reached
        while (true) {
            ae.checkHotKeys();
            item.doEnchant(menu);
            ae.typeE(menu.s);
            ae.doLeftClick(menu.merchant);
            ae.doRightClick(menu.findBuy(item));
            item.doBuy(menu);
            ae.typeE(menu.s);
            ae.typeE(menu.s);
            ae.doLeftClick(menu.verifyNetherStar());
            menu.offMenu.hover();
            ae.tryWait(menu.buyRegion, menu.barrier);
            ae.doLeftClick(menu.craftingBench);
        }
    }
}