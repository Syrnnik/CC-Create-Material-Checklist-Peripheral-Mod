package com.syrnnik.ccmcp;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adds Lua methods to a placed Create Clipboard block entity.
 *
 * Put a Create Clipboard with a printed Material Checklist next to a CC:Tweaked
 * wired modem/computer.
 * Then call the methods from Lua using peripheral.wrap/peripheral.find.
 */
public final class ClipboardChecklistPeripheral implements GenericPeripheral {
  @Override
  public String id() {
    return ResourceLocation.fromNamespaceAndPath(CCCreateMaterialChecklistPeripheral.MODID, "clipboard_checklist")
        .toString();
  }

  @LuaFunction(mainThread = true)
  public boolean hasClipboardContent(ClipboardBlockEntity clipboard) {
    return getContent(clipboard) != null;
  }

  @LuaFunction(mainThread = true)
  public String getClipboardType(ClipboardBlockEntity clipboard) {
    ClipboardContent content = getContent(clipboard);
    if (content == null) {
      return "empty";
    }

    return content.type().name().toLowerCase(Locale.ROOT);
  }

  @LuaFunction(mainThread = true)
  public int getPageCount(ClipboardBlockEntity clipboard) {
    return ClipboardEntry.readAll(clipboard.components()).size();
  }

  /**
   * Returns all clipboard pages as Lua-friendly tables.
   * Useful for debugging and for non-material-checklist clipboards.
   */
  @LuaFunction(mainThread = true)
  public List<List<Map<String, Object>>> getPages(ClipboardBlockEntity clipboard) {
    List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(clipboard.components());
    List<List<Map<String, Object>>> result = new ArrayList<>(pages.size());

    for (List<ClipboardEntry> page : pages) {
      List<Map<String, Object>> luaPage = new ArrayList<>(page.size());

      for (ClipboardEntry entry : page) {
        luaPage.add(entryToMap(entry));
      }

      result.add(luaPage);
    }

    return result;
  }

  /**
   * Returns every clipboard entry in one flat list.
   */
  @LuaFunction(mainThread = true)
  public List<Map<String, Object>> getEntries(ClipboardBlockEntity clipboard) {
    List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(clipboard.components());
    List<Map<String, Object>> result = new ArrayList<>();

    for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
      List<ClipboardEntry> page = pages.get(pageIndex);

      for (int entryIndex = 0; entryIndex < page.size(); entryIndex++) {
        Map<String, Object> entry = entryToMap(page.get(entryIndex));
        entry.put("page", pageIndex + 1);
        entry.put("index", entryIndex + 1);
        result.add(entry);
      }
    }

    return result;
  }

  /**
   * Reads missing material entries from a printed Create Material Checklist.
   *
   * In Create checklists, unfinished material rows are unchecked and contain an
   * item icon with itemAmount > 0.
   * Finished rows usually have checked=true and itemAmount=0, so this method is
   * best for "what is still missing".
   */
  @LuaFunction(mainThread = true)
  public List<Map<String, Object>> getMissingItems(ClipboardBlockEntity clipboard) {
    List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(clipboard.components());
    LinkedHashMap<String, Map<String, Object>> byItem = new LinkedHashMap<>();

    for (List<ClipboardEntry> page : pages) {
      for (ClipboardEntry entry : page) {
        if (entry.checked || entry.icon.isEmpty() || entry.itemAmount <= 0) {
          continue;
        }

        String itemId = getItemId(entry.icon);
        Map<String, Object> item = byItem.computeIfAbsent(itemId, ignored -> {
          Map<String, Object> created = new LinkedHashMap<>();
          created.put("name", itemId);
          created.put("displayName", entry.icon.getHoverName().getString());
          created.put("count", 0);
          return created;
        });

        item.put("count", ((Number) item.get("count")).intValue() + entry.itemAmount);
      }
    }

    return new ArrayList<>(byItem.values());
  }

  /**
   * Returns all item rows from the clipboard, including completed rows.
   * For completed rows in a Material Checklist, itemAmount may be 0 because
   * Create stores only missing amount there.
   */
  @LuaFunction(mainThread = true)
  public List<Map<String, Object>> getItemEntries(ClipboardBlockEntity clipboard) {
    List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(clipboard.components());
    List<Map<String, Object>> result = new ArrayList<>();

    for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
      List<ClipboardEntry> page = pages.get(pageIndex);

      for (int entryIndex = 0; entryIndex < page.size(); entryIndex++) {
        ClipboardEntry entry = page.get(entryIndex);

        if (entry.icon.isEmpty()) {
          continue;
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("page", pageIndex + 1);
        row.put("index", entryIndex + 1);
        row.put("checked", entry.checked);
        row.put("text", entry.text.getString());
        row.put("itemAmount", entry.itemAmount);
        row.put("item", itemToMap(entry.icon));

        result.add(row);
      }
    }

    return result;
  }

  private static ClipboardContent getContent(ClipboardBlockEntity clipboard) {
    return clipboard.components().get(AllDataComponents.CLIPBOARD_CONTENT);
  }

  private static Map<String, Object> entryToMap(ClipboardEntry entry) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("checked", entry.checked);
    result.put("text", entry.text.getString());
    result.put("itemAmount", entry.itemAmount);

    if (!entry.icon.isEmpty()) {
      result.put("item", itemToMap(entry.icon));
    }

    return result;
  }

  private static Map<String, Object> itemToMap(ItemStack stack) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("name", getItemId(stack));
    result.put("displayName", stack.getHoverName().getString());
    result.put("count", stack.getCount());
    result.put("maxCount", stack.getMaxStackSize());
    return result;
  }

  private static String getItemId(ItemStack stack) {
    return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
  }
}
