# Recipe collection and personal recipes

Open **Settings > Recipe collection** to see all saved recipes, whether or not the pantry can make them. The initial collection contains 20 recipes and 22 versions. Opening a version shows its ingredients, quantities, servings and preparation steps.

Choose **Add recipe** to enter a name, servings, ingredient rows and preparation steps. Write one step per line. Ingredient names offer suggestions from the saved collection; new names are also accepted. New names must be entered the same way in the pantry, ignoring case and extra spaces. Known aliases such as wors and boerewors share the same ingredient. Use one row per ingredient.

Personal recipes have Edit and Delete buttons on their detail screen. Deletion requires confirmation. Included recipes can also be edited. Editing replaces ingredients, amount made and preparation for the selected version only; changing the recipe name updates the shared name for all versions. Included recipes cannot be deleted. Saving, editing and deleting recipes does not change pantry quantities.

All recipes use the existing strict matching rules. Every ingredient except water and salt must be available in sufficient quantity. Water and salt remain listed for cooking but do not affect suggestions. Grams and kilograms are compatible, as are millilitres and litres. Count, weight and volume cannot substitute for one another. Stored recipe quantities use g, ml or count, so an entered 0.5 kg appears as 500 g when reopened.

## Storage

Database version 3 adds a user-created flag to recipes. Upgrades from versions 1 and 2 preserve pantry records and existing recipe details. Each personal recipe has one version. Saves and deletions use transactions. Ingredient names remain available for reuse after a recipe is deleted, because another recipe or pantry entry may use them.

## Checks in Android Studio

Run `PersonalRecipeTest` and `RecipeDatabaseTest` on the emulator. They cover the full collection, fresh databases, upgrades, matching, persistence, editing, deletion, editing included versions without changing sibling versions and rollback after invalid input.

Manual review:

1. Open Recipe collection with an empty or incomplete pantry. Check the count and open both Pap and wors versions.
2. Add a test recipe with two ingredients and two preparation steps. Check blank names, zero quantities, missing steps and duplicate ingredient names are rejected.
3. Rotate the device while filling in multiple rows. Names, quantities, units and preparation should remain in the draft.
4. Save and reopen the app. Check the personal recipe remains available in the collection.
5. Add enough matching ingredients to the pantry. Check the recipe appears in Suggested Recipes, then disappears if a required quantity becomes insufficient.
6. Edit the personal recipe and reopen its detail. Check the new ingredients and steps replaced the old ones.
7. Cancel a deletion, then confirm a deletion. Check the recipe disappears only after confirmation and pantry items remain unchanged.
