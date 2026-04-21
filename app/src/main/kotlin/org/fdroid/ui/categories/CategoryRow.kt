package org.fdroid.ui.categories

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import org.fdroid.ui.lists.AppListType
import org.fdroid.ui.navigation.NavigationKey

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun CategoryRow(categoryItem: CategoryItem, onNav: (NavKey) -> Unit) {
  ListItem(
    leadingContent = {
      Icon(
        imageVector = categoryItem.imageVector,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.semantics { hideFromAccessibility() },
      )
    },
    onClick = {
      val type = AppListType.Category(categoryItem.name, categoryItem.id)
      val navKey = NavigationKey.AppList(type)
      onNav(navKey)
    },
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Text(text = categoryItem.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
  }
}
