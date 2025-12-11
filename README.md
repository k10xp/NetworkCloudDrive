# Network Cloud Drive

Local network storage API that can store files and manage them. Uses SQLite for retrieving files and finding paths fast

[FEATURES](FEATURES.md)

### *Temporarily using HTTP Basic as authentication*

## Future plans
- React based Frontend for Desktop and iOS/Android phones
- Routinely syncing database with filesystem and a way to force a resync

## File Structure Visual

```mermaid
flowchart TD
    A[Project Path] -->|Storage| B{root/base}
    B -->|User 1 storage| C[User 1]
    B -->|User 2 storage| D[User 2]
    B -->|User 3 storage| E[User 3]
```
        
