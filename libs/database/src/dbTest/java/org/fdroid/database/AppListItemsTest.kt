package org.fdroid.database

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import org.fdroid.LocaleChooser.getBestLocale
import org.fdroid.database.AppListSortOrder.LAST_UPDATED
import org.fdroid.database.AppListSortOrder.NAME
import org.fdroid.database.TestUtils.getOrFail
import org.fdroid.index.v2.MetadataV2
import org.fdroid.test.TestAppUtils.getRandomMetadataV2
import org.fdroid.test.TestRepoUtils.getRandomRepo
import org.fdroid.test.TestUtils.getRandomString
import org.fdroid.test.TestVersionUtils.getRandomPackageVersionV2
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.collections.map
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
internal class AppListItemsTest : AppTest() {

    private val pm: PackageManager = mockk()

    private val appPairs = listOf(
        Pair(packageName1, app1),
        Pair(packageName2, app2),
        Pair(packageName3, app3),
    )

    @Test
    fun testSearchQuery() {
        val app1 = app1.copy(name = mapOf("en-US" to "One"), summary = mapOf("en-US" to "Onearry"))
        val app2 = app2.copy(name = mapOf("en-US" to "Two"), summary = mapOf("de" to "Zfassung"))
        val app3 = app3.copy(name = mapOf("de-DE" to "Drei"), summary = mapOf("de" to "Zfassung"))
        // insert three apps in a random order
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName1, app1, locales)
        appDao.insert(repoId, packageName3, app3, locales)
        appDao.insert(repoId, packageName2, app2, locales)

        // one of the apps is installed
        val packageInfo2 = PackageInfo().apply {
            packageName = packageName2
            versionName = getRandomString()
            versionCode = Random.nextInt(1, Int.MAX_VALUE)
        }
        every { pm.getInstalledPackages(0) } returns listOf(packageInfo2)

        // get first app by search, sort order doesn't matter
        appDao.getAppListItems(pm, "One", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(1, apps.size)
            assertEquals(app1, apps[0])
        }

        // get first app by partial search, sort by name
        appDao.getAppListItems(pm, "On", NAME).getOrFail().let { apps ->
            assertEquals(1, apps.size)
            assertEquals(app1, apps[0])
        }

        // get second app by search, sort order doesn't matter
        appDao.getAppListItems(pm, "Two", NAME).getOrFail().let { apps ->
            assertEquals(1, apps.size)
            assertEquals(app2, apps[0])
            assertEquals(PackageInfoCompat.getLongVersionCode(packageInfo2),
                apps[0].installedVersionCode)
            assertEquals(packageInfo2.versionName, apps[0].installedVersionName)
        }

        // get second and third app by searching for summary
        appDao.getAppListItems(pm, "Zfassung", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(2, apps.size)
            // sort-order isn't fixes, yet
            if (apps[0].packageName == packageName2) {
                assertEquals(app2, apps[0])
                assertEquals(app3, apps[1])
            } else {
                assertEquals(app3, apps[0])
                assertEquals(app2, apps[1])
            }
        }

