package com.fabianofranca.mapslab

import android.view.WindowManager
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

fun BottomSheetDialogFragment.adjustResize() {
    dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
}

fun BottomSheetDialogFragment.setStateExpanded() {
    dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.run {
        BottomSheetBehavior.from(this).state = BottomSheetBehavior.STATE_EXPANDED
    }
}