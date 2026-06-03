# CC: Create Material Checklist Peripheral
# Usage:
#   just --list
#   just setup
#   just compile
#   just run-client

set shell := ["bash", "-cu"]

# Show available commands.
default:
    @just --list

# Install basic tools on macOS through Homebrew.

# Gradle itself is downloaded by ./gradlew, so installing Gradle globally is not needed.
install-tools-macos:
    @command -v brew >/dev/null || (echo "Homebrew is not installed. Install it first: https://brew.sh" && exit 1)
    brew install just openjdk@21
    @echo ""
    @echo "If java -version does not show Java 21, run:"
    # shellcheck disable=SC2016
    @echo '  export PATH="$$(brew --prefix openjdk@21)/bin:$$PATH"'
    # shellcheck disable=SC2016
    @echo '  export JAVA_HOME="$$(/usr/libexec/java_home -v 21)"'

# Install basic tools on Arch/Manjaro.
install-tools-arch:
    sudo pacman -S --needed just jdk21-openjdk

# Install basic tools on Ubuntu/Debian.
install-tools-ubuntu:
    sudo apt update
    sudo apt install -y just openjdk-21-jdk

# Prepare the project after unpacking it.
setup:
    chmod +x ./gradlew
    ./gradlew --version

# Check Java and Gradle setup.
doctor:
    @echo "Java:"
    java -version
    @echo ""
    @echo "Gradle wrapper:"
    chmod +x ./gradlew
    ./gradlew --version

# Download/refresh all Gradle dependencies.
deps:
    chmod +x ./gradlew
    ./gradlew --refresh-dependencies dependencies --configuration runtimeClasspath

# Show all Gradle tasks.
tasks:
    chmod +x ./gradlew
    ./gradlew tasks

# Compile Java sources.
compile:
    chmod +x ./gradlew
    ./gradlew compileJava

# Run Gradle checks. There is no separate linter configured yet, so this is the current lint-like command.
lint:
    chmod +x ./gradlew
    ./gradlew check

# Clean build outputs.
clean:
    chmod +x ./gradlew
    ./gradlew clean

# Build the mod jar.
build:
    chmod +x ./gradlew
    ./gradlew build

# Build the mod jar.
clean-build:
    just clean
    just build

# Clean and build from scratch.
rebuild:
    chmod +x ./gradlew
    ./gradlew clean build

# Run Minecraft client in the NeoForge dev environment.
run-client:
    chmod +x ./gradlew
    ./gradlew runClient

# Run dedicated server in the NeoForge dev environment.
run-server:
    chmod +x ./gradlew
    ./gradlew runServer

# Generate data/resources if needed.
run-data:
    chmod +x ./gradlew
    ./gradlew runData

# Print built jar files.
jar:
    @ls -lh build/libs/*.jar

# Build and copy jar to a mods folder.
# Example:

# just install-jar "$HOME/Library/Application Support/PrismLauncher/instances/Test/.minecraft/mods"
install-jar mods_dir:
    chmod +x ./gradlew
    ./gradlew build
    mkdir -p "{{ mods_dir }}"
    cp build/libs/*.jar "{{ mods_dir }}/"
    @echo "Copied jar to: {{ mods_dir }}"

# Remove Gradle project cache. Use when dependencies/cache behave strangely.
clean-gradle-cache:
    rm -rf .gradle
