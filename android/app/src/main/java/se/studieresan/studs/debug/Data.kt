package se.studieresan.studs.debug

import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

sealed class Tree
data class Branch(val name: String, val subTrees: List<Tree>): Tree()
data class CollectionBranch(val name: String, val subTrees: List<Tree>): Tree()
data class Node(val name: String, val value: String): Tree()

fun <T: Any> parse(obj: T, rootName: String): Tree {
    val clazz = obj::class
    return if (clazz.isData) {
        val subTrees = obj::class.memberProperties.map { member ->
            if (member is KMutableProperty<*>)
                throw IllegalStateException("${member.name} *must* be a immutable")

            if (member.visibility == KVisibility.PUBLIC) {
                val field = member.getter.call(obj)
                if (field != null) {
                    if (field::class.isData) {
                        parse(field, member.name)
                    } else if (field is Collection<*> && field.isNotEmpty()) {
                        val sub = field
                                .mapIndexed { i, v ->
                                    if (v == null) Node(name = "$i", value = "$v")
                                    else parse(v, "$i")
                                }
                        CollectionBranch(name = member.name, subTrees = sub)
                    } else Node(name = member.name, value = "$field")
                } else {
                    Node(name = member.name, value = "$field")
                }
            } else null
        }.filterNotNull()
        return Branch(name = rootName, subTrees = subTrees)
    } else {
        Node(name = rootName, value = obj.toString())
    }
}

fun prettyPrint(tree: Tree, depth: Int = 0): String {
    fun tabs(count: Int) = " ".repeat(count * 4)
    fun indentSubTrees(subTrees: List<Tree>, depth: Int) =
            subTrees.map {
                "\n" + prettyPrint(it, depth + 1)
            }.joinToString(",")

    return when (tree) {
        is Node ->
            "${tabs(depth)}${tree.name}: ${tree.value}"

        is Branch -> {
            val subTrees = indentSubTrees(tree.subTrees, depth)
            tabs(depth) + tree.name + ": {" + subTrees + "\n" + tabs(depth) + "}"
        }

        is CollectionBranch -> {
            val subTrees = indentSubTrees(tree.subTrees, depth)
            tabs(depth) + tree.name + ": [" + subTrees + "\n" + tabs(depth) + "]"
        }
    }
}
