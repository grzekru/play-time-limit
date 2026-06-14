# Play Time Limit

A RuneLite plugin that tracks your total daily play time and strongly alerts you after the limit is exceeded.

## Features

- Tracks total play time for the current day (across multiple sessions).
- Sends warnings after crossing the limit: chat message, desktop notification, and beep.
- Flashes the game screen in red after the limit is exceeded.
- Repeats reminders at the configured interval.

## Configuration

- `Daily total limit (minutes)` - total daily playtime limit in minutes.
- `Reminder interval (minutes)` - how often warnings should repeat.
- `Show chat warning` - enable/disable chat warnings.
- `Desktop notification` - enable/disable desktop notifications.
- `Play beep` - enable/disable beep sound.

## Run Locally

```bash
./gradlew run
```