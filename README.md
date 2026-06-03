# CC: Create Material Checklist Peripheral

A small NeoForge addon for Minecraft `1.21.1` that lets **CC:Tweaked** computers read **Create Material Checklists** from placed **Create Clipboards**.

The mod does not add new blocks, items, or GUIs. It adds Lua peripheral methods to Create's placed Clipboard block entity.

## What it does

Create can print a Schematicannon Material Checklist onto a Clipboard. Normally, CC:Tweaked can only see the item name and an opaque NBT/data hash, not the checklist contents.

This addon reads Create's real Clipboard data component on the Java side and exposes the useful parts to Lua.

You can use it to:

- read all Clipboard pages and entries;
- read item rows from a printed Material Checklist;
- get a compact list of missing materials;
- use that list for storage systems, autocrafting, logistics, or dashboards.

## Requirements

Target versions used by this project:

| Dependency | Version |
|---|---:|
| Minecraft | `1.21.1` |
| NeoForge | `21.1.233` |
| Create | `6.0.10-280` |
| CC:Tweaked | `1.119.0` |
| Java | `21` |

## Development setup

### macOS

If you use the included `justfile`:

```bash
just install-tools-macos
```

If `java -version` still does not show Java 21, add Java 21 to your shell path:

```bash
export PATH="$(brew --prefix openjdk@21)/bin:$PATH"
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
```

### Linux

Arch/Manjaro:

```bash
just install-tools-arch
```

Ubuntu/Debian:

```bash
just install-tools-ubuntu
```

## Common commands

```bash
just setup        # make Gradle wrapper executable and print Gradle info
just deps         # download/refresh Gradle dependencies
just compile      # compile Java sources
just lint         # run Gradle checks
just build        # build the mod jar
just run-client   # run Minecraft client in the dev environment
just run-server   # run a dev server
just jar          # list built jar files
```

Equivalent Gradle commands:

```bash
./gradlew compileJava
./gradlew check
./gradlew build
./gradlew runClient
```

The built mod jar will be in:

```text
build/libs/
```

## How to test in game

1. Start the dev client:

   ```bash
   just run-client
   ```

2. Create a Material Checklist using Create's Schematicannon and a Create Clipboard.

3. Place the written Clipboard in the world.

4. Connect it to a CC:Tweaked computer using a wired modem, or place the computer/modem so the Clipboard is visible as a peripheral.

5. In Lua, inspect available peripherals:

   ```lua
   for _, name in ipairs(peripheral.getNames()) do
     print(name, peripheral.getType(name))
     print(textutils.serialize(peripheral.getMethods(name)))
   end
   ```

6. Wrap the Clipboard peripheral and read missing items:

   ```lua
   local cb = peripheral.wrap("right") -- replace with the real side or peripheral name

   print("Has content:", cb.hasClipboardContent())
   print("Type:", cb.getClipboardType())

   local missing = cb.getMissingItems()

   for _, item in ipairs(missing) do
     print(item.name .. " x" .. item.count)
   end
   ```

## Lua API

The peripheral is attached to placed Create Clipboards.

### `hasClipboardContent()`

Returns `true` when the Clipboard has Create Clipboard content.

```lua
local hasContent = cb.hasClipboardContent()
```

### `getClipboardType()`

Returns the Create Clipboard type as a lowercase string, or `"empty"` if no content exists.

```lua
local type = cb.getClipboardType()
```

### `getPageCount()`

Returns the number of pages on the Clipboard.

```lua
local pages = cb.getPageCount()
```

### `getPages()`

Returns all Clipboard pages as nested Lua tables.

```lua
local pages = cb.getPages()
print(textutils.serialize(pages))
```

Shape:

```lua
{
  {
    {
      checked = false,
      text = "Stone x128",
      itemAmount = 128,
      item = {
        name = "minecraft:stone",
        displayName = "Stone",
        count = 1,
        maxCount = 64,
      },
    },
  },
}
```

### `getEntries()`

Returns all entries from all pages in one flat list.

Each entry includes `page` and `index`.

```lua
local entries = cb.getEntries()
```

### `getItemEntries()`

Returns only entries that have an item icon.

This includes completed rows too, but for completed Material Checklist rows, `itemAmount` may be `0` because Create stores the missing amount there.

```lua
local items = cb.getItemEntries()
```

### `getMissingItems()`

Returns a compact grouped list of unfinished Material Checklist item rows.

```lua
local missing = cb.getMissingItems()

for _, item in ipairs(missing) do
  print(item.name, item.count)
end
```

Shape:

```lua
{
  {
    name = "minecraft:stone",
    displayName = "Stone",
    count = 128,
  },
  {
    name = "create:shaft",
    displayName = "Shaft",
    count = 32,
  },
}
```

## Important limitations

### It reads placed Create Clipboards, not inventory item stacks

This addon is designed for a Clipboard placed in the world as a block entity. It does not make normal CC:Tweaked inventory methods expose raw item data components.

### It is best for missing materials

Create's printed Material Checklist stores unfinished rows with an item icon and a positive `itemAmount`. Completed rows are usually checked and may have `itemAmount = 0`.

So `getMissingItems()` is reliable for answering:

```text
What materials are still missing?
```

It is not meant to reconstruct the original full `required / gathered / missing` state after the checklist has already been completed.

### It does not parse CC:Tweaked's `nbt` hash

CC:Tweaked exposes only an opaque hash for item data in inventory APIs. This addon avoids that by reading Create's Clipboard data directly on the Java side.

## Project structure

```text
src/main/java/com/syrnnik/ccmcp/
  CCCreateMaterialChecklistPeripheral.java   # main mod class, registers the generic peripheral
  ClipboardChecklistPeripheral.java          # Lua API for placed Create Clipboards

src/main/templates/META-INF/neoforge.mods.toml
  Mod metadata and dependencies

src/main/resources/assets/ccmcp/lang/en_us.json
  English mod display name

build.gradle
  NeoForge, Create, and CC:Tweaked dependencies

gradle.properties
  Version constants and mod metadata

justfile
  Convenience commands for setup, build, run, and checks
```

## Troubleshooting

### `java -version` is not Java 21

Install Java 21 and set `JAVA_HOME` correctly.

On macOS with Homebrew:

```bash
export PATH="$(brew --prefix openjdk@21)/bin:$PATH"
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
```

### The Clipboard does not appear in `peripheral.getNames()`

Check that:

- the Clipboard is placed in the world;
- it is connected through a CC:Tweaked wired modem or adjacent peripheral connection;
- the modem is activated;
- the mod is installed on both client and server in multiplayer;
- Create and CC:Tweaked versions match the target versions.

### `getMissingItems()` returns an empty list

Possible reasons:

- the Clipboard is empty;
- the Clipboard is not a printed Material Checklist;
- all checklist rows are completed;
- the checklist stores visible text but no item icons.

Use this for debugging:

```lua
print(textutils.serialize(cb.getPages()))
```
