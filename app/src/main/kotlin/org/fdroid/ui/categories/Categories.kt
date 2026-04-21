package org.fdroid.ui.categories

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import org.fdroid.R
import org.fdroid.ui.FDroidContent
import org.fdroid.ui.utils.BackButton
import org.fdroid.ui.utils.BigLoadingIndicator

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Categories(
  categories: List<CategoryItem>?,
  onNav: (NavKey) -> Unit,
  onBackClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollBehavior = enterAlwaysScrollBehavior(rememberTopAppBarState())
  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.main_menu__categories),
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
          )
        },
        navigationIcon = { BackButton(onClick = onBackClicked) },
        scrollBehavior = scrollBehavior,
      )
    },
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
  ) { paddingValues ->
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    if (categories == null) {
      BigLoadingIndicator(modifier.padding(paddingValues))
    } else {
      LazyColumn(
        state = listState,
        contentPadding = paddingValues,
        verticalArrangement = spacedBy(8.dp),
      ) {
        items(items = categories, key = { it.id }, contentType = { "C" }) { categoryItem ->
          CategoryRow(categoryItem, onNav)
        }
      }
    }
  }
}

@Composable
@PreviewLightDark
private fun PreviewLoading() {
  FDroidContent {
    Categories(null, {}, {})
  }
}

@Composable
@PreviewLightDark
private fun Preview() {
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
    Categories(categories, {}, {})
  }
}
