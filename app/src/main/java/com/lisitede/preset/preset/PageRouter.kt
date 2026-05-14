package com.lisitede.preset.preset

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDestination
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import com.therouter.TheRouter
import com.therouter.router.RouteItem
import com.therouter.router.matchRouteMap

object PageRouter {

    private const val INTENT_KEY_PATHNAME = "com.lisitede.preset.preset.intent.PATHNAME"

    fun navigate(context: Context, pathname: String, query: Bundle? = null) {
        val routeItem = matchRouteMap(pathname)
        if (routeItem != null && isFragment(routeItem)) {
            val activity = context as? AppCompatActivity
            if (activity != null) {
                val navHostFragment = findNavHostFragment(activity)
                if (navHostFragment != null) {
                    val destId = findDestinationId(navHostFragment.navController.graph, routeItem.className)
                    if (destId != null) {
                        navHostFragment.navController.navigate(destId, query)
                        return
                    }
                }
            }
        }

        if (routeItem != null && !isFragment(routeItem)) {
            TheRouter.build(pathname)
                .apply { query?.keySet()?.forEach { key -> withString(key, query.getString(key)) } }
                .navigation(context)
            return
        }

        val parentPathname = pathname.substringBeforeLast("/")
        val parentItem = matchRouteMap(parentPathname)
        if (parentItem != null && !isFragment(parentItem)) {
            TheRouter.build(parentPathname)
                .withString(INTENT_KEY_PATHNAME, pathname)
                .apply { query?.keySet()?.forEach { key -> withString(key, query.getString(key)) } }
                .navigation(context)
            return
        }

        TheRouter.build(pathname)
            .apply { query?.keySet()?.forEach { key -> withString(key, query.getString(key)) } }
            .navigation(context)
    }

    /**
     * Navigate to the target page and finish the source Activity.
     *
     * Used for cross-Activity navigation where the source Activity must not remain
     * in the back stack (e.g. login -> main, logout -> login, plan -> main/home).
     * For same-Activity page switches, use [navigate] instead.
     */
    fun navigateAndFinish(activity: Activity, pathname: String, query: Bundle? = null) {
        navigate(activity, pathname, query)
        activity.finish()
    }

    /**
     * Navigate to the page specified by the incoming Intent's INTENT_KEY_PATHNAME.
     *
     * When a cross-Activity navigation passes a full page pathname (e.g. "/main/home"),
     * TheRouter only resolves the Activity part ("/main"). The full pathname is carried
     * via INTENT_KEY_PATHNAME so the target Activity can navigate its NavController
     * to the correct PageFragment inside its own nav graph.
     *
     * If INTENT_KEY_PATHNAME is absent or matches an Activity-level pathname (e.g. "/main"),
     * no additional navigation is performed — the Activity's start destination applies.
     *
     * Should be called in Activity.onCreate() after the NavHostFragment is set up.
     */
    fun navigateToPage(activity: AppCompatActivity) {
        val pathname = activity.intent.getStringExtra(INTENT_KEY_PATHNAME) ?: return
        val routeItem = matchRouteMap(pathname) ?: return
        if (!isFragment(routeItem)) return
        val navHostFragment = findNavHostFragment(activity) ?: return
        val destId = findDestinationId(navHostFragment.navController.graph, routeItem.className) ?: return
        navHostFragment.navController.navigate(destId)
    }

    private fun matchRouteMap(pathname: String): RouteItem? {
        return com.therouter.router.matchRouteMap(pathname)
    }

    private fun isFragment(routeItem: RouteItem): Boolean {
        val className = routeItem.className ?: return false
        return try {
            val clazz = Class.forName(className)
            androidx.fragment.app.Fragment::class.java.isAssignableFrom(clazz)
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    private fun findNavHostFragment(activity: AppCompatActivity): NavHostFragment? {
        return activity.supportFragmentManager.fragments
            .filterIsInstance<NavHostFragment>()
            .firstOrNull()
    }

    private fun findDestinationId(destination: NavDestination, className: String): Int? {
        if (destination is FragmentNavigator.Destination && destination.className == className) {
            return destination.id
        }
        if (destination is androidx.navigation.NavGraph) {
            for (child in destination) {
                val result = findDestinationId(child, className)
                if (result != null) return result
            }
        }
        return null
    }
}
