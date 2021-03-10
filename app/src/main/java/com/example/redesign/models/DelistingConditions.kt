package com.example.redesign.models

enum class DelistingConditions (val conditionName : String, val compareValue : Int) {
    CARE("관리조건", 0),
    DELIST("상폐조건", 1)
    ;

    fun compare(other : DelistingConditions) : Int{
        return when {
            this.compareValue < other.compareValue -> -1
            this.compareValue > other.compareValue -> +1
            else -> 0
        }
    }
}