package org.fdroid.ui.categories

import android.app.Application
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.Collator
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import org.fdroid.database.FDroidDatabase

@HiltViewModel
class CategoriesViewModel @Inject constructor(app: Application, db: FDroidDatabase) :
  AndroidViewModel(app) {
  private val collator = Collator.getInstance(Locale.getDefault())
  private val localeList = LocaleListCompat.getDefault()

  val categories =
    db.getRepositoryDao().getLiveCategories().asFlow().map { categories ->
      categories
        .map { category ->
          CategoryItem(id = category.id, name = category.getName(localeList) ?: "Unknown Category")
        }
        .sortedWith { c1, c2 -> collator.compare(c1.name, c2.name) }
    }
}
