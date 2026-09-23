# Backlog

## Multiple tenants

- It should be allowed for multiple tenants to share an appartment

## Inputting numbers

- KeyboardType.DecimalSigned doesn't work on Samsung, what in the actuall fuck.

## Deleting unused stuff

- UI should provide an easy way to delete stuff created by accident or that's no longer of any use
- Put properties in unavailable if they cannot be used for contracts rn

## Properties for contract

- Only properties without a current active contract should be shown in the create contract list
- Unavailable properties also don't show

## View

- The x icon on Bill item is slightly unaligned
- Scrolling Bills would be nice

## End of Contract

- Start registering lease extension monthly after the last bill

## Language (again)

- There is a bug on language that some stuff like the create new service label isn't properly translated
- Default message server should depend on locale

## List

1. Medium — Adjustments with no amount cannot be edited.
/C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/detail/AdjustmentCard.kt:59 nests the edit button inside adjustment.amount != null. Leaving the optional fixed hike blank creates null amounts, so those rows never expose their editor even when editable is true.
Render the edit action independently of whether an amount exists.

2. Adjustment saves lack error handling. /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/detail/
ContractDetailViewModel.kt:53 launches the write without catching failures, while the editor closes immediately. A rejected update can become an uncaught exception and lose the draft.

3. Finish/delete retain UI callbacks during async work. /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/detail/
ContractDetailViewModel.kt:70 can invoke callbacks from the old screen after rotation. Exposing completion state, as contract creation does, would let the current screen handle navigation.

4. Observed database flows lack error handling. The list, detail and creation ViewModels use stateIn without handling upstream failures. A read failure can terminate observation without showing a recoverable error.
