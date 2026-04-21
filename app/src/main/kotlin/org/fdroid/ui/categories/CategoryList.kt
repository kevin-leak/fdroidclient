package org.fdroid.ui.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import org.fdroid.R
import org.fdroid.ui.FDroidContent
import org.fdroid.ui.navigation.NavigationKey

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun CategoryList(
  categoryList: List<CategoryItem>?,
  onNav: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
) {
  val onAllCategories = { onNav(NavigationKey.Categories) }
  AnimatedVisibility(!categoryList.isNullOrEmpty()) {
    Column(modifier = modifier) {
      Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier =
          Modifier.fillMaxWidth().clickable(onClick = onAllCategories).padding(horizontal = 16.dp),
      ) {
        Text(
          text = stringResource(R.string.categories_features),
          style = MaterialTheme.typography.titleMediumEmphasized,
          fontSize = 20.sp,
        )
        TextButton(onClick = onAllCategories) { Text(stringResource(R.string.see_all)) }
      }
      if (categoryList != null) {
        // FIXME: A LazyColumn would be better, but we can't use this inside a scrollable column
        Column { categoryList.forEach { categoryItem -> CategoryRow(categoryItem, onNav) } }
      }
    }
  }
}

@Composable
@PreviewLightDark
fun CategoryListPreview() {
  FDroidContent {
    val categories =
      listOf(
        CategoryItem("App Store & Updater", "App Store & Updater"),
        CategoryItem("Browser", "Browser"),
        CategoryItem("Calendar & Agenda", "Calendar & Agenda"),
        CategoryItem("Cloud Storage & File Sync", "Cloud Storage & File Sync"),
        CategoryItem("Connectivity", "Connectivity"),
        CategoryItem("Development", "Development"),
        CategoryItem("doesn't exist", "Foo bar"),
      )
    CategoryList(categories, {})
  }
}
