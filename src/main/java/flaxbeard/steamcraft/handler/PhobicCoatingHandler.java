package flaxbeard.steamcraft.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import flaxbeard.steamcraft.Config;
import flaxbeard.steamcraft.SteamcraftItems;
import flaxbeard.steamcraft.item.ItemExosuitArmor;


import java.util.UUID;

public class PhobicCoatingHandler {
    private static final UUID uuid4 = UUID.fromString("ddb705e1-f95d-40e7-8cda-1be73a4739a1");
    private static final AttributeModifier exoWaterBoost = new AttributeModifier(uuid4, "EXOWATERBOOST", 0.4D, 2).setSaved(true);
    private static final UUID uuid5 = UUID.fromString("ecc2b61a-b8ca-4411-9158-e4d365e3ca7c");
    private static final AttributeModifier exoLavaBoost = new AttributeModifier(uuid5, "EXOLAVABOOST", 0.4D, 2).setSaved(true);
    private boolean isWalkingInLava = false;

    @SubscribeEvent
    public void preventLavaDamage(LivingAttackEvent event) {
        if (isWalkingInLava && (event.source == DamageSource.lava ||
          event.source == DamageSource.inFire || event.source == DamageSource.onFire)) {
            event.entity.motionY = 0.5D;
            event.setCanceled(true);
        }
    }

    private boolean isJumping = false;

    @SubscribeEvent
    public void walkOnFluid(TickEvent.PlayerTickEvent event) {
        EntityPlayer entity = event.player;
        int consumptionHydro = Config.hydrophobicConsumption;
        int consumptionPyro = Config.pyrophobicConsumption;
        IAttributeInstance attributes = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
        AttributeModifier modifierWater = attributes.getModifier(uuid4);
        AttributeModifier modifierLava = attributes.getModifier(uuid5);
        boolean canHydro = canWalkOnFluid(entity, consumptionHydro,
          SteamcraftItems.coatingsHydrophobic, modifierWater);
        boolean canPyro = canWalkOnFluid(entity, consumptionPyro,
          SteamcraftItems.coatingsPyrophobic, modifierLava);
        int x = MathHelper.floor_double(entity.posX);
        int y = MathHelper.floor_double(entity.boundingBox.minY - 0.11F);
        int z = MathHelper.floor_double(entity.posZ);
        Block blockUnder = entity.worldObj.getBlock(x, y, z);
        if (event.side == Side.CLIENT) {
            isJumping = Minecraft.getMinecraft().gameSettings.keyBindJump.getIsKeyPressed();
        }

        if (canHydro) {
            if (blockUnder == Blocks.water || blockUnder == Blocks.flowing_water) {
                entity.fallDistance = 0;

                if (isJumping) {
                    entity.motionY = 0.5D;
                } else {
                    entity.motionY = 0;
                }

                if (modifierWater == null) {
                    attributes.applyModifier(exoWaterBoost);
                }
                SteamcraftEventHandler.drainSteam(entity.getEquipmentInSlot(3), consumptionHydro);
            }

            if (entity.isInWater()) {
                entity.jump();
                entity.fallDistance = 0;
            }
        } else if (modifierWater != null) {
            attributes.removeModifier(exoWaterBoost);
        }


        if (canPyro) {
            if (entity.isBurning()) {
                isWalkingInLava = true;
                entity.extinguish();
                entity.motionY = 0.5;
            }

            if (blockUnder == Blocks.lava || blockUnder == Blocks.flowing_lava) {
                isWalkingInLava = true;
                if (isJumping) {
                    entity.motionY = 0.5D;
                } else {
                    entity.motionY = 0;
                }

                if (modifierLava == null) {
                    attributes.applyModifier(exoLavaBoost);
                }
                SteamcraftEventHandler.drainSteam(entity.getEquipmentInSlot(3), consumptionPyro);
            }

            if (entity.handleLavaMovement()) {
                isWalkingInLava = true;
                entity.motionY = 0.5;
//                entity.jump();
                entity.fallDistance = 0;
            }
        } else if (modifierLava != null) {
            attributes.removeModifier(exoLavaBoost);
        }
    }

    /**
     * Gets whether the Entity can walk on fluids.
     * @param player The EntityPlayer testing against.
     * @param consumption The amount of steam required.
     * @param coating The Exo upgrade needed to walk on fluids.
     * @param modifier The AttributeModifier that cannot exist.
     * @return True if the player can walk on top of fluids.
     */
    private boolean canWalkOnFluid(EntityPlayer player, int consumption, Item coating, AttributeModifier modifier) {
        if (SteamcraftEventHandler.hasPower(player, consumption) && modifier == null) {
            ItemStack equipment = player.getEquipmentInSlot(1);
            if (equipment != null) {
                Item boots = equipment.getItem();
                if (boots instanceof ItemExosuitArmor) {
                    ItemExosuitArmor bootsArmor = (ItemExosuitArmor) boots;
                    if (bootsArmor.hasUpgrade(equipment, coating)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
