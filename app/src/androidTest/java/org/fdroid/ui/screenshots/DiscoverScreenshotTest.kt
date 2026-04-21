package org.fdroid.ui.screenshots

import java.text.Collator
import org.fdroid.ui.discover.Discover
import org.fdroid.ui.discover.LoadedDiscoverModel
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DiscoverScreenshotTest(localeName: String) : LocalizedScreenshotTest(localeName) {

  companion object {
    @JvmStatic @Parameterized.Parameters(name = "{0}") fun locales() = locales
  }

  @Test
  fun appDetails() =
    screenshotTest("1_Discover") { localeList ->
      val collator = Collator.getInstance(localeList.get(0))
      val model =
        LoadedDiscoverModel(
          newApps = getNewApps(localeList),
          recentlyUpdatedApps = getRecentlyUpdatedApps(localeList),
          mostDownloadedApps = getMostDownloadedApps(localeList),
          categories =
            getCategoryItems(localeList).filter { it.featured }.sortedWith { c1, c2 ->
              collator.compare(c1.name, c2.name)
            },
          hasRepoIssues = false,
        )
      Discover(discoverModel = model, onListTap = {}, onAppTap = {}, onNav = {})
    }
}
