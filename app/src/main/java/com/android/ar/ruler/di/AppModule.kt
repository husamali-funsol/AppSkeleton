package com.android.ar.ruler.di

import android.content.Context
import com.android.ar.ruler.databinding.FragmentHomeBinding
import com.android.ar.ruler.databinding.FragmentStartBinding
import com.android.ar.ruler.ui.activities.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(FragmentComponent::class)
object AppModule {

    @Provides
    fun fragmentStartBinding(@ActivityContext context: Context): FragmentStartBinding {
        return FragmentStartBinding.inflate((context as MainActivity).layoutInflater)
    }

    @Provides
    fun fragmentHomeBinding(@ActivityContext context: Context): FragmentHomeBinding {
        return FragmentHomeBinding.inflate((context as MainActivity).layoutInflater)
    }

}