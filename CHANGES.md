# PantryPal 1.1 (final) - changes since the zip you sent

NOT compiled or run (no Android SDK where these edits were made). Build once and send the first error if any.

## Bug fixes
- Recipe matching is now word-based ("eggplant" no longer matches "egg", "licorice" no longer matches "rice"; plurals handled).
- "I cooked this!" no longer subtracts 1 from everything: only count items (pcs) lose a piece; g/kg/ml/L items are left alone and you are told. Switched-off ingredients are never used. List and detail screens now agree.
- Sample pantry only loads when you choose it (the database used to insert it on first open, even for "Start empty"). Reminders do nothing before onboarding finishes.
- Notification permission is asked once after onboarding, not on the first screen.
- Lists no longer jump or replay animations; quantity must be a positive number; Done key adds a grocery item; "Works 100% offline" claim corrected (ads need internet).
- Build: Termux aapt2 override on, Kotlin daemon memory capped, setup-wrapper.sh + build.sh added (README step 2 would fail with Gradle 9).

## New features
- 20 built-in recipes (was 6). Existing installs get the new ones through a database migration (v1 -> v2).
- Smart ranking: recipes that use up your expiring items come first; cards say "Uses up: spinach".
- Dashboard "Cook this soon" suggestion built from what is about to expire.
- "Your impact" replaces the made-up dollar Saved Wallet: items rescued (used within 3 days of expiry) and meals cooked.
- Reminders on/off switch in Settings (cancels/reschedules the daily job); expired items nag for 2 days, not forever.
- Item editor: Delete button (with Undo), quick-pick chips for unit and category, typical shelf life pre-fills the expiry date, validation.
- Pantry category filter chips.
- Grocery list: Share button, and moving checked items into the pantry merges duplicates (+1) instead of creating copies.
- Accessibility: darker accent text for small labels in light mode, pulse animation respects "animations off", lifecycle-aware state collection.
- Tests: RecipeMatcher (word matching, ranking, recipe data), ShelfLife.

## Still not done
Barcode scanning, user-created recipes, quantity-aware cooking, import/export or cloud backup, Room schema export, release signing, a real privacy-policy URL for the Play Store.
