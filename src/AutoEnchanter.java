import org.sikuli.script.*;
import org.sikuli.basics.*;
import java.util.*;
import java.io.*;

public class AutoEnchanter {
    private boolean hotKeyP = false;
    private View view;
    private Menu menu;
    private Item item;
    boolean run = false;

    /**
     * Constructor
     */
    public AutoEnchanter() {
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

        view = new View(this);
        getSettings();
    }

    /**
     * Sets run to false, which will stop all enchanting, and enable the Settings window
     */
    void stop() {
        run = false;
    }

    /**
     * Logs an exception, along with the values of the menu object
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
            bw.write(menu.toString() + "\n");

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

    /**
     * Find the center point of a Match object
     *
     * @param match Match object
     * @return new Location object of the center of the match
     */
    Location location(Match match) {
        return new Location(match.x + match.w / 2, match.y + match.h / 2);
    }

    /**
     * Try-Catch wrapper for Sikuli find() method
     *
     * @param screen Screen on which to search
     * @param pattern Pattern to search for
     * @return The match object resulting from a successful search
     */
    Match tryFind(Screen screen, Pattern pattern){
        try {
            return screen.find(pattern);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Try-Catch wrapper for Sikuli find() method
     *
     * @param region Region in which to search
     * @param pattern Pattern to search for
     * @return The match object resulting from a successful search
     */
    Match tryFind(Region region, Pattern pattern) {
        try {
            return region.find(pattern);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Try-Catch wrapper with error logging for Sikuli findAll() method
     *
     * @param region Region in which to search
     * @param pattern Pattern to search for
     * @return The match object resulting from a successful search
     */
    Iterator<Match> tryFindAll(Region region, Pattern pattern) {
        try {
            menu.offMenu.hover();
            return region.findAll(pattern);
        }
        catch (Exception e) {
            logException(e);
            Sikulix.popError("Error: Pattern not found in region");
            return null;
        }
    }

    /**
     * Try-Catch wrapper with error logging for Thread.sleep() method
     *
     * @param ns Nano-seconds to sleep for
     */
    private void trySleep(int ns) {
        try {
            Thread.sleep(ns);
        }
        catch (Exception e) {
            logException(e);
            Sikulix.popError("Error: Sleep error");
        }
    }

    /**
     * Try-Catch wrapper with error logging for Sikuli wait() method
     *
     * @param region Region in which to search
     * @param pattern Pattern to search for
     * @return The match object resulting from a successful search
     */
    Match tryWait(Region region, Pattern pattern) {
        try {
            menu.offMenu.hover();
            return region.wait(pattern, 5);
        } catch (Exception e) {
            logException(e);
            Sikulix.popError("Error: Pattern not found in region");
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

    /**
     * Do a shift click, spaced out with sleeps, to keep CPS low
     *
     * @param screen Screen on which to type
     * @param match Match on which to click
     */
    void doShiftClick(Screen screen, Match match) {
        screen.keyDown(Key.SHIFT);
        trySleep(100);
        location(match).click();
        trySleep(100);
        screen.keyUp();
        randSleep();
    }

    /**
     * Overloaded method for left clicking without moving the mouse
     */
    private void doLeftClick() {
        trySleep(200);
        menu.s.click();
        randSleep();
    }

    /**
     * Do a left click, spaced out with sleeps, to keep CPS low
     *
     * @param location Location to click at
     */
    void doLeftClick(Location location) {
        trySleep(200);
        location.click();
        randSleep();
    }

    /**
     * Overloaded method for right clicking without moving the mouse
     */
    private void doRightClick() {
        trySleep(200);
        menu.s.rightClick();
        randSleep();
    }

    /**
     * Overloaded method for right clicking on a Match object
     *
     * @param match Match object to click on
     */
    private void doRightClick(Match match) {
        doRightClick(location(match));
    }

    /**
     * Do a right click, spaced out with sleeps, to keep CPS low
     *
     * @param location Location to click at
     */
    private void doRightClick(Location location) {
        trySleep(200);
        location.rightClick();
        randSleep();

    }

    /**
     * Press the 'e' key, spaced out with sleeps, to keep CPS low and pad for latency
     *
     * @param screen Screen object on which to type
     */
    private void typeE(Screen screen) {
        trySleep(100);
        screen.type("e");
        trySleep(200);
        randSleep();
    }

    /**
     * Checks if the pause hotkey was hit, and if so, sleeps until it's hit again
     */
    void checkHotKeys() {
        while (hotKeyP) {
            trySleep(1000);
        }
    }

    /**
     * Main enchant loop, runs through the crafting/buying process until finished
     */
    private void enchantLoop() {
        // Click on the app window to focus it
        doLeftClick(menu.offMenu);
        Match m;

        // Main loop, that runs through all the steps until interrupt sequence is detected, or enchant limit is reached
        while (run) {
            checkHotKeys();

            item.doEnchant(menu);
            view.progressBar.setValue(item.totalEnchanted);
            if (item.finishedEnchanting()) return;
            if (!run) return;

            typeE(menu.s);

            if (menu.itemSource == 0) {
                doLeftClick();

                m = menu.findBuy(item);
                if (m == null) return;

                doRightClick(m);

                item.doBuy(menu);
            } else {
                doRightClick();
                item.getFromChest(menu);
            }

            if (!run) return;

            typeE(menu.s);
            typeE(menu.s);

            if (!menu.verifyNetherStar()) return;
            doLeftClick(menu.nethStar);

            if (tryWait(menu.buyRegion, menu.barrier) == null) return;
            doLeftClick(menu.craftingBench);
        }

    }

    /**
     * Main control loop, manages, the settings window, and the crafting loop
     */
    private void getSettings() {
        while (true) {
            if (!run) {
                trySleep(1000);
            } else {
                Settings.MoveMouseDelay = (float) 0.0;
                Settings.MinSimilarity = view.minSimilarity;

                // Path where images are stored
                ImagePath.add("AutoEnchanter/images");

                menu = new Menu(this);
                if(!run) {
                    view.settingsEnabled(true);
                    continue;
                }

                item = menu.createItem();

                menu.itemSource = view.itemSource;
                item.stacksToMake = view.stacksToMake;
                item.stacksPerEnchant = view.stacksPerEnchant;

                enchantLoop();

                run = false;

                view.settingsEnabled(true);
            }
        }
    }

    /**
     * Main function run upon initialization that starts the application by creating a new AutoEnchanter instance
     *
     * @param args Not used
     */
    public static void main(String[] args) {
        new AutoEnchanter();
    }
}