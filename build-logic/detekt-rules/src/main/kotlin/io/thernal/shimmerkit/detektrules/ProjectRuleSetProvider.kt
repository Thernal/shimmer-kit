package io.thernal.shimmerkit.detektrules

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider
import io.thernal.shimmerkit.detektrules.collections.UnsafeCollectionIndexAccess
import io.thernal.shimmerkit.detektrules.packageboundary.LayerPackageBoundary
import io.thernal.shimmerkit.detektrules.packageboundary.LayerPackageRequired
import io.thernal.shimmerkit.detektrules.preview.PreviewMustBePrivate
import io.thernal.shimmerkit.detektrules.style.ExpressionBodyNotAllowed
import io.thernal.shimmerkit.detektrules.style.MultilineConstructorRequired

class ProjectRuleSetProvider : RuleSetProvider {
    override val ruleSetId = RuleSetId("project")

    override fun instance() = RuleSet(
        ruleSetId,
        listOf(
            ::PreviewMustBePrivate,
            ::UnsafeCollectionIndexAccess,
            ::LayerPackageBoundary,
            ::LayerPackageRequired,
            ::ExpressionBodyNotAllowed,
            ::MultilineConstructorRequired,
        ),
    )
}
