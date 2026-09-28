# Recipe collection

This collection contains 20 recipe titles and 22 complete versions. The quantities and methods are simplified home-style versions for review; they have not been kitchen-tested. Recipe data is stored in `app/src/main/assets/recipes.json`. This checkpoint does not yet load recipes into SQLite or display suggestions.

## Matching rules for the next stage

- A recipe is eligible only when every ingredient in at least one complete version is available in sufficient quantity. Never combine parts of different versions to produce a match.
- Display a recipe title once, with a choice of eligible versions if both qualify. Detail instructions must correspond to the selected version.
- Raw maize meal and cooked pap are different ingredients. Potatoes and prepared mash are different ingredients. Do not infer a cooked yield from raw stock.
- Quantities are for the full yield listed, not one serving. For example, the muffin requirements make 12 muffins.
- Sum pantry duplicates after resolving aliases and compatible units. Convert only g/kg and ml/l; do not convert count to mass, or mass to volume.
- All listed ingredients are required, including water, salt and oil. No garnish, sauce or side dish is assumed. Adding these requires updating the ingredient list too.
- Deep-frying oil is the amount needed in the pan, not an estimate of how much is eaten.
- `count` means individual eggs, Viennas or buns, not packets. Bread, onions and potatoes are weighed in g. Stock and white sauce mean prepared liquids, not powder or cubes.
- Plain dry pasta can use the listed aliases. Instant noodles, cooked pasta, self-raising flour and stock cubes are not substitutes for the corresponding dry pasta, cake flour or prepared stock entries.
- The pantry words `pap` and `mash` mean prepared food. Use `maize meal` for the raw ingredient.
- Equipment (oven, grill, saucepan, microwave, paper cases and baking paper) is described in the steps, not treated as pantry food.
- Matching checks quantity and identity only. It does not assess freshness, allergies or equipment availability.

## Collection notes

The first nine titles cover the requested dishes, with Vienna hot dogs and boerewors rolls stored separately. The white-sauce pasta keeps the requested pasta, cheese and prepared white sauce. The remaining eleven titles are South African home-style choices; there are many household variations.

Background references for dish selection (not claims that these versions reproduce or were tested by these sources):

