# Backlog

## Multiple tenants

- It should be allowed for multiple tenants to share an appartment

## Inputting numbers

- KeyboardType.DecimalSigned doesn't work on Samsung, what in the actuall fuck.
- Something should be done so the number parser takes both . and , as decimal separators

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

- Renew contract after registering the last bill
- Start registering lease extension monthly after the last bill

## Changeable language

Language should be able to be changed, I've never done it before, but it probably involves every string beign parameriticized and whatnot. God that sounds like a drag.

## List

1. Medium — Screens close before saves succeed, and write failures are unhandled.
   /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/create/CreateContractScreen.kt:107 starts saving and
   immediately navigates back. Payment creation follows  the same pattern. The ViewModel coroutines provide no error handling or success state. A database
   exception can terminate the app after the draft has disappeared. Expose saving/success/error state, prevent duplicate submissions, and navigate only after successful persistence.

2. Medium — Creating a complete contract is not atomic.
   /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/create/CreateContractViewModel.kt:72 separately inserts the  owner, property, services, and contract. The DAO transaction covers only the final contract and its relations.
   Failure partway through leaves partially created records; retrying can duplicate them. Move the entire operation behind one database transaction.

3. Medium — Adjustments with no amount cannot be edited.
   /C:/Users/user/Desktop/MyApplication/app/src/main/java/com/example/myapplication/ui/contract/detail/AdjustmentCard.kt:59 nests the edit button inside adjustment.amount != null. Leaving the optional fixed hike blank creates null amounts, so those rows never expose their editor even when editable is true.
   Render the edit action independently of whether an amount exists.
