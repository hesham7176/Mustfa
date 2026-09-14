# Architecture

The app follows a small layered structure:

- `data`: filesystem models, repository access, and cancellable file operations
- `domain`: folder-cover resolution, search traversal, and media gesture decisions
- `media`: lifecycle-independent persistence primitives for playback state
- `ui`: Compose screens and a ViewModel that owns directory state and user actions

Filesystem work runs on `Dispatchers.IO`. UI state is exposed with `StateFlow`; long-running operations are coroutine-cancellable. Android Storage Access Framework and Media3 integrations should be added behind the existing data/domain boundaries rather than placing I/O or playback logic in Composables.

## Status

The foundation and core local browsing operations are implemented. USB/SAF, full media player surfaces, archive/network/cloud providers, recycle bin, application manager, and advanced settings remain planned integrations and are intentionally not advertised as complete.
