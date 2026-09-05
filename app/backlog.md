# Backlog

## Better validations

- functions for generic validations
- messages when they fail
- dsl for easy chaining

## Multiple tenants

- It should be allowed for multiple tenants to share an appartment

## Missed updates

- Better UI for when you missed some update on a payment

## Deleting unused stuff

- UI should provide an easy way to delete stuff created by accident or that's no longer of any use

## ViewModel

- I still don't fully get the view model thing, and the current implementation is doing a lot of stuff and should probably be reworked

## Changable language

Language should be able to be changed, I've never done it before, but it probably involves every string beign parameriticized and whatnot. God that sounds like a drag.

## Figure out a way to make the contract card show if a payment, adjustment or deadline is near

## Something

  1. High — An adjustment interval of zero freezes the app.
     /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/model/Contract.kt:34 builds a sequence that never advances when
     adjustmentTime == 0, then collects it with toList(). The form accepts zero because it only validates conversion to Long. This runs inside the submit
     callback on the UI thread. Negative values and extreme positive values are also unsafe. Validate a bounded positive interval in both the form and the
     generator.

  2. High — Selecting the finishing date first crashes validation.
     /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/create/CreateContractScreen.kt:186 calls
     date.isAfter(startDate) while startDate can still be null. The safe call protects date, not its argument. Check that both dates exist before comparing
     them.

  3. High — Recomposition repeatedly creates sharing coroutines and database subscriptions.
     /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/create/CreateContractViewModel.kt:24 creates a new
     stateIn(viewModelScope, …) every time owners(), properties(), or services() is called. The screen calls all three during composition. Repeated
     recompositions therefore create new flows and restart collection; previous sharing jobs remain tied to the ViewModel’s lifetime. Expose stable val flows,
     as ContractViewModel already does. See Kotlin’s stateIn documentation
     (https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/state-in.html).

  4. High — Database upgrades can erase all stored records.
     /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/MyApplication.kt:13 enables destructive migration, with no migration
     paths registered. Schema export is also disabled. An existing installation requiring migration will lose its data. Introduce migrations, export schemas,
     and test upgrades before distributing updates. This behavior is documented in Room’s migration guide
     (https://developer.android.com/training/data-storage/room/migrating-db-versions).

  5. Medium — Activity recreation loses navigation and unfinished forms.
     /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/navigation/AppNavigation.kt:25 stores the back stack with remember;
     creation forms do the same for their inputs. Rotating the device recreates the activity, returns navigation to the list, and discards drafts. Persist
     navigation using restorable route identifiers and save draft state through appropriate saved-state APIs. See Compose state saving
     (https://developer.android.com/develop/ui/compose/state-saving).

  6. Medium — Screens close before saves succeed, and write failures are unhandled.
     /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/create/CreateContractScreen.kt:107 starts saving and
     immediately navigates back. Payment creation follows the same pattern. The ViewModel coroutines provide no error handling or success state. A database
     exception can terminate the app after the draft has disappeared. Expose saving/success/error state, prevent duplicate submissions, and navigate only
     after successful persistence.

  7. Medium — Creating a complete contract is not atomic.
     /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/create/CreateContractViewModel.kt:72 separately inserts the
     owner, property, services, and contract. The DAO transaction covers only the final contract and its relations. Failure partway through leaves partially
     created records; retrying can duplicate them. Move the entire operation behind one database transaction.

  8. Medium — Searching after selecting an entity uses invisible input.
     /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/components/picker/EntityPicker.kt:156 displays the selected item’s
     label while typing updates only query. Filtering changes, but the text field continues showing the old selection. Clear the selection when editing
     begins, or explicitly separate selected and search states.

  9. Medium — Adjustments with no amount cannot be edited.
     /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/detail/AdjustmentCard.kt:59 nests the edit button inside
     adjustment.amount != null. Leaving the optional fixed hike blank creates null amounts, so those rows never expose their editor even when editable is
     true. Render the edit action independently of whether an amount exists.
