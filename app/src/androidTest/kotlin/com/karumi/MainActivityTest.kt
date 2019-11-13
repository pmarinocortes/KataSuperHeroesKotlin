package com.karumi

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.karumi.data.repository.SuperHeroRepository
import com.karumi.domain.model.SuperHero
import com.karumi.recyclerview.RecyclerItemViewAssertion
import com.karumi.recyclerview.RecyclerViewInteraction
import com.karumi.ui.view.MainActivity
import com.karumi.ui.view.SuperHeroDetailActivity
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class MainActivityTest : AcceptanceTest<MainActivity>(MainActivity::class.java) {
    companion object {
        const val NUMBER_OF_SUPER_HEROES = 10
    }

    @Mock
    lateinit var repository: SuperHeroRepository

    @Test
    fun showsEmptyCaseIfThereAreNoSuperHeroes() {
        givenThereAreNoSuperHeroes()

        startActivity()

        onView(withText("¯\\_(ツ)_/¯")).check(matches(isDisplayed()))
    }

    @Test
    fun avengerBadgeIsShown() {
        val superHeroes = givenThereAreSomeAvengers(NUMBER_OF_SUPER_HEROES)
        givenThereAreSomeAvengers(NUMBER_OF_SUPER_HEROES)

        startActivity()

        RecyclerViewInteraction.onRecyclerView<SuperHero>(ViewMatchers.withId(R.id.recycler_view))
            .withItems(superHeroes)
            .check { _, view, exception ->
                matches(
                    ViewMatchers.hasDescendant(
                        CoreMatchers.allOf<View>(
                            ViewMatchers.withId(R.id.iv_avengers_badge),
                            ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                        )
                    )
                ).check(view, exception)
            }
    }

    @Test
    fun avengerBadgeIsNotShown() {
        val superHeroes = givenThereAreNoAvengers(NUMBER_OF_SUPER_HEROES)
        givenThereAreSomeAvengers(NUMBER_OF_SUPER_HEROES)

        startActivity()

        RecyclerViewInteraction.onRecyclerView<SuperHero>(ViewMatchers.withId(R.id.recycler_view))
            .withItems(superHeroes)
            .check { _, view, exception ->
                matches(
                    ViewMatchers.hasDescendant(
                        CoreMatchers.allOf<View>(
                            ViewMatchers.withId(R.id.iv_avengers_badge),
                            ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                        )
                    )
                ).check(view, exception)
            }
    }

    @Test
    fun givenRecyclerItemTappedThenSuperHeroDetailIsOpened() {
        val superHeroes = givenThereAreSomeSuperHeroes(NUMBER_OF_SUPER_HEROES)

        startActivity()

        onView(ViewMatchers.withId(R.id.recycler_view))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, ViewActions.click()))

        val superHeroSelected = superHeroes[0]
        Intents.intended(IntentMatchers.hasComponent(SuperHeroDetailActivity::class.java.canonicalName))
        Intents.intended(IntentMatchers.hasExtra("super_hero_name_key", superHeroSelected.name))
    }

    private fun givenThereAreNoSuperHeroes() {
        whenever(repository.getAllSuperHeroes()).thenReturn(emptyList())
    }

    private fun givenThereAreSomeSuperHeroes(
        numberOfSuperHeroes: Int = NUMBER_OF_SUPER_HEROES,
        avengers: Boolean = false
    ): List<SuperHero> {
        val superHeroes = IntRange(0, numberOfSuperHeroes - 1).map {
            val superHeroName = "SuperHero - $it"
            val superHeroPhoto = "https://i.annihil.us/u/prod/marvel/i/mg/c/60/55b6a28ef24fa.jpg"
            val superHeroDescription = "Description Super Hero - $it"
            val superHero = SuperHero(superHeroName, superHeroPhoto, avengers, superHeroDescription)
            superHero
        }

        superHeroes.forEach { whenever(repository.getByName(it.name)).thenReturn(it) }
        whenever(repository.getAllSuperHeroes()).thenReturn(superHeroes)
        return superHeroes
    }

    private fun givenThereAreSomeAvengers(numberOfAvengers: Int): List<SuperHero> =
        givenThereAreSomeSuperHeroes(numberOfAvengers, avengers = true)

    private fun givenThereAreNoAvengers(numberOfAvenger: Int): List<SuperHero> =
        givenThereAreSomeSuperHeroes(numberOfAvenger, false)

    override val testDependencies = Kodein.Module(allowSilentOverride = true) {
        bind<SuperHeroRepository>() with instance(repository)
    }
}