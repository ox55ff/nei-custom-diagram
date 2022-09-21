package com.github.dcysteine.neicustomdiagram.generators.gregtech5.materialparts;

import com.github.dcysteine.neicustomdiagram.api.diagram.component.Component;
import com.github.dcysteine.neicustomdiagram.api.diagram.component.ItemComponent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import gregtech.api.enums.ItemList;
import gregtech.common.blocks.GT_Block_Casings5;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import static com.github.dcysteine.neicustomdiagram.generators.gregtech5.materialparts.HeatingCoilHandler.HeatingCoilLevel.*;

class HeatingCoilHandler {
    /** List of all heating coil items in GregTech. Unfortunately, must be manually updated. */
    private static final ImmutableList<ItemList> HEATING_COILS =
            ImmutableList.of(
                    ItemList.Casing_Coil_Cupronickel,
                    ItemList.Casing_Coil_Kanthal,
                    ItemList.Casing_Coil_Nichrome,
                    ItemList.Casing_Coil_TungstenSteel
//                    ItemList.Casing_Coil_HSSG,
//                    ItemList.Casing_Coil_Naquadah,
//                    ItemList.Casing_Coil_NaquadahAlloy,
//                    ItemList.Casing_Coil_ElectrumFlux,
//                    ItemList.Casing_Coil_AwakenedDraconium,
//                    ItemList.Casing_Coil_HSSS,
//                    ItemList.Casing_Coil_Trinium
            );

    /** Sorted map of blast furnace recipe heat to heating coil item. */
    private ImmutableSortedMap<Long, Component> heatingCoilMap;

    // https://github.com/GTNewHorizons/GT5-Unofficial/blob/master/src/main/java/gregtech/api/enums/HeatingCoilLevel.java
    public enum HeatingCoilLevel {
        None, //                       0
        ULV, // Not implemented      901
        LV, // Cupronickel         1801
        MV, // KANTHAL             2701
        HV, // NICHROME            3601
        EV, // TUNGSTENSTEEL       4501
        IV, // HSSG                5401
        LuV, // HSSS                6301
        ZPM, // NAQUADAH            7201
        UV, // NAQUADAHALLOY       8101
        UHV, // TRINIUM             9001
        UEV, // ELECTRUMFLUX        9901
        UIV, // AWAKENEDDRACONIUM  10801
        UMV, // INFINITY		   11701
        UXV, // HYPOGEN 		   12601
        MAX, // ETERNAL			   13501
        ;

        private static final HeatingCoilLevel[] VALUES = values();

        /**
         * @return the coil heat, used for recipes in the Electronic Blast Furnace for example.
         */
        public long getHeat() {
            return this == None ? 0 : 1L + (900L * this.ordinal());
        }

        /**
         * @return the coil tier, used for discount in the Pyrolyse Oven for example.
         */
        public byte getTier() {
            return (byte) (this.ordinal() - 2);
        }

        /**
         * @return the coil Level, used for Parallels in the Multi Furnace for example.
         */
        public byte getLevel() {
            return (byte) (1 << Math.min(Math.max(0, this.ordinal() - 2), 4));
        }

        /**
         * @return the coil Discount, used for discount in the Multi Furnace for example
         */
        public int getCostDiscount() {
            return 1 << Math.max(0, this.ordinal() - 5);
        }

        public String getName() {
            return StatCollector.translateToLocal("GT5U.coil." + this);
        }

        public static HeatingCoilLevel getFromTier(byte tier) {
            if (tier < 0 || tier > getMaxTier()) return HeatingCoilLevel.None;

            return VALUES[tier + 2];
        }

        public static int size() {
            return VALUES.length;
        }

        public static int getMaxTier() {
            return VALUES.length - 1 - 2;
        }
    }

    // https://github.com/GTNewHorizons/GT5-Unofficial/blob/master/src/main/java/gregtech/common/blocks/GT_Block_Casings5.java#L94
    public static HeatingCoilLevel getCoilHeatFromDamage(int meta) {
        switch (meta) {
            case 0:
                return LV;
            case 1:
                return MV;
            case 2:
                return HV;
            case 3:
                return EV;
            case 4:
                return IV;
            case 5:
                return ZPM;
            case 6:
                return UV;
            case 7:
                return UEV;
            case 8:
                return UIV;
            case 9:
                return LuV;
            case 10:
                return UHV;
            case 11:
                return UMV;
            case 12:
                return UXV;
            case 13:
                return MAX;
            default:
                return None;
        }
    }

    /** This method must be called before any other methods are called. */
    void initialize() {
        ImmutableSortedMap.Builder<Long, Component> builder = ImmutableSortedMap.naturalOrder();
        for (ItemList item : HEATING_COILS) {
            ItemStack itemStack = item.get(1);
            long heat =
                    getCoilHeatFromDamage(itemStack.getItemDamage()).getHeat();

            builder.put(heat, ItemComponent.create(itemStack));
        }
        heatingCoilMap = builder.build();
    }

    /**
     * Returns a sorted map of heating coils that support the specified heat level.
     *
     * <p>The returned map is a map of heating coil base heat capacity to heating coil.
     */
    ImmutableSortedMap<Long, Component> getHeatingCoils(long heat) {
        return heatingCoilMap.tailMap(heat, true);
    }
}
