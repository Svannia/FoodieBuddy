package com.example.foodiebuddy.data

import android.net.Uri

data class UserPersonal(val uid: String, val favouriteRecipes: List<Recipe>, val groceryList: Map<String, List<OwnedIngredient>>, val fridge: Map<String, List<OwnedIngredient>>) {
    companion object {
        /**
         * Creates an empty UserPersonal object.
         *
         * @return empty UserPersonal object
         */
        fun empty(): UserPersonal {
            return UserPersonal("", emptyList<Recipe>(), emptyMap<String, List<OwnedIngredient>>(), emptyMap<String, List<OwnedIngredient>>())
        }
    }
    /**
     * Checks if this User data object is empty.
     *
     * @return true if the User data object is empty
     */
    fun isEmpty(): Boolean {
        return this == empty()
    }
}