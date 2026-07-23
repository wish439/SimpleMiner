package com.wishtoday.ts.simpleminer;

import com.wishtoday.simpleservices.services.*;
import com.wishtoday.ts.simpleminer.gui.MinerScreenHandlerTypes;
import net.fabricmc.api.ModInitializer;

public class Simpleminer implements ModInitializer {

    @Override
    public void onInitialize() {
        ServiceAssembler assembler = new ServiceAssembler("com.wishtoday.ts.simpleminer");
        assembler.registerFactoryExtension(new CollectionExtension());
        assembler.registerFactoryExtension(new ExtensionHandlerMapExtension());
        assembler.registerFactoryExtension(new QualifierExtension());
        assembler.registerFactoryExtension(new TwoTypeMapExtension());
        assembler.distributeDepends();
        MinerScreenHandlerTypes.init();
    }
}
