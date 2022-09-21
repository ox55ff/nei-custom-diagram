package com.github.dcysteine.neicustomdiagram.util;

import gregtech.api.GregTech_API;
import gregtech.api.objects.ItemData;

import java.lang.reflect.Field;
import java.util.Map;

import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Utility;
import net.minecraft.item.ItemStack;

import static gregtech.api.util.GT_OreDictUnificator.getAssociation;
import static gregtech.api.util.GT_OreDictUnificator.isBlacklisted;

public class GT_OreDictUnificator_WitchGetNoCopy {
    private static Map<String, ItemStack> sName2StackMap;

    static {
        try {
            Field f = GT_OreDictUnificator.class.getDeclaredField("sName2StackMap");
            f.setAccessible(true);
            sName2StackMap = (Map<String, ItemStack>)f.get(GT_OreDictUnificator.class);
        }
        catch (Exception ignored)
        {}
    }

    /** Doesn't copy the returned stack or set quantity. Be careful and do not mutate it;
     * intended only to optimize comparisons */
    public static ItemStack get_nocopy(ItemStack aStack) {
        return get_nocopy(true, aStack);
    }

    /** Doesn't copy the returned stack or set quantity. Be careful and do not mutate it;
     * intended only to optimize comparisons */
    public static ItemStack get_nocopy(boolean aUseBlackList, ItemStack aStack) {
        if (GT_Utility.isStackInvalid(aStack)) return null;
        ItemData tPrefixMaterial = getAssociation(aStack);
        ItemStack rStack = null;
        if (tPrefixMaterial == null
                || !tPrefixMaterial.hasValidPrefixMaterialData()
                || (aUseBlackList && tPrefixMaterial.mBlackListed)) return aStack;
        if (aUseBlackList && !GregTech_API.sUnificationEntriesRegistered && isBlacklisted(aStack)) {
            tPrefixMaterial.mBlackListed = true;
            return aStack;
        }
        if (tPrefixMaterial.mUnificationTarget == null)
            tPrefixMaterial.mUnificationTarget = sName2StackMap.get(tPrefixMaterial.toString());
        rStack = tPrefixMaterial.mUnificationTarget;
        if (GT_Utility.isStackInvalid(rStack)) return aStack;
        assert rStack != null;
        rStack.setTagCompound(aStack.getTagCompound());
        return rStack;
    }

    /** Compares the first argument against an already-unificated second argument as if
     * aUseBlackList was both true and false. */
    public static boolean isInputStackEqual(ItemStack aStack, ItemStack unified_tStack) {
        boolean alreadyCompared = false;
        if (GT_Utility.isStackInvalid(aStack)) return false;
        ItemData tPrefixMaterial = getAssociation(aStack);
        ItemStack rStack = null;
        if (tPrefixMaterial == null || !tPrefixMaterial.hasValidPrefixMaterialData())
            return GT_Utility.areStacksEqual(aStack, unified_tStack, true);
        else if (tPrefixMaterial.mBlackListed) {
            if (GT_Utility.areStacksEqual(aStack, unified_tStack, true)) return true;
            else alreadyCompared = true;
        }
        if (!alreadyCompared && !GregTech_API.sUnificationEntriesRegistered && isBlacklisted(aStack)) {
            tPrefixMaterial.mBlackListed = true;
            if (GT_Utility.areStacksEqual(aStack, unified_tStack, true)) return true;
            else alreadyCompared = true;
        }
        if (tPrefixMaterial.mUnificationTarget == null)
            tPrefixMaterial.mUnificationTarget = sName2StackMap.get(tPrefixMaterial.toString());
        rStack = tPrefixMaterial.mUnificationTarget;
        if (GT_Utility.isStackInvalid(rStack))
            return !alreadyCompared && GT_Utility.areStacksEqual(aStack, unified_tStack, true);
        rStack.setTagCompound(aStack.getTagCompound());
        return GT_Utility.areStacksEqual(rStack, unified_tStack, true);
    }
}
