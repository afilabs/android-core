package com.support.core.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.support.core.dependenceContext
import com.support.core.functional.Creatable

class ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = dependenceContext.get(modelClass)
        if (viewModel is Creatable) viewModel.onCreate()
        return viewModel
    }
}