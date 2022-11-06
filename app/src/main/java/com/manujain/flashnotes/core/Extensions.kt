package com.manujain.flashnotes.core

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.manujain.flashnotes.domain.model.Note
import com.manujain.flashnotes.domain.model.NoteState
import com.manujain.flashnotes.domain.utils.NotesOrder
import kotlinx.coroutines.launch

fun Note?.isDiff(state: NoteState): Boolean {
    return (
        (this == null) ||
            (this.title != state.title) ||
            (this.content != state.content) ||
            (this.color != state.color)
        )
}

fun NotesOrder.isDiff(order: NotesOrder): Boolean {
    return (
        this.javaClass != order.javaClass ||
            this.orderType != order.orderType
        )
}

fun Spinner.selected(action: (position: String) -> Unit) {
    this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {}
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            action(parent?.getItemAtPosition(position) as String)
        }
    }
}

fun Fragment.launchCoroutineOnStart(action: () -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            action.invoke()
        }
    }
}
