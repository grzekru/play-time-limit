# Play Time Limit

Play Time Limit is a RuneLite plugin for players who want a hard daily stop signal.
It tracks your total in-game time for the current day and starts warning you once you cross your limit.

## Features

- Daily timer that survives relogs and multiple sessions.
- Repeating reminders after the daily limit is reached.
- Three warning channels: in-game chat, desktop notification, and system beep.
- Full-screen red flash overlay for an unmistakable "stop" signal.

## Configuration

- `Daily limit (minutes)` - total play time allowed in one calendar day.
- `Reminder every (minutes)` - interval between repeated warnings after going over limit.
- `Chat warning` - send warning text to game chat.
- `Desktop notification` - show OS notification popups.
- `Sound alert` - play a short system beep with warnings.

## Run Locally

```bash
./gradlew run
```