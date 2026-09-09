# Connecting Power BI to GridDB Cloud with the ODBC Driver (and the One Setup Step That Isn't Documented in English)

GridDB ships an ODBC driver, which means any SQL-speaking Windows tool can query GridDB directly: Power BI, Excel, Tableau, Access. For GridDB Cloud users, this is the path to building dashboards on top of time-series data without writing any application code.

The driver works. But if you install it by following the bundled setup script, it will fail on every connection attempt with an instant, empty error, and nothing in the English documentation will tell you why. This post covers the setup, the failure, the systematic diagnosis, and the one-line fix that lives only in the Japanese user guide.

## What's in the download

The ODBC driver ships in the GridDB Cloud library bundle, under an `ODBC/` folder:

```
ODBC/
├── gridstore-odbc-sample.zip
├── GridStoreODBC-sample.cpp / .sln / .vcproj
├── x64/
│   ├── GridStoreODBC64.dll
│   ├── gridstore_advanced.dll
│   ├── libssl-3-x64.dll
│   ├── libcrypto-3-x64.dll
│   ├── msvcp140.dll
│   ├── vcruntime140.dll
│   ├── vcruntime140_1.dll
│   └── GridStoreODBC_64bit_setup.bat
└── x86/
    └── (32-bit equivalents)
```

A few things to understand about what's here:

- **This is the SQL interface**, not the NoSQL native client. ODBC talks to GridDB's SQL port (20001 by default), so it's the same interface as JDBC.
- **`gridstore_advanced.dll` is the client library that does the actual connecting**, including SSL. The OpenSSL DLLs (`libssl`, `libcrypto`) are its dependencies.
- **The `.cpp` sample** connects via a plain DSN name with `SQLConnect`. There's no connection-string keyword format; all parameters live in the DSN.
- **Windows only.** These are DLLs and a `.bat`. If you're on Linux or macOS you need a Windows box or VM.

## Prerequisites

- A 64-bit Windows host (Server or 11 both work).
- **Network access to the GridDB Cloud cluster's SQL port.** For GridDB Cloud this means your host must be inside a VNet peered to the Cloud tenant VNet, or connected via Point-to-Site VPN. The Web API IP whitelist does not grant native/SQL port access.
- **Visual C++ 2015-2022 x64 Redistributable.** The loose `vcruntime140*.dll` files in the folder are not a substitute for installing it properly.
- Power BI Desktop (64-bit, so use the 64-bit driver).

Confirm reachability before installing anything, so a later failure can't be a network problem:

```powershell
Test-NetConnection <node-ip> -Port 20001
```

`TcpTestSucceeded : True` means the SQL port is open and routable.

## Installing the driver

Run `GridStoreODBC_64bit_setup.bat` **as administrator** (it writes to `HKLM` and `C:\Program Files`, both of which need elevation; run it un-elevated and it fails silently). It does two things:

