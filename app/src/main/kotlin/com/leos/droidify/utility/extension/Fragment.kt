package com.leos.droidify.utility.extension

import androidx.fragment.app.Fragment
import com.leos.droidify.ScreenActivity

inline val Fragment.screenActivity: ScreenActivity
    get() = requireActivity() as ScreenActivity
