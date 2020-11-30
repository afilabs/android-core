package com.support.core.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.support.core.base.BaseFragment
import com.support.core.functional.Backable
import com.support.core.inject


class NavHostFragment : BaseFragment(0), NavigationOwner, Backable {
    companion object {
        const val START_CLASS_NAME = "fragment:navigation:start:class"
        const val NAV_OPTION = "fragment:navigation:options"
        const val ARGUMENT = "fragment:navigation:argument"
        const val ID = "fragment:navigation:id"
    }

    private var mNavigator: Navigator? = null
    override val navigator: Navigator
        get() = mNavigator ?: error("Navigator not init yet!")

    private val factory: FragmentNavigatorFactory by inject()
    private val containerId get() = arguments?.getInt(ID) ?: id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNavigator = factory.create(childFragmentManager, containerId)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return FrameLayout(requireContext()).also { it.id = containerId }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doNavigateIfNeeded()
    }

    private fun doNavigateIfNeeded() {
        val startClassName: String? = arguments?.getString(START_CLASS_NAME)
        val bundleArgs: Bundle? = arguments?.getBundle(ARGUMENT)
        val navOptions: NavOptions? = arguments?.getParcelable(NAV_OPTION)
        if (startClassName.isNullOrEmpty()) return
        mNavigator?.navigate(
                Class.forName(startClassName).asSubclass(Fragment::class.java).kotlin,
                bundleArgs,
                navOptions
        )
    }

    override fun onInterceptBackPress(): Boolean {
        return mNavigator?.navigateUp() ?: false
    }
}