# Privacy and Security Plan

| | |
|---|---|
| **Version** | 0.1 (Draft) |
| **Status** | Design. Nothing here is audited or tested yet; every guarantee is a design target until the release audit (section 16) passes. |
| **Resolves** | OQ-05 (backup encryption scheme), OQ-09 (automatic cloud backup), AQ-02 (app lock mechanism), AQ-03 |
| **Last updated** | 2026-10-25 |

**Legend.** **[G]** = guarantee the design aims to provide and that the release audit must verify. **[A]** = assumption about the platform or user behavior that this app cannot control. Anything not marked is explanation.

Platform facts below (API behavior, algorithm availability, library manifests) come from my knowledge of Android and **must be re-checked against current official documentation** when the relevant task is implemented. They are marked *verify*.

---

## 1. Decisions

| ID | Decision |
|---|---|
| SEC-D01 | The app declares **no INTERNET permission** and no component makes network requests. **[G]** |
| SEC-D02 | **No permissions at all** in v1, with one possible exception: the biometric permission that the app-lock library may declare (section 4). |
| SEC-D03 | Data is stored in Room/SQLite in app-private storage, **unencrypted**. No database encryption in v1. |
| SEC-D04 | Android's automatic cloud backup and device-to-device transfer of app data are **disabled** (`allowBackup` false plus extraction rules). The app's own backup is the only recovery path. |
| SEC-D05 | The **app lock** uses the system's authentication (biometric or device screen lock) through the platform prompt. No custom PIN in v1. It is a lock, not encryption. |
| SEC-D06 | **Backup encryption** is optional: a password-derived key (PBKDF2-HMAC-SHA256) with AES-256-GCM, using platform cryptography only. No new dependency, no custom primitives. |
| SEC-D07 | The v1 app holds **no stored secrets** and creates no Android Keystore keys. |
| SEC-D08 | The AI-readable export is written only to a location the user picks (system "save as"). No share-provider component in the manifest unless approved later. |
| SEC-D09 | All text shown to the user about security states exactly what the app does and does not do (section 15). |

---

## 2. Data inventory

| Data | Where it lives | Sensitivity | Leaves the device? |
|---|---|---|---|
| Tasks, descriptions, tags | Room (app-private) | Medium | Only inside a backup or AI export the user creates |
| Time blocks, categories, actual durations | Room | Medium | Same |
| Habits and entries, goals, milestones, progress | Room | Medium | Same |
| Notes and journal | Room | **High** (free text, may be very personal) | Same |
| Resources (URLs, titles, notes) | Room | Medium to high: **URLs can embed tokens or private identifiers** | Same; opening a link hands the URL to the browser or another app |
| Finance records (P4) | Room | **High** | Same |
| Settings (theme, defaults, week start) | DataStore | Low | Portable ones are included in backups |
| Backup bookkeeping (last backup time, dirty flag, folder URI, snooze, lock flag) | DataStore | Low; the folder URI reveals a folder path | No; not included in backups |
| Backup archives | User-chosen folder | Same as the data inside; unencrypted unless the user enables encryption | Wherever that folder syncs |
| AI-readable export file | User-chosen location | Same as the selected data | Wherever the user shares it |
| Logs | Debug only | Must contain no user content | No |

The app collects nothing about the user's identity or behavior and sends nothing anywhere. **[G]**

---

## 3. Local storage

