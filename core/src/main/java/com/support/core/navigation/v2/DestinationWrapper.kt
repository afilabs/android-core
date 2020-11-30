package com.support.core.navigation.v2

import androidx.fragment.app.Fragment
import com.support.core.navigation.Destination

class DestinationWrapper(
        val destination: Destination,
        val fragment: Fragment,
        val isUpdateCurrent: Boolean = false
)