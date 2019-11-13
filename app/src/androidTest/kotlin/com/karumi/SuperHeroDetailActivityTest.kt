package com.karumi

import android.os.Bundle
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.runner.AndroidJUnit4
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.karumi.data.repository.SuperHeroRepository
import com.karumi.domain.model.SuperHero
import com.karumi.matchers.ToolbarMatcher.onToolbarWithTitle
import com.karumi.ui.view.SuperHeroDetailActivity
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

@RunWith(AndroidJUnit4::class)
class SuperHeroDetailActivityTest : AcceptanceTest<SuperHeroDetailActivity>(SuperHeroDetailActivity::class.java) {

    @Mock
    lateinit var repository: SuperHeroRepository

    @Test
    fun givenASuperHeroDetailActivityShowsTheSuperHeroNameAndTheTitle() {
        val superHero = givenThereIsASuperHero()
        startActivity(superHero)
        onToolbarWithTitle(superHero.name).check(matches(isDisplayed()))
        onView(allOf(withId(R.id.tv_super_hero_name), withText(superHero.name))).check(
            matches(isDisplayed())
        )
    }

    private fun givenThereIsASuperHero(isAvenger: Boolean = false): SuperHero {
        val superHeroName = "SuperHero"
        val superHeroPhoto = "https://i.annihil.us/u/prod/marvel/i/mg/c/60/55b6a28ef24fa.jpg"
        val superHeroDescription = "Super Hero Description"
        val superHero = SuperHero(superHeroName, superHeroPhoto, isAvenger, superHeroDescription)
        whenever(repository.getByName(superHeroName)).thenReturn(superHero)
        return superHero
    }

    private fun startActivity(superHero: SuperHero): SuperHeroDetailActivity {
        val args = Bundle()
        args.putString("super_hero_name_key", superHero.name)
        return startActivity(args)
    }

    override val testDependencies = Kodein.Module(allowSilentOverride = true) {
        bind<SuperHeroRepository>() with instance(repository)
    }
}