name: build

on:
  push:
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 8
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '8'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '7.6.4'

      - name: Build mod
        run: gradle build --no-daemon --stacktrace

      - name: Upload jar
        uses: actions/upload-artifact@v4
        with:
          name: verity-ai-friend-jar
          path: build/libs/*.jar
          if-no-files-found: error
