
We need to explicitly enable Redis keyspace notification.

# Notifications for value changes at key

To enable keyspace notification (`K`) for list commands (`l`) and stream commands (`t`) 
we can use `redis-cli` at runtime: 

```shell
CONFIG SET notify-keyspace-events Klt
```

Above configs will enable notification like `xadd` or `xdel` at key `user:6345:stream` 
on database `0` via channel `__keyspace@0__:user:6345:stream`.

# Notifications for command invocations

To enable notification for specific event (`E`) regardless the key, for list commands (`l`)
and set commands (`s`) we can use `redis-cli` at runtime:

```shell
CONFIG SET notify-keyspace-events Els
```

Above configs will enable notifications like `user:3546:processes` or `user:1985:processes`
when executing `rpush` on those keys. These notifications are available via channel `__keyevent@0__:rpush`. 

# Configuration details

| Config Character | Description                                                                                               |
|------------------|-----------------------------------------------------------------------------------------------------------|
| K                | Keyspace events, published with __keyspace@<db>__ prefix.                                                 |
| E                | Keyevent events, published with __keyevent@<db>__ prefix.                                                 |
| g                | Generic commands (non-type specific) like DEL, EXPIRE, RENAME, ...                                        |
| $                | String commands                                                                                           |
| l                | List commands                                                                                             |
| s                | Set commands                                                                                              |
| h                | Hash commands                                                                                             |
| z                | Sorted set commands                                                                                       |
| t                | Stream commands                                                                                           |
| d                | Module key type events                                                                                    |
| x                | Expired events (events generated every time a key expires)                                                |
| e                | Evicted events (events generated when a key is evicted for maxmemory)                                     |
| m                | Key miss events generated when a key that doesn't exist is accessed (Note: not included in the 'A' class) |
| n                | New key events generated whenever a new key is created (Note: not included in the 'A' class)              |
| o                | Overwritten events generated every time a key is overwritten (Note: not included in the 'A' class)        |
| c                | Type-changed events generated every time a key's type changes (Note: not included in the 'A' class)       |
| A                | Alias for "g$lshztdxe", so that the "AKE" string means all the events except "m", "n", "o" and "c".       |
