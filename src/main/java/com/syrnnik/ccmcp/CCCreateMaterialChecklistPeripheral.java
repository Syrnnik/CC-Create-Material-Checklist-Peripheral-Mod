package com.syrnnik.ccmcp;

import com.mojang.logging.LogUtils;
import dan200.computercraft.api.ComputerCraftAPI;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CCCreateMaterialChecklistPeripheral.MODID)
public final class CCCreateMaterialChecklistPeripheral {
  public static final String MODID = "ccmcp";
  public static final Logger LOGGER = LogUtils.getLogger();

  public CCCreateMaterialChecklistPeripheral() {
    ComputerCraftAPI.registerGenericSource(new ClipboardChecklistPeripheral());
    LOGGER.info("Registered Create Clipboard checklist peripheral");
  }
}
