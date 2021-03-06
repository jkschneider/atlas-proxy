package io.pivotal.atlas.groovy.script

open class StyleExpr(q: String): Expr(q) {
    fun lineWidth(n: Number): StyleExpr {
        return StyleExpr("$query,$n,:lw")
    }

    fun axis(n: Int): StyleExpr {
        return StyleExpr("$query,$n,:axis")
    }
}
