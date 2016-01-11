package flaxbeard.steamcraft.api.tool;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import flaxbeard.steamcraft.misc.OreDictHelper;
import flaxbeard.steamcraft.misc.RecipeHelper;

import java.util.ArrayList;

public class UtilSteamTool {
    /**
     * Checks if the drill has a particular upgrade.
     * @param me The ItemStack version of the drill
     * @param check The item that is being checked against, or the upgrade
     * @return Whether it has any upgrades.
     */
    public static boolean hasUpgrade(ItemStack me, Item check) {
        if (check == null) {
            return false;
        }

        if (me.hasTagCompound() && me.stackTagCompound.hasKey("upgrades")) {
            for (int i = 1; i < 10; i++) {
                if (me.stackTagCompound.getCompoundTag("upgrades").hasKey(Integer.toString(i))) {
                    ItemStack stack = ItemStack.loadItemStackFromNBT(me.stackTagCompound.getCompoundTag("upgrades").getCompoundTag(Integer.toString(i)));
                    if (stack.getItem() == check) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets all of the upgrades (except non-standard ones that do not implement ISteamToolUpgrade)
     * that are installed in the tool
     * @param me The tool ItemStack.
     * @return The ArrayList of all the upgrades, or null.
     */
    public static ArrayList<ISteamToolUpgrade> getUpgrades(ItemStack me) {
        ArrayList<ISteamToolUpgrade> upgrades = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            if (me.stackTagCompound.getCompoundTag("upgrades").hasKey(Integer.toString(i))) {
                ItemStack stack = ItemStack.loadItemStackFromNBT(
                  me.stackTagCompound.getCompoundTag("upgrades")
                    .getCompoundTag(Integer.toString(i)));
                if (stack != null) {
                    Item item = stack.getItem();
                    if (item != null && item instanceof ISteamToolUpgrade) {
                        upgrades.add((ISteamToolUpgrade) item);
                    }
                }
            }
        }

        if (upgrades.isEmpty()) {
            return null;
        }

        return upgrades;
    }

    /**
     * Gets the Harvest Level upgrade installed in the tool. Though it returns an arraylist for
     * ultimate mod compatibility, chances are it will be of size 1 or null.
     * @param me The tool ItemStack.
     * @return null if there is no harvest level modifier, otherwise the ArrayList of the Items.
     */
    public static ArrayList<Item> getHarvestLevelModifiers(ItemStack me) {
        ArrayList<Item> ret = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (me.stackTagCompound.getCompoundTag("upgrades").hasKey(Integer.toString(i))) {
                ItemStack stack = ItemStack.loadItemStackFromNBT(
                  me.stackTagCompound.getCompoundTag("upgrades").getCompoundTag(Integer.toString(i)));
                if (stack != null) {
                    Item item = stack.getItem();
                    if (item != null && RecipeHelper.blockMaterials.keySet().contains(item)) {
                        ret.add(item);
                    }
                }
            }
        }

        if (ret.isEmpty()) {
            return null;
        }

        return ret;
    }
}