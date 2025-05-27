# CreativeRequests Plugin

A Paper plugin for Minecraft 1.21.4 that allows players to request creative mode, which staff members can approve or deny through an intuitive command system.

## Features

- **Player Request System**: Players can request creative mode with optional reasoning
- **Join Message**: Players are welcomed with information about creative requests when they join
- **Admin Management**: Staff can view, approve, and deny requests through simple commands
- **Enhanced List Command**: View pending, approved, denied, or all requests with detailed history
- **Persistent Storage**: All requests are saved to YAML files for persistence across server restarts
- **Configurable Settings**: Customize plugin behavior through config.yml
- **Permission-Based**: Secure permission system to control access
- **Modern Adventure API**: Uses the latest Minecraft text components for rich formatting

## Installation

1. Download the `CreativeRequests-1.2.1.jar` file
2. Place it in your Paper 1.21.4 server's `plugins/` folder
3. Start or restart your server
4. Configure permissions for your players and staff
5. Optionally edit the generated `config.yml` file in `plugins/CreativeRequests/`

## Commands

### For Players

#### `/request creative [reason]`
- **Description**: Request creative mode from staff
- **Permission**: `creativerequests.request` (default: true)
- **Usage Examples**:
  - `/request creative` - Request creative mode without a reason
  - `/request creative Building a new spawn area` - Request with a specific reason
- **Notes**: 
  - Players already in creative mode cannot make requests
  - Only one pending request per player is allowed
  - Reasons are optional unless configured otherwise

### For Staff/Admins

#### `/requests [list [type]]`
- **Description**: View creative mode requests by type
- **Permission**: `creativerequests.admin` (default: op)
- **Usage Examples**:
  - `/requests` - View all pending requests (default)
  - `/requests list` - View all pending requests
  - `/requests list pending` - View only pending requests
  - `/requests list approved` - View only approved requests
  - `/requests list denied` - View only denied requests
  - `/requests list all` - View all requests (pending, approved, and denied)
- **Shows**: Player name, reason (if provided), timestamp, and action dates for each request

#### `/requests approve <player>`
- **Description**: Approve a player's creative mode request
- **Permission**: `creativerequests.admin` (default: op)
- **Usage Example**: `/requests approve Steve`
- **Effect**: 
  - Sets the player to creative mode
  - Removes the request from pending list
  - Notifies the player of approval
  - Schedules automatic return to survival mode (if configured)

#### `/requests deny <player>`
- **Description**: Deny a player's creative mode request
- **Permission**: `creativerequests.admin` (default: op)
- **Usage Example**: `/requests deny Steve`
- **Effect**: 
  - Removes the request from pending list
  - Notifies the player of denial

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `creativerequests.request` | Allows players to request creative mode | `true` |
| `creativerequests.admin` | Allows managing creative mode requests | `op` |

## Configuration

The plugin generates a `config.yml` file in `plugins/CreativeRequests/` with the following options:

```yaml
# Notify online admins when a new request is made
notify-admins: true

# Require players to provide a reason for their request
allow-reason: true

# Duration in minutes before automatically returning to survival mode (0 = never)
creative-duration: 60
```

### Configuration Options

- **notify-admins**: When `true`, all online staff members with admin permissions will be notified when a player makes a new request
- **allow-reason**: When `true`, players can optionally provide a reason. When `false`, reasons are required
- **creative-duration**: Number of minutes before automatically switching approved players back to survival mode. Set to `0` to disable automatic switching

## File Structure

The plugin creates the following files:

```
plugins/CreativeRequests/
├── config.yml          # Plugin configuration
└── requests.yml        # Stored pending requests (auto-generated)
```

## Workflow Example

1. **Player joins the server**:
   ```
   Welcome! Need creative mode? Use /request creative to get creative!
   ```

2. **Player makes a request**:
   ```
   Player: /request creative Building a castle for the community
   System: ✅ Your creative mode request has been submitted!
   ```

3. **Admin receives notification** (if enabled):
   ```
   [Admin] Steve has requested creative mode: Building a castle for the community
   ```

4. **Admin reviews requests**:
   ```
   Admin: /requests
   System: === Pending Creative Requests ===
           Steve: Building a castle for the community (2 minutes ago)
   
   Admin: /requests list approved
   System: === Approved Creative Requests ===
           Alex: Building spawn area (approved 1 hour ago)
   
   Admin: /requests list all
   System: === ALL CREATIVE MODE REQUESTS ===
           [Shows pending, approved, and denied requests]
   ```

5. **Admin approves the request**:
   ```
   Admin: /requests approve Steve
   System: ✅ Steve's creative mode request has been approved
   Player receives: ✅ Your creative mode request has been approved!
   ```

6. **Automatic return to survival** (if configured):
   ```
   After 60 minutes: Steve has been returned to survival mode
   ```

## Troubleshooting

### Common Issues

**Players can't use `/request creative`**
- Check that they have the `creativerequests.request` permission
- Ensure they're not already in creative mode
- Verify they don't have a pending request already

**Admins can't see or manage requests**
- Verify they have the `creativerequests.admin` permission
- Check that there are actually pending requests with `/requests`

**Plugin not loading**
- Ensure you're running Paper 1.21.4 or compatible
- Check the server console for any error messages
- Verify the plugin JAR is in the correct `plugins/` folder

### Support

For issues, suggestions, or contributions:
- Create an issue on the project repository
- Contact the SkyeNetwork Team

## Technical Details

- **Minecraft Version**: 1.21.4+
- **Server Software**: Paper (required)
- **Java Version**: 21+
- **API Version**: 1.21
- **Dependencies**: None (uses Paper API only)

## Authors

- NobleSkye
- SkyeNetwork Team

## Version

Current version: 1.2.1

---

*This plugin was created for the SkyeNetwork server community.*
