package com.fabianofranca.mapslab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.my_bottom_sheet.*

class MyBottomSheet : BottomSheetDialogFragment() {

    override fun onStart() {
        super.onStart()

        adjustResize()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.my_bottom_sheet, container)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clDeleteConfirmation.post{
            clDeleteConfirmation
                .animate()
                .alpha(0f)
                .withEndAction {
                    clDeleteConfirmation.visibility = View.VISIBLE
                }.start()
        }

        clDeleteConfirmation.post {
            val layoutParams = clDeleteConfirmation.layoutParams
            layoutParams.width = clContainer.width
            clDeleteConfirmation.layoutParams = layoutParams
            clDeleteConfirmation.requestLayout()
        }
        clOptions.post {
            val layoutParams = clOptions.layoutParams
            layoutParams.width = clContainer.width
            clOptions.layoutParams = layoutParams
            clOptions.requestLayout()
        }


        btnDelete.setOnClickListener {
            showFadingDeleteConfirmation()
        }

        btnDeleteCancel.setOnClickListener {
            hideFadingConfirmation()
        }
    }

    private fun hideFadingConfirmation() {
        clDeleteConfirmation
            .animate()
            .alpha(0f)
            .withEndAction {
                clDeleteConfirmation.visibility = View.GONE
                clOptions.visibility = View.VISIBLE
                clOptions
                    .animate()
                    .alpha(1f)
                    .start()
            }.start()
    }

    private fun showFadingDeleteConfirmation() {
        clOptions
            .animate()
            .alpha(0f)
            .withEndAction {
                clOptions.visibility = View.GONE
                clDeleteConfirmation.visibility = View.VISIBLE
                clDeleteConfirmation
                    .animate()
                    .alpha(1f)
                    .start()
            }.start()
    }

    private fun hideSlidingConfirmation() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(clContainer)
        constraintSet.connect(
            R.id.clOptions,
            ConstraintSet.END,
            R.id.clContainer,
            ConstraintSet.END
        )
        constraintSet.connect(
            R.id.clOptions,
            ConstraintSet.START,
            R.id.clContainer,
            ConstraintSet.START
        )
        constraintSet.connect(
            R.id.clDeleteConfirmation,
            ConstraintSet.START,
            R.id.clContainer,
            ConstraintSet.END
        )
        constraintSet.clear(R.id.clDeleteConfirmation, ConstraintSet.END)
        constraintSet.applyTo(clContainer)
        TransitionManager.beginDelayedTransition(clContainer, AutoTransition())

    }

    private fun showSlidingDeleteConfirmation() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(clContainer)
        constraintSet.connect(
            R.id.clOptions,
            ConstraintSet.END,
            R.id.clContainer,
            ConstraintSet.START
        )
        constraintSet.connect(
            R.id.clDeleteConfirmation,
            ConstraintSet.START,
            R.id.clContainer,
            ConstraintSet.START
        )
        constraintSet.connect(
            R.id.clDeleteConfirmation,
            ConstraintSet.END,
            R.id.clContainer,
            ConstraintSet.END
        )
        constraintSet.clear(R.id.clOptions, ConstraintSet.START)
        constraintSet.applyTo(clContainer)
        TransitionManager.beginDelayedTransition(clContainer, AutoTransition())
    }


    companion object {
        fun newInstance(): MyBottomSheet = MyBottomSheet()
    }
}