- Database and DataStore files are in the app's private directory, protected by Android's per-app sandbox. **[A]** The sandbox protects against other ordinary apps, not against a rooted or compromised device or physical forensic extraction.
- **Device-level encryption** (Android's file-based encryption) usually protects data at rest when the phone is locked, but this app does not control it and does not verify it. **[A]**
- Temporary files (for example, a backup being written) are created in the destination folder with a recognizable temporary name, and deleted on completion or failure. Temporary files in app cache contain no more than the backup in progress and are removed afterward.
- No user content is written to external storage except through user-initiated backup or export.

---

## 4. Permission inventory and manifest rules

| Permission | Needed? | Decision |
|---|---|---|
| `INTERNET` | No | **Never declared** without an approved ADR |
| `ACCESS_NETWORK_STATE` | No | Not declared |
| `POST_NOTIFICATIONS` | No (no system notifications) | Not declared |
| `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM` | No | Not declared |
| `RECEIVE_BOOT_COMPLETED` | No | Not declared |
| Storage permissions | No: the folder picker (Storage Access Framework) and "save as" need none | Not declared |
| `USE_BIOMETRIC` | Possibly, if the biometric library is adopted (*verify*: the library's own manifest typically declares it, and it is a normal permission granted at install) | Accepted if needed; list it in the release audit; no prompt to the user |
| `USE_FINGERPRINT` | Only if the library adds it for older API levels (*verify*) | Accept only if merged in by the library; check the merged manifest |

Manifest requirements:
- Only `MainActivity` is exported (launcher). No services, receivers, or content providers in v1. **[G]**
- `android:allowBackup="false"` and extraction rules that exclude all app data (`dataExtractionRules` for newer Android versions, `fullBackupContent` for older). *Verify* the exact attribute behavior in the current official documentation when implementing T-002.
- `android:usesCleartextTraffic` is irrelevant with no network use; do not add a network security config.
- No `<queries>` entries are needed: open links with `ACTION_VIEW` inside a try/catch for "no app can open this link", instead of querying for resolvers.
- Debug and release merged manifests are compared; any permission present in only one is explained.

**Consequence of SEC-D04 to tell users plainly:** switching phones means making an app backup and restoring it on the new phone. The OS will not move the data for them. The privacy and backup screens say so.

---

## 5. Network access decision

- v1 has no network capability. Every feature except opening an external link works offline. **[G]**
- **Opening a link** hands the URL to the system through `ACTION_VIEW`; the browser or another app makes the connection, not this app. This needs no permission. The in-app wording states that opening a link needs the user's internet connection and that the destination site can see the request.
- **Copying a URL** places it on the system clipboard, which other apps may be able to read depending on the Android version. The copy action is user-initiated; warn in the resource screen only if the URL looks like it contains a long token (optional).
- No image loading, no favicon fetching, no metadata scraping, no update checks, no crash upload.
- A future feature that needs the network (for example optional sync) requires an ADR, an explicit permission decision, and a revision of this document first.

---

## 6. Dependency and networking audit

Run at every dependency change and before every release (T-034).

1. List the resolved dependency tree (`./gradlew :app:dependencies` for the release runtime classpath) and review every library, including transitive ones, for network use.
2. Build the release variant and inspect the **merged manifest** and the **built APK's permissions** (for example with the SDK's `apkanalyzer` or `aapt2` tools). *Verify* tool names and paths for the installed SDK.
3. The release manifest must show **no INTERNET** and no other permission beyond the section 4 table. Add this as an automated check in T-034.
4. Search the dependency tree and source for networking classes (`HttpURLConnection`, `okhttp`, `retrofit`, `Socket`, WebView) and for analytics, ads, crash-reporting, and AI SDKs. Any hit needs a written explanation.
5. Confirm there are no exported components other than `MainActivity` in the merged manifest.
6. Confirm no native libraries or assets were added unexpectedly (check APK contents).
7. Record the dependency list and versions in the tracker with the audit date.

New dependencies are approved only after an ADR covers purpose, maintenance status, size effect, and network behavior.

---

## 7. Android automatic backup decision (OQ-09)

**Decision:** disable it (SEC-D04).

| | Enabled | Disabled (chosen) |
|---|---|---|
| Data leaves the device without a user action | Yes (to the user's Google account, under the platform's rules) | No |
| Automatic recovery on a new phone | Possible, depends on platform and settings | Not available; user restores from the app's backup |
| Contradicts "no data leaves the device unless I choose" | Yes | No |
| Reliability | Not under this app's control or visibility | Fully under the app's control |

The cost is real: users must keep their own backups. That is the reason the backup nudge exists and the reason restore must be tested thoroughly.

---

## 8. App lock design (AQ-02)

**Mechanism:** the system biometric prompt through the AndroidX biometric library (one small dependency, requires an ADR), configured to accept biometrics **or the device's screen lock**.

*Verify at T-033:* on older Android versions (reported as API 29 and below) the combination of strong biometrics with device credential may not be supported in one prompt. If so, the fallback is the platform's confirm-device-credential screen. The task must test on the author's device and an emulator image of an older version, and the chosen behavior goes into this document.

**Behavior (fixed in v1, not configurable):**
- Locked on every cold start when the lock is enabled.
- Locks again when the app has been in the background longer than **30 seconds**. A short grace period avoids prompting when the user briefly switches apps. The value is a named constant so it can change after real use.
- The unlocked state lives in memory only and is never persisted.
- The lock screen shows no user content.
- While the lock is enabled, the window is marked secure so that content does not appear in the recent-apps preview. A side effect is that screenshots are blocked while the lock is on; the lock setting's help text says so. This may be revisited after real use.
- The system handles failed attempts and lockout; the app keeps no attempt counter or secret.
- If the device has **no secure screen lock or enrolled biometrics**, the lock cannot be enabled, and the settings screen explains why. If the device's lock is removed after the app lock was enabled, the app opens with a visible notice instead of locking the user out, because the lock would offer no protection in that state.

**Honest wording:** "App lock hides your data behind your phone's screen lock. It does not encrypt your data." This text appears in settings and on the privacy screen. **[G]**

**Why not a custom PIN:** a PIN requires the app to store a verifier, handle brute-force limits, and manage reset flows, all of which are extra security-sensitive code. The system prompt already does this work.

---

## 9. Database encryption: options and recommendation

| | A. Plain Room (chosen for v1) | B. SQLCipher-style encrypted SQLite |
|---|---|---|
| Protects against | Other apps (sandbox) | Also casual file extraction, some forensic access |
| Key management | None | A random database key must be wrapped by an Android Keystore key and stored; if the Keystore key is lost (device reset, certain lock-screen changes, restoring to a new device) the data is unrecoverable without a backup |
| Recovery | Simple | Depends entirely on backups |
| Migration | None | A one-time copy from plain to encrypted for existing data; tested migrations |
| Size and performance | Smallest; no native libraries | Adds native libraries per CPU architecture and a third-party dependency, with some performance cost; **measure before claiming numbers** |
| Dependency and maintenance risk | None | A security-critical third-party library that must be kept updated |
| What it does not fix | | A lock does not equal encryption; an unlocked, compromised device still exposes data |

**Recommendation:** do **not** include database encryption in v1. The app is single-user, personal, and offline. The realistic leak paths are lost or shared devices (covered by the device's own encryption and screen lock, and by the optional app lock) and exported files (covered by backup encryption). The schema and repository boundaries do not preclude adding encryption later.

**Revisit when:** a Play Store release is planned, friends with sensitive use appear, or finance (P4) begins. If adopted: Room with an encrypted open-helper factory, a random key wrapped by a Keystore-backed key, a tested migration from the plain database, a documented loss-of-key behavior (restore from backup), and a measured effect on size and performance.

---

## 10. Backup protection

### 10.1 Unencrypted backups
Allowed, but every unencrypted backup action shows a clear warning (FR-BKP-005): "This backup is not encrypted. Anyone who can open the file can read all your data." The default backup flow offers encryption but does not force it.

### 10.2 Encrypted backups (OQ-05)

| Item | Decision |
|---|---|
| Key derivation | PBKDF2-HMAC-SHA256 from a user password (*verify* availability on API 26) with a random 16-byte salt per backup. Iteration count is calibrated by measurement on the author's device and **never below the current published recommendation** for this algorithm; the recommendation and the chosen value are recorded in the tracker when T-022 is implemented |
| Cipher | AES-256-GCM, random 12-byte nonce per backup, 128-bit tag |
| Scope | The entire ZIP archive is encrypted as one message; the header is authenticated as associated data |
| Library | Platform `javax.crypto` only. No custom primitives, no home-made modes |
| Password | Minimum 8 characters; encourage a passphrase; no composition rules. The password is **never stored**; held in memory only for the operation and cleared as far as the platform allows (best effort; the JVM gives no hard guarantee) |
| Recovery | **None.** A forgotten password means the backup cannot be opened. The UI says so before the first encrypted backup and requires the password to be typed twice |
| Convenience cost | The password is typed for each encrypted backup and restore. Caching a key would need Keystore-bound storage, which would not survive a phone change |
| Size ceiling | Single-message AES-GCM must hold the archive in memory. Initial ceiling is **32 MiB**, to be validated on a low-memory device in T-022. If exceeded, the app says the backup is too large to encrypt in this version and offers an unencrypted backup. The upgrade path, if needed, is a streaming authenticated-encryption library, decided by ADR |
| Error messages | A wrong password and a damaged file are indistinguishable with authenticated encryption; the message says "wrong password or damaged file" |

**Encrypted file layout (version 1):**
```
magic          4 bytes   ASCII marker for "encrypted backup"
format version 1 byte    1
kdf id         1 byte    1 = PBKDF2-HMAC-SHA256
iterations     4 bytes   big-endian
salt length    1 byte    16
salt           16 bytes
nonce length   1 byte    12
nonce          12 bytes
ciphertext     remaining bytes (includes the 16-byte GCM tag)
AAD            all header bytes above
```
Reader rules: reject unknown magic or format version; accept iteration counts only within a sane range (a lower bound that rejects trivial values, and an upper bound that prevents a crafted file from freezing the app); never trust length fields beyond the file size.

File names: unencrypted `{app}-backup-YYYYMMDD-HHmm.zip`; encrypted `{app}-backup-YYYYMMDD-HHmm.zip.enc`.

### 10.3 Backup folder risks
- The user chooses the folder. If it is synced to a cloud service, the data (encrypted or not) is on that service under its rules. The folder picker help text says so.
- The app keeps only a persisted permission for that one folder and does not scan other files.
- The retention policy (keep the last **5** backups, delete older ones) deletes only files matching the app's own naming pattern.

### 10.4 Restoring untrusted files
A backup file is untrusted input, even one the user made. The restore procedure in `DATABASE_SCHEMA.md` section 11 validates structure and content before any change. Checksums detect accidents, not malice; only an encrypted archive's authentication tag detects tampering.

---

## 11. Android Keystore

v1 stores no secrets, so it creates no Keystore keys (SEC-D07). Backup encryption uses a password-derived key that is never persisted. The app lock uses the system prompt and keeps no key.

When Keystore becomes relevant (database encryption, section 9), the requirements are: a hardware-backed key where available, key invalidation behavior documented, and a clear user message that losing the key means restoring from a backup.

---

## 12. Data deletion

**"Delete all data"** (T-021) requires a typed confirmation, then: closes the database, deletes the database files (main, write-ahead log, and shared-memory files), clears DataStore, and recreates an empty database. *Verify* with a test that no database file remains.

Limits, stated in the UI and here:
- Flash storage and the file system may keep remnants of deleted data; this is not a secure wipe. **[A]**
- Backups already written to the user's folder are **not** deleted by this action. The confirmation says so and offers to open the backup folder.
- Uninstalling the app removes its private data, but not backup files in a user folder.
- Individual record deletion removes the row; the database file may retain freed pages until reused. An optional compaction step may be added later; it is not a security guarantee.

---

## 13. Local document handling

None. Resources are URLs only (D-08). The app never stores file paths or content URIs of user documents, never copies documents, and never requests broad file access. The single persisted URI permission is for the backup folder and is not a document reference.

---

## 14. Privacy risks and mitigations

| # | Threat | Mitigation in v1 | Remaining exposure |
|---|---|---|---|
| T1 | Lost or stolen **locked** phone | Device encryption and screen lock **[A]**; optional app lock | Depends on the device's own protection |
| T2 | Someone using the **unlocked** phone briefly | Optional app lock; secure window flag | Screenshots blocked only while the lock is on; no protection if the lock is off |
| T3 | Another app on the same phone reading data | Android app sandbox; no exported components or providers **[G]** | None against a rooted or compromised device |
| T4 | Backup file read by someone else | Optional encryption; unencrypted-backup warning; user-chosen folder | Unencrypted backups and cloud-synced folders |
| T5 | AI export shared with an external AI service | Explicit module and date selection; warning before export; user-initiated only | Content is outside the app's control once shared |
| T6 | Malicious or corrupted backup file imported | Full validation before any change; atomic restore; size and range checks | An unencrypted archive can be crafted validly; restore replaces data with what it contains |
| T7 | Supply-chain compromise via dependencies | Minimal dependency list; pinned versions; ADR per dependency; release audit | Residual risk of any third-party code |
| T8 | URL secrets stored in resources | Warning text in the resource form; the URL stays local | Anyone with access to data sees the URL |
| T9 | Keyboard or clipboard leakage (third-party keyboards, clipboard readers) | Out of the app's control; documented | Residual |
| T10 | Data loss (device loss, reset, uninstall) | Backups, nudge, tested restore | Depends on the user making backups |
| T11 | Debug artifacts leaking data (logs, debuggable builds) | No content in logs; release builds not debuggable | Verify in the audit |
| T12 | Forgotten backup password | Warned; typed twice; no recovery by design | Backup unrecoverable |

---

## 15. In-app privacy screen text (draft for T-032)

> **What this app does**
> - Stores your data on this phone only. There is no account and no server.
> - Never sends your data anywhere. The app has no internet permission.
> - Contains no ads, no tracking, and no AI.
>
> **What this app does not do**
> - It does not encrypt your data on the phone. Your phone's own encryption and screen lock protect it when the phone is locked.
> - App lock (if you turn it on) hides the app behind your phone's screen lock. It is a lock, not encryption.
> - Android's automatic cloud backup is turned off for this app. To move to a new phone or protect against loss, make a backup and restore it.
>
> **Backups and exports**
> - Backups are saved in the folder you choose. If that folder syncs to a cloud service, your backup goes there.
> - Unencrypted backups can be read by anyone who gets the file. Encrypted backups cannot be opened without your password, and we cannot recover a forgotten password.
> - A readable export for AI tools is created only when you ask, and it leaves the app's protection once you share it.
>
> **Links**
> - Opening a saved link uses your browser or another app and needs an internet connection. The website can see that request.

---

## 16. Release audit checklist

Run for every build shared with anyone. Record results in `TEST_PLAN.md` release records.

- [ ] Release manifest has **no INTERNET** and no permission outside section 4; any difference from debug is explained.
- [ ] Only `MainActivity` is exported; no services, receivers, or providers.
- [ ] `allowBackup` is false and extraction rules exclude all data.
- [ ] Release build is not debuggable; no debug-only code paths ship.
- [ ] Dependency tree reviewed; no networking, analytics, ads, crash-reporting, or AI libraries.
- [ ] No logging of user content in release (search the code; run the app and inspect logcat).
- [ ] No cleartext secrets, keys, or passwords in code or resources.
- [ ] App lock behaves per section 8 on the author's device and an older-Android emulator.
- [ ] Backup: unencrypted warning shown; encrypted backup round-trips; wrong password changes nothing; tampered file rejected.
- [ ] Restore of corrupt, truncated, wrong-version, and invalid-row archives leaves data unchanged.
- [ ] Delete-all leaves no database files.
- [ ] Airplane-mode checklist passes (`TEST_PLAN.md`).
- [ ] In-app privacy text matches this document.
- [ ] Signing key backed up in two places and not in the repository.

---

## 17. Play Store notes (when relevant)

- A privacy policy will probably be required to publish; state what section 2 and section 15 say. *Verify* current store requirements (OQ-08).
- The store's data-safety declaration should reflect: no data collected, no data shared, no network access. Be careful with the "export" feature description: data leaves only by user action.
- If a library you add later declares permissions or collects data, this document and the store declaration must change first.

---

## 18. Explicit limitations of the privacy model

1. The database is **not encrypted** in v1.
2. A rooted or otherwise compromised device defeats the sandbox.
3. App lock hides the UI; it does not protect files.
4. Unencrypted backups, and backups in cloud-synced folders, are only as safe as that location.
5. Anything shared (AI export, copied URLs) is outside the app's control.
6. There is no secure deletion guarantee.
7. Encrypted backups have no password recovery.
8. Third-party keyboards and other accessibility or screen-reading apps can see what you type or display.
9. No tamper protection for unencrypted backups.
10. Device encryption and OS update status are assumptions, not guarantees.

---

## 19. Open items and verification tasks

| ID | Item | Resolve in |
|---|---|---|
| SV-01 | Confirm `allowBackup` and extraction-rule attribute behavior in current docs | T-002 |
| SV-02 | Confirm biometric library manifest permissions and behavior on older Android | T-033 |
| SV-03 | Calibrate PBKDF2 iterations on the author's device and record the published recommendation | T-022 |
| SV-04 | Validate the 32 MiB encrypted-backup ceiling on a low-memory device | T-022 |
| SV-05 | Confirm delete-all removes all database files | T-021 |
| SV-06 | Confirm secure window flag behavior and its effect on screenshots | T-033 |
| SV-07 | Review Play Store policy requirements | Before any store plan (OQ-08) |
