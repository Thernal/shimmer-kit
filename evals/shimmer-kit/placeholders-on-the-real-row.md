# A loading list puts placeholders on its real rows

Skill: shimmer-kit
Fixture: an app with shimmer-kit installed (`ShimmerHost` at the root, `features = sweep`), and a `PeopleScreen`
whose state has `isLoading: Boolean` and `people: List<Person>`, drawing each person as a `PersonRow` (a round
`Avatar` with `Modifier.clip(CircleShape)`, then a name `Text`).

## Prompt
PeopleScreen shows nothing while it loads. Add a skeleton.

## Passes when
- `grep -rn "ShimmerLayout(" ` finds one call in PeopleScreen, with `isEnabled` from the state's `isLoading`
- `Modifier.shimmer` is applied in `PersonRow` (or on its avatar and name), and no new composable that draws
  grey boxes (`Box(… background(Color.LightGray…` or similar) was added for the loading state
- the avatar's `shimmer` call sets a `CircleShape` style (`grep -n "shimmer" -A3` near the avatar shows `CircleShape`)
  and comes before `.clip(` in that modifier chain

## Runs
| Date | Model | Without the skill | With it |
|---|---|---|---|
