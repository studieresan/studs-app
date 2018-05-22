package se.studieresan.studs.debug.list

import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.Group
import se.studieresan.studs.debug.Branch
import se.studieresan.studs.debug.CollectionBranch
import se.studieresan.studs.debug.Node
import se.studieresan.studs.debug.Tree

fun fromTree(tree: Tree, depth: Int = 0): Group {
    fun groupFrom(header: String, subTrees: Collection<Tree>): Group {
        val group = ExpandableGroup(HeaderItem(header, indentation = depth))
        val sub = subTrees
                .map { fromTree(it, depth = depth + 1) }
                .toSet()
        group.addAll(sub)
        return group
    }
    return when (tree) {
        is Node -> NormalItem(
                name = tree.name,
                value = tree.value,
                indentation = depth
        )

        is Branch -> groupFrom(tree.name, tree.subTrees)

        is CollectionBranch -> groupFrom(tree.name, tree.subTrees)
    }
}
