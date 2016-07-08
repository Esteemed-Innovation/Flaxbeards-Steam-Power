package flaxbeard.steamcraft.item.firearm.enhancement;

import flaxbeard.steamcraft.Steamcraft;
import flaxbeard.steamcraft.api.enhancement.IEnhancementFirearm;
import flaxbeard.steamcraft.entity.projectile.EntityMusketBall;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static flaxbeard.steamcraft.init.items.firearms.FirearmItems.Items.MUSKET;
import static flaxbeard.steamcraft.init.items.firearms.FirearmItems.Items.BLUNDERBUSS;

public class ItemEnhancementSpeedloader extends Item implements IEnhancementFirearm {
    @Override
    public boolean canApplyTo(ItemStack stack) {
        return stack.getItem() == MUSKET.getItem() || stack.getItem() == BLUNDERBUSS.getItem();
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return Steamcraft.upgrade;
    }

    @Override
    public String getID() {
        return "Speedloader";
    }

    @Override
    public String getIcon(Item item) {
        String weapon = item == MUSKET.getItem() ? "Musket" : "Blunderbuss";
        return "steamcraft:weapon" + weapon + "Speedloader";
    }

    @Override
    public String getName(Item item) {
        String weapon = item == MUSKET.getItem() ? "musket" : "blunderbuss";
        return "item.steamcraft:" + weapon + "Speedloader";
    }

    @Override
    public float getAccuracyChange(Item weapon) {
        return 0;
    }

    @Override
    public float getKnockbackChange(Item weapon) {
        return 0;
    }

    @Override
    public float getDamageChange(Item weapon) {
        return 0;
    }

    @Override
    public int getReloadChange(Item weapon) {
        return -30;
    }

    @Override
    public int getClipSizeChange(Item weapon) {
        return 0;
    }

    @Override
    public EntityMusketBall changeBullet(EntityMusketBall bullet) {
        return bullet;
    }

}