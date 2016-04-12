package com.minecolonies.util;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * Recipe storage for minecolonies
 */
public final class RecipeHandler
{

    /**
     * Private constructor to hide the implicit public one
     */
    private RecipeHandler()
    {
    }

    /**
     * Initialize all recipes for minecolonies
     *
     * @param enableInDevelopmentFeatures if we want development recipes
     * @param supplyChests                if we want supply chests or direct town hall crafting
     */
    public static void init(final boolean enableInDevelopmentFeatures, final boolean supplyChests)
    {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutMiner, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_pickaxe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_axe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_door);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutCitizen, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.stick);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFisherman, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.fishing_rod);
        GameRegistry.addRecipe(new ItemStack(ModItems.scanTool, 1), "  I", " S ", "S  ", 'I', Items.iron_ingot, 'S', Items.stick);
        GameRegistry.addRecipe(new ItemStack(ModItems.buildTool, 1), "  C", " S ", "S  ", 'C', Blocks.cobblestone, 'S', Items.stick);

        if (enableInDevelopmentFeatures)
        {
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBaker, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wheat);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutWarehouse, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Blocks.chest);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBlacksmith, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.iron_ingot);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutStonemason, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Blocks.stonebrick);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFarmer, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_hoe);
        }

        if (supplyChests)
        {
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.boat);
        }
        else
        {
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutTownhall, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.boat);
        }
    }
}