        // empty search for unknown search term
        appDao.getAppListItems(pm, "foo bar", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(0, apps.size)
        }
    }

    @Test
    fun testSearchQueryInCategory() {
        val app1 = app1.copy(name = mapOf("en-US" to "One"), summary = mapOf("en-US" to "Onearry"))
        val app2 = app2.copy(name = mapOf("en-US" to "Two"), summary = mapOf("de" to "Zfassung"))
        val app3 = app3.copy(name = mapOf("de-DE" to "Drei"), summary = mapOf("de" to "Zfassung"))
        // insert three apps in a random order
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName1, app1, locales)
        appDao.insert(repoId, packageName3, app3, locales)
        appDao.insert(repoId, packageName2, app2, locales)

        // one of the apps is installed
        val packageInfo2 = PackageInfo().apply {
            packageName = packageName2
            versionName = getRandomString()
            versionCode = Random.nextInt(1, Int.MAX_VALUE)
        }
        every { pm.getInstalledPackages(0) } returns listOf(packageInfo2)

        // get first app by search, sort order doesn't matter
        appDao.getAppListItems(pm, "A", "One", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(1, apps.size)
            assertEquals(app1, apps[0])
        }

        // get second app by search, sort order doesn't matter
        appDao.getAppListItems(pm, "A", "Two", NAME).getOrFail().let { apps ->
            assertEquals(1, apps.size)
            assertEquals(app2, apps[0])
            assertEquals(PackageInfoCompat.getLongVersionCode(packageInfo2),
                apps[0].installedVersionCode)
            assertEquals(packageInfo2.versionName, apps[0].installedVersionName)
        }

        // get second and third app by searching for summary
        appDao.getAppListItems(pm, "A", "Zfassung", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(2, apps.size)
            // sort-order isn't fixes, yet
            if (apps[0].packageName == packageName2) {
                assertEquals(app2, apps[0])
                assertEquals(app3, apps[1])
            } else {
                assertEquals(app3, apps[0])
                assertEquals(app2, apps[1])
            }
        }

        // get third app by searching for summary in category B only
        appDao.getAppListItems(pm, "B", "Zfassung", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(1, apps.size)
            assertEquals(app3, apps[0])
        }

        // empty search for unknown category
        appDao.getAppListItems(pm, "C", "Zfassung", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(0, apps.size)
        }

        // empty search for unknown search term
        appDao.getAppListItems(pm, "A", "foo bar", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(0, apps.size)
        }
    }

    @Test
    fun testSearchQueryPerRepo() {
        val app1 = app1.copy(name = mapOf("en-US" to "One"), summary = mapOf("en-US" to "Onearry"))
        val app2 = app2.copy(name = mapOf("en-US" to "Two"), summary = mapOf("de" to "Zfassung"))
        val app3a = app3.copy(name = mapOf("de-DE" to "Drei"), summary = mapOf("de" to "Zfassung"))
        val app3b = app3.copy(name = mapOf("en-US" to "Three"), summary = mapOf("en" to "summary"))
        // insert three apps in a random order
        val repoId1 = repoDao.insertOrReplace(getRandomRepo())
        val repoId2 = repoDao.insertOrReplace(getRandomRepo())
        val repoId3 = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId1, packageName1, app1, locales)
        appDao.insert(repoId2, packageName2, app2, locales)
        appDao.insert(repoId2, packageName3, app3a, locales)
        appDao.insert(repoId3, packageName3, app3b, locales)

        // one of the apps is installed
        val packageInfo2 = PackageInfo().apply {
            packageName = packageName2
            versionName = getRandomString()
            versionCode = Random.nextInt(1, Int.MAX_VALUE)
        }
        every { pm.getInstalledPackages(0) } returns listOf(packageInfo2)

        // get all apps in repo2 sorted by last updated
        appDao.getAppListItems(pm, repoId2, null, LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(2, apps.size)
            assertEquals(app3a, apps[0])
            assertEquals(app2, apps[1])
            assertEquals(PackageInfoCompat.getLongVersionCode(packageInfo2),
                apps[1].installedVersionCode)
            assertEquals(packageInfo2.versionName, apps[1].installedVersionName)
        }

        // get all apps in repo2 searching for summary
        appDao.getAppListItems(pm, repoId2, "Zfassung", NAME).getOrFail().let { apps ->
            assertEquals(2, apps.size)
            val sortedApps = apps.sortedBy { it.lastUpdated }
            assertEquals(app2, sortedApps[0])
            assertEquals(app3a, sortedApps[1])
            assertEquals(PackageInfoCompat.getLongVersionCode(packageInfo2),
                sortedApps[0].installedVersionCode)
            assertEquals(packageInfo2.versionName, sortedApps[0].installedVersionName)
        }

        // get first app by searching for summary
        appDao.getAppListItems(pm, repoId1, "One", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(1, apps.size)
            assertEquals(app1, apps[0])
        }

        // get third app by searching for summary in repo3 only
        appDao.getAppListItems(pm, repoId3, "summary", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(1, apps.size)
            assertEquals(app3b, apps[0])
        }

        // empty search for unknown category
        appDao.getAppListItems(pm, 1337L, "Zfassung", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(0, apps.size)
        }

        // empty search for unknown search term
        appDao.getAppListItems(pm, repoId2, "foo bar", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(0, apps.size)
        }
    }

    @Test
    fun testMalformedSearchQuery() {
        every { pm.getInstalledPackages(0) } returns emptyList()

        // without category
        appDao.getAppListItems(pm, "\"", LAST_UPDATED).getOrFail().let { apps ->
            assertTrue(apps.isEmpty())
        }
        appDao.getAppListItems(pm, "*simple\"*", NAME).getOrFail().let { apps ->
            assertTrue(apps.isEmpty())
        }

        // with category
        appDao.getAppListItems(pm, "Category", "\"", LAST_UPDATED).getOrFail().let { apps ->
            assertTrue(apps.isEmpty())
        }
        appDao.getAppListItems(pm, "Category", "*simple\"*", NAME).getOrFail().let { apps ->
            assertTrue(apps.isEmpty())
        }
    }

    @Test
    fun testSortOrderByLastUpdated() {
        // insert three apps in a random order
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName1, app1, locales)
        appDao.insert(repoId, packageName3, app3, locales)
        appDao.insert(repoId, packageName2, app2, locales)

        // nothing is installed
        every { pm.getInstalledPackages(0) } returns emptyList()

        // get apps sorted by last updated
        appDao.getAppListItems(pm, "", LAST_UPDATED).getOrFail().let { apps ->
            assertEquals(3, apps.size)
            // we expect apps to be sorted by last updated descending
            appPairs.sortedByDescending { (_, metadataV2) ->
                metadataV2.lastUpdated
            }.forEachIndexed { i, pair ->
                assertEquals(pair.first, apps[i].packageName)
                assertEquals(pair.second, apps[i])
            }
        }
    }

    @Test
    fun testSortOrderByName() {
        // insert three apps
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName1, app1, locales)
        appDao.insert(repoId, packageName2, app2, locales)
        appDao.insert(repoId, packageName3, app3, locales)

        // nothing is installed
        every { pm.getInstalledPackages(0) } returns emptyList()

        // get apps sorted by name ascending
        appDao.getAppListItems(pm, null, NAME).getOrFail().let { apps ->
            assertEquals(3, apps.size)
            // we expect apps to be sorted by last updated descending
            appPairs.sortedBy { (_, metadataV2) ->
                metadataV2.name.getBestLocale(locales)
            }.forEachIndexed { i, pair ->
                assertEquals(pair.first, apps[i].packageName)
                assertEquals(pair.second, apps[i])
            }
        }
    }

    @Test
    fun testPackageManagerInfo() {
        // insert two apps
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName1, app1, locales)
        appDao.insert(repoId, packageName2, app2, locales)

        // one of the apps is installed
        val packageInfo2 = PackageInfo().apply {
            packageName = packageName2
            versionName = getRandomString()
            versionCode = Random.nextInt(1, Int.MAX_VALUE)
        }
        every { pm.getInstalledPackages(0) } returns listOf(packageInfo2)

        // get apps sorted by name and last update, test on both lists
        listOf(
            appDao.getAppListItems(pm, "", NAME).getOrFail(),
            appDao.getAppListItems(pm, null, LAST_UPDATED).getOrFail(),
        ).forEach { apps ->
            assertEquals(2, apps.size)
            // the installed app should have app data
            val installed = if (apps[0].packageName == packageName1) apps[1] else apps[0]
            val other = if (apps[0].packageName == packageName1) apps[0] else apps[1]
            assertEquals(packageInfo2.versionName, installed.installedVersionName)
            assertEquals(
                PackageInfoCompat.getLongVersionCode(packageInfo2),
                installed.installedVersionCode
            )
            assertNull(other.installedVersionName)
            assertNull(other.installedVersionCode)
        }
    }

    @Test
    fun testCompatibility() {
        // insert two apps
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName1, app1, locales)
        appDao.insert(repoId, packageName2, app2, locales)

        // both apps are not compatible
        getItems { apps ->
            assertEquals(2, apps.size)
            assertFalse(apps[0].isCompatible)
            assertFalse(apps[1].isCompatible)
        }

        // each app gets a version
        versionDao.insert(repoId, packageName1, "1", getRandomPackageVersionV2(), true)
        versionDao.insert(repoId, packageName2, "1", getRandomPackageVersionV2(), false)

        // updating compatibility for apps
        appDao.updateCompatibility(repoId)

        // now only one is not compatible
        getItems { apps ->
            assertEquals(2, apps.size)
            if (apps[0].packageName == packageName1) {
                assertTrue(apps[0].isCompatible)
                assertFalse(apps[1].isCompatible)
            } else {
                assertFalse(apps[0].isCompatible)
                assertTrue(apps[1].isCompatible)
            }
        }
    }

    @Test
    fun testAntiFeaturesFromHighestVersion() {
        // insert one app with no versions
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName1, app1, locales)

        // app has no anti-features, because no version
        getItems { apps ->
            assertEquals(1, apps.size)
            assertNull(apps[0].antiFeatures)
            assertEquals(emptyList(), apps[0].antiFeatureKeys)
        }

        // app gets a version
        val version1 = getRandomPackageVersionV2(42)
        versionDao.insert(repoId, packageName1, "1", version1, true)

        // app has now has the anti-features of the version
        // note that installed versions don't contain anti-features, so they are ignored
        getItems(alsoInstalled = false) { apps ->
            assertEquals(1, apps.size)
            assertEquals(version1.antiFeatures.map { it.key }, apps[0].antiFeatureKeys)
        }

        // app gets another version
        val version2 = getRandomPackageVersionV2(23)
        versionDao.insert(repoId, packageName1, "2", version2, true)

        // app has now has the anti-features of the initial version still, because 2nd is lower
        // note that installed versions don't contain anti-features, so they are ignored
        getItems(alsoInstalled = false) { apps ->
            assertEquals(1, apps.size)
            assertEquals(version1.antiFeatures.map { it.key }, apps[0].antiFeatureKeys)
        }
    }

    @Test
    fun testOnlyFromEnabledRepos() {
        // insert two apps in two different repos
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        val repoId2 = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName1, app1, locales)
        appDao.insert(repoId2, packageName2, app2, locales)

        // initially both apps get returned
        getItems { apps ->
            assertEquals(2, apps.size)
        }

        // disable first repo
        repoDao.setRepositoryEnabled(repoId, false)

        // now only app from enabled repo gets returned
        getItems { apps ->
            assertEquals(1, apps.size)
            assertEquals(repoId2, apps[0].repoId)
        }
    }

    @Test
    fun testFromRepoWithHighestWeight() {
        // insert same app into three repos (repoId1 has highest weight)
        val repoId1 = repoDao.insertOrReplace(getRandomRepo())
        val repoId3 = repoDao.insertOrReplace(getRandomRepo())
        val repoId2 = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId2, packageName, app2, locales)
        appDao.insert(repoId1, packageName, app1, locales)
        appDao.insert(repoId3, packageName, app3, locales)

        // ensure expected repo weights
        val repoPrefs1 = repoDao.getRepositoryPreferences(repoId1) ?: fail()
        val repoPrefs2 = repoDao.getRepositoryPreferences(repoId2) ?: fail()
        val repoPrefs3 = repoDao.getRepositoryPreferences(repoId3) ?: fail()
        assertTrue(repoPrefs2.weight < repoPrefs3.weight)
        assertTrue(repoPrefs3.weight < repoPrefs1.weight)

        // app from repo with highest weight is returned (app1)
        getItems { apps ->
            assertEquals(1, apps.size)
            assertEquals(packageName, apps[0].packageName)
            assertEquals(app1, apps[0])
        }
    }

    @Test
    fun testFromRepoFromAppPrefs() {
        // insert same app into three repos (repoId1 has highest weight)
        val repoId1 = repoDao.insertOrReplace(getRandomRepo())
        val repoId3 = repoDao.insertOrReplace(getRandomRepo())
        val repoId2 = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId2, packageName, app2, locales)
        appDao.insert(repoId1, packageName, app1, locales)
        appDao.insert(repoId3, packageName, app3, locales)

        // app from repo1 with highest weight gets returned
        getItems { apps ->
            assertEquals(1, apps.size)
            assertEquals(packageName, apps[0].packageName)
            assertEquals(app1, apps[0])
        }

        // prefer repo3 for this app
        appPrefsDao.update(AppPrefs(packageName, preferredRepoId = repoId3))
        getItems { apps ->
            assertEquals(1, apps.size)
            assertEquals(packageName, apps[0].packageName)
            assertEquals(app3, apps[0])
        }

        // prefer repo2 for this app
        appPrefsDao.update(AppPrefs(packageName, preferredRepoId = repoId2))
        getItems { apps ->
            assertEquals(1, apps.size)
            assertEquals(packageName, apps[0].packageName)
            assertEquals(app2, apps[0])
        }

        // prefer repo1 for this app
        appPrefsDao.update(AppPrefs(packageName, preferredRepoId = repoId1))
        getItems { apps ->
            assertEquals(1, apps.size)
            assertEquals(packageName, apps[0].packageName)
            assertEquals(app1, apps[0])
        }
    }

    @Test
    fun testOnlyFromGivenCategories() {
        // nothing is installed
        every { pm.getInstalledPackages(0) } returns emptyList()

        // insert three apps
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName1, app1, locales)
        appDao.insert(repoId, packageName2, app2, locales)
        appDao.insert(repoId, packageName3, app3, locales)

        // only two apps are in category B
        listOf(
            appDao.getAppListItems(pm, "B", null, NAME).getOrFail(),
            appDao.getAppListItems(pm, "B", null, LAST_UPDATED).getOrFail(),
        ).forEach { apps ->
            assertEquals(2, apps.size)
            assertNotEquals(packageName2, apps[0].packageName)
            assertNotEquals(packageName2, apps[1].packageName)
        }

        // no app is in category C
        listOf(
            appDao.getAppListItems(pm, "C", null, NAME).getOrFail(),
            appDao.getAppListItems(pm, "C", null, LAST_UPDATED).getOrFail(),
        ).forEach { apps ->
            assertEquals(0, apps.size)
        }

        // we'll add app1 as a variant of app2, but its repo has lower weight, so no effect
        val repoId2 = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId2, packageName2, app1, locales)
        listOf(
            appDao.getAppListItems(pm, "B", null, NAME).getOrFail(),
            appDao.getAppListItems(pm, "B", null, LAST_UPDATED).getOrFail(),
        ).forEach { apps ->
            assertEquals(2, apps.size)
            assertNotEquals(packageName2, apps[0].packageName)
            assertNotEquals(packageName2, apps[1].packageName)
        }
    }

    @Test
    fun testGetInstalledAppListItems() {
        // insert three apps
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName1, app1, locales)
        appDao.insert(repoId, packageName2, app2, locales)
        appDao.insert(repoId, packageName3, app3, locales)

        // define packageInfo for each test
        val packageInfo1 = PackageInfo().apply {
            packageName = packageName1
            versionName = getRandomString()
            versionCode = Random.nextInt(1, Int.MAX_VALUE)
        }
        val packageInfo2 = PackageInfo().apply { packageName = packageName2 }
        val packageInfo3 = PackageInfo().apply { packageName = packageName3 }

        // all apps get returned, if we consider all of them installed
        every {
            pm.getInstalledPackages(0)
        } returns listOf(packageInfo1, packageInfo2, packageInfo3)
        assertEquals(3, appDao.getInstalledAppListItems(pm).getOrFail().size)

        // one apps get returned, if we consider only that one installed
        every { pm.getInstalledPackages(0) } returns listOf(packageInfo1)
        appDao.getInstalledAppListItems(pm).getOrFail().let { apps ->
            assertEquals(1, apps.size)
            assertEquals(app1, apps[0])
            // version code and version name gets taken from supplied packageInfo
            assertEquals(
                PackageInfoCompat.getLongVersionCode(packageInfo1),
                apps[0].installedVersionCode
            )
            assertEquals(packageInfo1.versionName, apps[0].installedVersionName)
        }

        // no app gets returned, if we consider none installed
        every { pm.getInstalledPackages(0) } returns emptyList()
        appDao.getInstalledAppListItems(pm).getOrFail().let { apps ->
            assertEquals(0, apps.size)
        }
    }

    @Test
    fun testGetInstalledAppListItemsMaxVars() {
        // insert an app
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName, app1, locales)

        val packageInfoCreator = { name: String ->
            PackageInfo().apply {
                packageName = name
                versionName = name
                versionCode = Random.nextInt(1, Int.MAX_VALUE)
            }
        }
        val packageInfo = packageInfoCreator(packageName)

        // sqlite has a maximum number of 999 variables that can be used in a query
        // one additional package info is added to the package lists with each test case
        val listPackageInfo = listOf(packageInfo)
        val packageInfoOk = MutableList(998) { packageInfoCreator(getRandomString()) }
        val packageInfoNotOk1 = MutableList(999) { packageInfoCreator(getRandomString()) }
        val packageInfoNotOk2 = MutableList(5000) { packageInfoCreator(getRandomString()) }

        // app gets returned no matter how many packages are installed
        every { pm.getInstalledPackages(0) } returns packageInfoOk + listPackageInfo
        assertEquals(1, appDao.getInstalledAppListItems(pm).getOrFail().size)
        every { pm.getInstalledPackages(0) } returns packageInfoNotOk1 + listPackageInfo
        assertEquals(1, appDao.getInstalledAppListItems(pm).getOrFail().size)
        every { pm.getInstalledPackages(0) } returns packageInfoNotOk2 + listPackageInfo
        assertEquals(1, appDao.getInstalledAppListItems(pm).getOrFail().size)

        // ensure they have version info set
        every { pm.getInstalledPackages(0) } returns packageInfoOk + listPackageInfo
        assertNotNull(appDao.getInstalledAppListItems(pm).getOrFail()[0].installedVersionName)
        every { pm.getInstalledPackages(0) } returns packageInfoNotOk1 + listPackageInfo
        assertNotNull(appDao.getInstalledAppListItems(pm).getOrFail()[0].installedVersionName)
        every { pm.getInstalledPackages(0) } returns packageInfoNotOk2 + listPackageInfo
        assertNotNull(appDao.getInstalledAppListItems(pm).getOrFail()[0].installedVersionName)
    }

    // region author tests
    @Test
    fun testAuthor_NoApp() {
        // should never happen, but test this just in case
        every { pm.getInstalledPackages(0) } returns emptyList()
        val author = getRandomString()

        assertFalse(appDao.hasAuthorMoreThanOneApp(author).getOrFail())
        assertTrue(appDao.getAppListItemsForAuthor(pm, author, null, NAME).getOrFail().isEmpty())
        assertTrue(appDao.getAppListItemsForAuthor(pm, author, null, LAST_UPDATED)
            .getOrFail().isEmpty())
    }

    @Test
    fun testAuthor_OneApp() {
        every { pm.getInstalledPackages(0) } returns emptyList()
        val author = getRandomString()
        val packageName = getRandomString()
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId, packageName, getRandomMetadataV2(author), locales)

        assertFalse(appDao.hasAuthorMoreThanOneApp(author).getOrFail())
        val appsForAuthor = appDao.getAppListItemsForAuthor(pm, author, null, NAME).getOrFail()
        assertEquals(1, appsForAuthor.size)
        assertEquals(1, appDao.getAppListItemsForAuthor(pm, author, null, LAST_UPDATED)
            .getOrFail().size)
        assertEquals(packageName, appsForAuthor[0].packageName)
    }

    @Test
    fun testAuthor_TwoApps() {
        every { pm.getInstalledPackages(0) } returns emptyList()
        val author = getRandomString()
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        val packageName1 = getRandomString()
        val packageName2 = getRandomString()
        val metadata2 = getRandomMetadataV2(author)
        val metadata1 = getRandomMetadataV2(author, metadata2.lastUpdated + 1)
        appDao.insert(repoId, packageName1, metadata1, locales)
        appDao.insert(repoId, packageName2, metadata2, locales)

        assertTrue(appDao.hasAuthorMoreThanOneApp(author).getOrFail())
        val appsForAuthor = appDao.getAppListItemsForAuthor(pm, author, null, LAST_UPDATED)
            .getOrFail()
        assertEquals(2, appsForAuthor.size)
        assertEquals(packageName1, appsForAuthor[0].packageName)
        assertEquals(packageName2, appsForAuthor[1].packageName)
    }

    @Test
    fun testAuthor_MultipleApps() {
        every { pm.getInstalledPackages(0) } returns emptyList()
        val author = getRandomString()
        val repoId = repoDao.insertOrReplace(getRandomRepo())
        for (i in 1..50) {
            appDao.insert(repoId, getRandomString(), getRandomMetadataV2(author), locales)
        }

        assertTrue(appDao.hasAuthorMoreThanOneApp(author).getOrFail())
        assertEquals(50, appDao.getAppListItemsForAuthor(pm, author, null, NAME).getOrFail().size)
    }

    @Test
    fun testHasAuthorMoreThanOneApp_OneAppDifferentRepos() {
        every { pm.getInstalledPackages(0) } returns emptyList()
        val author = getRandomString()
        val repoId1 = repoDao.insertOrReplace(getRandomRepo())
        val repoId2 = repoDao.insertOrReplace(getRandomRepo())
        val packageName = getRandomString()
        appDao.insert(repoId1, packageName, getRandomMetadataV2(author), locales)
        val app4 = getRandomMetadataV2(author)
        appDao.insert(repoId2, packageName, app4, locales)
        appPrefsDao.update(
            AppPrefs(
                packageName = packageName,
                preferredRepoId = repoId2,
            )
        )
        assertFalse(appDao.hasAuthorMoreThanOneApp(author).getOrFail())
        val appsForAuthor = appDao.getAppListItemsForAuthor(pm, author, null, NAME).getOrFail()
        assertEquals(1, appsForAuthor.size)
        assertEquals(packageName, appsForAuthor[0].packageName)
        assertEquals(repoId2, appsForAuthor[0].repoId)
    }

    @Test
    fun testHasAuthorMoreThanOneApp_TwoAppsDifferentRepos() {
        every { pm.getInstalledPackages(0) } returns emptyList()
        val author = getRandomString()
        val repoId1 = repoDao.insertOrReplace(getRandomRepo())
        val repoId2 = repoDao.insertOrReplace(getRandomRepo())
        appDao.insert(repoId1, getRandomString(), getRandomMetadataV2(author), locales)
        appDao.insert(repoId2, getRandomString(), getRandomMetadataV2(author), locales)

        assertTrue(appDao.hasAuthorMoreThanOneApp(author).getOrFail())
        val appsForAuthor = appDao.getAppListItemsForAuthor(pm, author, null, NAME).getOrFail()
        assertEquals(2, appsForAuthor.size)
    }
    // endregion

    /**
     * Runs the given block on all getAppListItems* methods.
     * Uses category "A" as all apps should be in that.
     */
    private fun getItems(alsoInstalled: Boolean = true, block: (List<AppListItem>) -> Unit) {
        every { pm.getInstalledPackages(0) } returns emptyList()
        appDao.getAppListItemsByName().getOrFail().let(block)
        appDao.getAppListItems(pm, "A", null, NAME).getOrFail().let(block)
        appDao.getAppListItemsByLastUpdated().getOrFail().let(block)
        appDao.getAppListItems(pm, "A", null, LAST_UPDATED).getOrFail().let(block)
        if (alsoInstalled) {
            // everything is always considered to be installed
            val packageInfo =
                PackageInfo().apply { packageName = this@AppListItemsTest.packageName }
            val packageInfo1 = PackageInfo().apply { packageName = packageName1 }
            val packageInfo2 = PackageInfo().apply { packageName = packageName2 }
            val packageInfo3 = PackageInfo().apply { packageName = packageName3 }
            every {
                pm.getInstalledPackages(0)
            } returns listOf(packageInfo, packageInfo1, packageInfo2, packageInfo3)
            appDao.getInstalledAppListItems(pm).getOrFail().let(block)
        }
    }

    private fun assertEquals(expected: MetadataV2, actual: AppListItem) {
        assertEquals(expected.name.getBestLocale(locales), actual.name)
        assertEquals(expected.summary.getBestLocale(locales), actual.summary)
        assertEquals(expected.icon.getBestLocale(locales), actual.getIcon(locales))
    }

}
