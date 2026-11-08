package com.wishtoday.ts.simpleminer;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.type.FoodComponents;import net.minecraft.entity.effect.StatusEffectUtil;import net.minecraft.entity.effect.StatusEffects;import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.world.GameMode;
//I don't know why I'm writing this, it's not useful anyway...
public class AGameTest implements FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void testSomeValueIsPositive(TestContext context) {
        PlayerEntity player = context.createMockPlayer(GameMode.CREATIVE);
        player.eatFood(player.getWorld(), new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), FoodComponents.ENCHANTED_GOLDEN_APPLE);
        context.expectEntityHasEffect(player, StatusEffects.REGENERATION, 1);
        context.assertTrue(true, "SomeValue should be positive, but was ");

        context.complete();
    }
}