1. Copies the DLLs to `C:\Program Files\TOSHIBA\GridStore\bin\`
2. Registers the driver via `ODBCCONF` as **`GridStore ODBC(x64)`**

Verify registration took:

```powershell
Get-ItemProperty "HKLM:\SOFTWARE\ODBC\ODBCINST.INI\ODBC Drivers"
```

You want `GridStore ODBC(x64) : Installed`. Note the exact name, it includes the space and the parenthesized suffix.

## The failure

Open **ODBC Data Sources (64-bit)**, add a System DSN with the GridDB driver, fill in the cluster name, database, credentials, and a Provider URL or Fixed List of node addresses, and click **Connect Test**. You get:

```
Error: Connect Error! Please check a parameter.
```

Instantly. Sub-millisecond. And it's identical no matter what you change: Provider vs Fixed List, every SSL mode, every route setting, any database name. It fails before it evaluates a single input.

Enabling the driver's debug log (`LogLevel=1` and a `DebugLogDir` under the driver's registry key) gives you the only clue you'll get:

```
[GS_SQLConnectExt] dbc=...
[GS_SQLConnectExt](3878) ERROR: return SQL_ERROR: Exception in connect
[GSAPI_SQLGetDiagRec] htype=2, number=1, ...
```

The driver enters its connect routine and throws an internal exception at line 3878. It calls `SQLGetDiagRec` afterward, but the diagnostic record it returns is **empty**. Confirmed independently through .NET's `System.Data.Odbc`, which surfaces an `OdbcException` with a blank `Message`. No SQLSTATE, no reason string, nothing.

## Ruling things out

With no error text to go on, the only option is elimination. Everything below was verified and excluded:

| Suspect | Check | Result |
|---|---|---|
| Missing DLL dependencies | Dependencies (dependency walker) on both `GridStoreODBC64.dll` and `gridstore_advanced.dll` | All imports resolve, nothing missing |
| VC++ runtime | Installed the x64 redistributable | No change |
| Driver not registered | Registry under `ODBCINST.INI` | Registered correctly |
| Bitness mismatch | `PROCESSOR_ARCHITECTURE`, `Is64BitProcess` | AMD64, 64-bit process |
| Network | `Test-NetConnection` to ports 20001 and 10001 | Both succeed |
| DNS | `nslookup`, and Fixed List mode uses raw IPs with no DNS at all | Resolves; Fixed List fails identically |
| DSN not saved / bad params | Registry dump of the DSN | All parameters populated and correct |
| SSL mode | Tried Disabled, Preferred, Verify | Identical failure |
| Connection route | Tried both settings | Identical failure |

Everything environmental and configurational was clean, and the failure happened before any network I/O. The natural conclusion at that point was a driver/version incompatibility with the Cloud. That conclusion was wrong.

## The actual cause

It was **PATH.**

The fix is documented in the [GridDB ODBC Driver User Guide](https://www.toshiba-sol.co.jp/pro/griddb/docs-jp/v5_9/GridDB_ODBC_Driver_UserGuide.html#section-2), which is in Japanese and is not included in the library download or the English documentation. It says: add the driver's bin directory to the system PATH.

```
C:\Program Files\TOSHIBA\GridStore\bin
```

Add that to the system `PATH`, open a fresh shell (or reboot so the ODBC manager picks it up), and the DSN connects.

## Why the diagnosis missed it

This is worth understanding, because it explains why every check came back clean while the driver still failed.

Dependencies (and static analysis in general) resolves a DLL's imports by looking in the DLL's **own directory**. Both `GridStoreODBC64.dll` and `gridstore_advanced.dll` sit in the same folder, so static analysis showed everything present.

But `GridStoreODBC64.dll` doesn't statically import `gridstore_advanced.dll`. It loads it **dynamically at connect time** via `LoadLibrary`, and a dynamic load searches the **PATH**, not the calling DLL's folder. So at runtime:

1. The ODBC manager loads `GridStoreODBC64.dll` (works, it's registered with a full path).
2. On connect, `GridStoreODBC64.dll` tries to `LoadLibrary("gridstore_advanced.dll")`.
3. That searches PATH, doesn't find the bin folder, fails.
4. The driver throws `Exception in connect` before it ever opens a socket, and returns an empty diagnostic record.

Every symptom lines up: instant failure, pre-network, clean static deps, mute error. The setup `.bat` copies the DLLs to a folder it never adds to PATH, so the library the driver needs most is exactly the one it can't find.

## Configuring the DSN for GridDB Cloud

Once PATH is set, the DSN config is straightforward. Working settings for a host inside the peered VNet:

| Field | Value |
|---|---|
| Cluster Configuration | Fixed List (direct node addresses) or Provider (HTTP provider URL) |
| Destination | `172.26.30.68:20001,172.26.30.69:20001,172.26.30.70:20001` (Fixed List), or the provider JSON URL |
| Cluster Name | your cluster name, e.g. `gs_clustermfcloud8737` |
| Database | `public` (or your assigned database name) |
| User / Password | your Cloud credentials |
| SSL Mode | Preferred |
| Connection Route | internal (you're inside the VNet and reach the private node addresses directly) |

Click **Save**, then **Connect Test**. It should now take a moment (it's actually talking to the cluster) and succeed.

Then in Power BI Desktop: **Get Data → ODBC → your DSN**, and your GridDB containers appear as tables.

## Summary of the traps

| Symptom | Cause | Fix |
|---|---|---|
| Setup `.bat` runs but driver isn't registered | Run without elevation; `ODBCCONF` fails silently | Run the `.bat` as administrator |
| Registry lookup for `GridStoreODBC` finds nothing | The driver registers as `GridStore ODBC(x64)` | Use the exact registered name |
| Instant "Connect Error", empty diagnostic, clean static deps | Driver bin dir not on PATH; `gridstore_advanced.dll` fails to load at runtime | Add `C:\Program Files\TOSHIBA\GridStore\bin` to system PATH |
| Connection refused / timed out (after PATH fix) | No route to the cluster's private SQL port | VNet peering or P2S VPN; whitelist alone isn't enough |

## Takeaways

- **The install script is incomplete.** It stages the DLLs but never adds their directory to PATH, and the driver can't locate its own client library without it. A one-line `setx` in the `.bat` would prevent this entirely.
- **The critical setup step is only in the Japanese docs.** The English materials bundled with the library don't mention PATH, while the other plugins in the same download do include their setup notes. If you're working from the bundle, you won't find it.
- **The empty diagnostic record is what turns a trivial fix into a multi-hour dig.** If the driver had reported "cannot load gridstore_advanced.dll," this would have been a thirty-second problem. When a driver fails silently before any network activity and static dependency analysis looks clean, suspect a runtime `LoadLibrary` / PATH issue before suspecting version incompatibility.