- [What's for Dinner recipe collection](https://www.whatsfordinner.co.za/recipes-catalog.html)
- [SASKO amagwinya / vetkoek](https://www.sasko.co.za/recipes/savoury/sasko-amagwinya-vetkoek/)
- [Winemag discussion of malva pudding](https://winemag.co.za/food/recipe/malva-pudding/)

## Recipes

### 1. Pap and wors

**Make pap from maize meal — 2 servings**

Ingredients:
- Maize meal: 200 g
- Water: 800 ml
- Salt: 2 g
- Boerewors: 300 g

Steps:
1. Bring the water and salt to a boil in a heavy saucepan. Gradually stir in the maize meal.
2. Reduce to low heat, cover and cook for about 30 minutes, stirring regularly, until the pap is cooked and thick.
3. Grill the boerewors over moderate heat, turning regularly, until cooked through. Serve with the pap.

**Use cooked pap — 2 servings**

Ingredients:
- Cooked pap: 500 g
- Water: 50 ml
- Boerewors: 300 g

Steps:
1. Break the cooked pap into a saucepan and add the water. Cover and warm gently, stirring, until steaming throughout.
2. Grill the boerewors over moderate heat, turning regularly, until cooked through. Serve with the reheated pap.

### 2. Mash and wors

**Make mash from potatoes — 2 servings**

Ingredients:
- Potatoes: 500 g
- Water: 1500 ml
- Milk: 100 ml
- Butter: 20 g
- Salt: 2 g
- Boerewors: 300 g

Steps:
1. Peel and dice the potatoes. Put in a saucepan with the water and bring to a boil. Simmer for 15–20 minutes until tender.
2. Drain the potatoes. Warm the milk and mash it into the potatoes with the butter and salt.
3. Grill the boerewors over moderate heat, turning regularly, until cooked through. Serve with the mash.

**Use prepared mash — 2 servings**

Ingredients:
- Prepared mash: 500 g
- Boerewors: 300 g

Steps:
1. Reheat the prepared mash in a covered microwave-safe dish, stirring at intervals, until steaming throughout.
2. Grill the boerewors over moderate heat, turning regularly, until cooked through. Serve with the reheated mash.

### 3. Vienna hot dogs

**Standard — 2 servings**

Ingredients:
- Viennas: 4 count
- Long buns: 4 count
- Water: 1000 ml

Steps:
1. Heat the water in a saucepan. Heat the Viennas in it according to their package instructions until steaming hot.
2. Drain the Viennas, split the buns and put one Vienna in each bun. Serve two per person.

### 4. Boerewors rolls

**Standard — 2 servings**

Ingredients:
- Boerewors: 300 g
- Long buns: 4 count

Steps:
1. Grill the boerewors over moderate heat, turning regularly, until cooked through.
2. Split the buns. Cut the cooked boerewors into four portions and put one portion in each bun.

### 5. Bully beef and eggs

**Standard — 2 servings**

Ingredients:
- Bully beef: 300 g
- Eggs: 4 count
- Cooking oil: 10 ml

Steps:
1. Break up the bully beef. Warm the oil in a frying pan and heat the beef until steaming hot.
2. Beat the eggs, pour them into the pan and stir gently until the eggs are fully set. Serve immediately.

### 6. Creamy cheese pasta with white sauce

**Standard — 2 servings**

Ingredients:
- Dry pasta: 200 g
- Water: 2000 ml
- Prepared white sauce: 250 ml
- Cheese: 100 g

Steps:
1. Boil the pasta in the water for the time on its packet, then drain.
2. Heat the prepared white sauce gently in the saucepan. Grate the cheese and stir it into the sauce until melted.
3. Add the drained pasta and stir over low heat until hot throughout.

### 7. Bully beef and cheese pasta

**Standard — 2 servings**

Ingredients:
- Bully beef: 300 g
- Dry pasta: 200 g
- Water: 2000 ml
- Cheese: 100 g

Steps:
1. Boil the pasta in the water following the packet timing. Reserve 50 ml of the cooking water before draining.
2. Break up the bully beef in the saucepan. Add the reserved water and heat until steaming.
3. Stir in the pasta and grated cheese. Heat gently until the cheese melts.

### 8. Plain sweet muffins

**Standard — 12 muffins**

Ingredients:
- Cake flour: 250 g
- Sugar: 120 g
- Baking powder: 10 g
- Salt: 2 g
- Eggs: 2 count
- Milk: 200 ml
- Cooking oil: 80 ml

Steps:
1. Heat the oven to 180°C and line a 12-hole muffin tin with paper cases.
2. Mix the flour, sugar, baking powder and salt in one bowl. Beat the eggs, milk and oil in another.
3. Fold the wet mixture into the dry ingredients just until no dry flour remains. Divide between the cases.
4. Bake for 20–25 minutes until risen and a skewer inserted in the centre comes out clean. Cool on a rack.

### 9. Cheese savoury muffins

**Standard — 12 muffins**

Ingredients:
- Cake flour: 250 g
- Cheese: 150 g
- Baking powder: 10 g
- Salt: 2 g
- Eggs: 2 count
- Milk: 200 ml
- Cooking oil: 60 ml

Steps:
1. Heat the oven to 180°C and line a 12-hole muffin tin with paper cases.
2. Mix the flour, baking powder, salt and grated cheese. In another bowl beat the eggs, milk and oil.
3. Fold the wet mixture into the dry ingredients just until combined. Divide between the cases.
4. Bake for 20–25 minutes until golden and a skewer comes out clean. Cool before serving.

### 10. Frikkadels

**Standard — 4 servings**

Ingredients:
- Beef mince: 500 g
- Onions: 100 g
- Bread: 60 g
- Milk: 60 ml
- Eggs: 1 count
- Salt: 4 g
- Black pepper: 1 g
- Cooking oil: 20 ml

Steps:
1. Finely chop the onion. Soak the bread in the milk and mash with a fork.
2. Mix the mince, onion, soaked bread, egg, salt and pepper. Shape into eight evenly sized meatballs.
3. Heat the oil in a large frying pan. Brown the meatballs on all sides, then reduce the heat and cover.
4. Cook gently, turning, until the centres are fully cooked. Serve the meatballs on their own.

### 11. Bobotie

**Standard — 4 servings**

Ingredients:
- Beef mince: 500 g
- Onions: 150 g
- Bread: 60 g
- Milk: 250 ml
- Eggs: 2 count
- Cooking oil: 15 ml
- Curry powder: 10 g
- Chutney: 40 g
- Raisins: 40 g
- Salt: 4 g

Steps:
1. Heat the oven to 180°C. Chop the onion and soften it in the oil in a frying pan.
2. Add the mince and cook, breaking it up. Stir in the curry powder and salt.
3. Soak the bread in 100 ml of the milk. Mash it and stir it into the mince with the chutney and raisins. Cook for five minutes, then transfer to a baking dish.
4. Beat the eggs with the remaining 150 ml milk. Pour over the mince and bake for about 35 minutes until the topping is set and the centre is piping hot.

### 12. Tomato bredie

**Standard — 4 servings**

Ingredients:
- Stewing lamb: 600 g
- Tomatoes: 600 g
- Onions: 150 g
- Potatoes: 400 g
- Cooking oil: 20 ml
- Water: 400 ml
- Salt: 5 g
- Black pepper: 1 g

Steps:
1. Cut the lamb into chunks, chop the onion and tomatoes, and peel and cube the potatoes.
2. Heat the oil in a heavy pot. Brown the lamb in batches, then soften the onion in the same pot.
3. Return all the lamb to the pot. Add the tomatoes, water, salt and pepper. Cover and simmer gently for 60 minutes.
4. Add the potatoes. Cover and simmer for another 30–45 minutes until the lamb and potatoes are tender, stirring occasionally.

### 13. Beef potjiekos

**Standard — 4 servings**

Ingredients:
- Stewing beef: 600 g
- Onions: 150 g
- Carrots: 250 g
- Potatoes: 400 g
- Prepared beef stock: 750 ml
- Cooking oil: 20 ml
- Salt: 3 g
- Black pepper: 1 g

Steps:
1. Cut the beef into chunks. Chop the onion and cut the carrots and potatoes into thick pieces.
2. Heat the oil in a heavy pot or potjie. Brown the beef in batches and soften the onion.
3. Add the stock, salt and pepper. Cover and simmer gently for 90 minutes.
4. Layer the carrots and potatoes on top. Cover and cook gently for another 45–60 minutes until the beef and vegetables are tender. Check the liquid level and reduce the heat if it boils rapidly.

### 14. Chicken pie

**Standard — 4 servings**

Ingredients:
- Boneless chicken: 500 g
- Onions: 100 g
- Butter: 30 g
- Cake flour: 30 g
- Milk: 350 ml
- Puff pastry: 250 g
- Eggs: 1 count
- Salt: 4 g
- Black pepper: 1 g

Steps:
1. Dice the chicken and onion. Melt the butter in a pan and gently cook the onion, then add the chicken and cook it through.
2. Stir in the flour. Gradually add the milk while stirring, then add the salt and pepper. Simmer until thickened.
3. Transfer the filling to a pie dish and let it cool until no longer steaming. Heat the oven to 200°C.
4. Cover with the puff pastry without rolling it on a floured surface. Cut a steam vent and brush with the beaten egg.
5. Bake for 25–30 minutes until the pastry is golden and the filling is piping hot.

### 15. Vetkoek with curried mince

**Standard — 8 vetkoek (4 servings)**

Ingredients:
- Cake flour: 500 g
- Instant yeast: 7 g
- Sugar: 20 g
- Salt: 8 g
- Water: 425 ml
- Cooking oil: 1015 ml
- Beef mince: 400 g
- Onions: 150 g
- Curry powder: 10 g
- Tomatoes: 200 g

Steps:
1. Mix the flour, yeast, sugar and 5 g of the salt. Add 325 ml lukewarm water and knead in the bowl until smooth. Cover and leave to rise until doubled, about one hour.
2. For the filling, chop the onion and tomatoes. Heat 15 ml of the oil in a pan, soften the onion and brown the mince. Add the curry powder, tomatoes, remaining 3 g salt and remaining 100 ml water. Simmer for 20 minutes until cooked and thick.
3. Divide the dough into eight pieces on baking paper, shape and rest for 15 minutes.
4. Heat the remaining 1000 ml oil in a deep, stable saucepan to about 170°C; the oil must fill less than half the pan. Fry in small batches, turning, until golden and cooked through. Drain on kitchen paper.
5. Split the vetkoek and spoon the hot mince into them.

### 16. Braaibroodjies

**Standard — 2 servings**

Ingredients:
- Bread: 160 g
- Cheese: 100 g
- Tomatoes: 120 g
- Onions: 40 g
- Butter: 20 g

Steps:
1. Use four bread slices weighing about 160 g in total. Thinly slice the tomato and onion and grate the cheese.
2. Spread the butter on the outside faces of the bread. Place cheese, tomato and onion between each pair of slices.
3. Toast in a braai grid over gentle coals, or in a dry pan over low heat, turning until both sides are golden and the cheese has melted.

### 17. Pumpkin fritters

**Standard — 12 small fritters**

Ingredients:
- Cooked pumpkin: 300 g
- Cake flour: 120 g
- Eggs: 1 count
- Baking powder: 5 g
- Salt: 1 g
- Sugar: 30 g
- Ground cinnamon: 2 g
- Cooking oil: 60 ml

Steps:
1. Mash the cooked pumpkin and drain any loose liquid. Beat in the egg, then stir in the flour, baking powder and salt.
2. Mix the sugar and cinnamon separately for coating.
3. Heat the oil in a frying pan over medium heat. Fry spoonfuls of batter in batches, turning once, until golden and cooked through.
4. Drain on kitchen paper and sprinkle with the cinnamon sugar.

### 18. Potato bake

**Standard — 4 side servings**

Ingredients:
- Potatoes: 800 g
- Cream: 250 ml
- Milk: 150 ml
- Cheese: 100 g
- Salt: 4 g
- Black pepper: 1 g

Steps:
1. Heat the oven to 180°C. Peel the potatoes and slice them very thinly.
2. Layer the potatoes in a baking dish with the salt and pepper. Pour over the cream and milk.
3. Cover with foil and bake for 60 minutes. Remove the foil, add grated cheese and bake for another 20–30 minutes until the potatoes are tender. Rest for 10 minutes before serving.

### 19. Milk tart

**Standard — 8 slices**

Ingredients:
- Cake flour: 200 g
- Butter: 100 g
- Sugar: 140 g
- Eggs: 3 count
- Milk: 750 ml
- Cornflour: 45 g
- Vanilla essence: 5 ml
- Ground cinnamon: 2 g

Steps:
1. For the crust, mix 80 g softened butter with 40 g sugar, then beat in one egg. Mix in the flour to form a soft dough. Press into a 23 cm tart tin and chill for 20 minutes.
2. Heat the oven to 180°C. Line the crust with baking paper and baking weights. Bake for 15 minutes, remove the paper and weights, then bake for another 10 minutes until cooked and lightly golden.
3. For the filling, whisk the remaining two eggs with the cornflour, remaining 100 g sugar and 150 ml of the milk.
4. Heat the remaining 600 ml milk until steaming. Slowly whisk it into the egg mixture, return to the saucepan and cook over low heat while stirring until thick and gently bubbling. Cook for a further two minutes.
5. Remove from heat and stir in the remaining 20 g butter and the vanilla. Pour into the crust, sprinkle with cinnamon, cool and refrigerate until set.

### 20. Malva pudding

**Standard — 6 servings**

Ingredients:
- Cake flour: 150 g
- Sugar: 200 g
- Eggs: 1 count
- Apricot jam: 30 g
- Milk: 150 ml
- Bicarbonate of soda: 5 g
- White vinegar: 15 ml
- Butter: 80 g
- Cream: 200 ml
- Water: 100 ml
- Salt: 1 g

Steps:
1. Heat the oven to 180°C. Use 5 g of the butter to grease a medium baking dish.
2. Beat the egg with 120 g of the sugar, then beat in the apricot jam. Melt 15 g butter and mix it with the milk and vinegar.
3. Mix the flour, bicarbonate of soda and salt. Fold this into the egg mixture alternately with the milk mixture. Pour into the dish.
4. Bake for 30–40 minutes until risen and a skewer inserted into the centre comes out clean.
5. Gently heat the cream, water, remaining 80 g sugar and remaining 60 g butter in a saucepan, stirring until dissolved.
6. Prick the hot pudding and slowly pour the hot sauce over it. Allow it to absorb before serving.

## Review checklist

- Confirm the ingredient choices and batch sizes suit the intended recipes.
- Check both pap versions and both mash versions.
- Keep the water requirement in mind when testing strict matching.
- Before the recipe screen is complete, verify the data loader and matcher using missing-ingredient, insufficient-quantity, duplicate-item, compatible-unit and incompatible-unit cases.
