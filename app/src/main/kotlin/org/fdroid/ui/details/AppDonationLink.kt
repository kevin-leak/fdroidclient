package org.fdroid.ui.details

import android.content.ClipData
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.fdroid.R
import org.fdroid.ui.utils.openUriSafe

@Composable
fun AppDonationLink(
  url: String,
  modifier: Modifier = Modifier
) {
  when {
    url.startsWith("https://opencollective.com") -> {
      AppDetailsLink(
        painterResource(R.drawable.ic_donation_opencollective),
        "Open Collective",
        url,
        disableTinting = true,
      )
    }
    url.startsWith("https://liberapay.com") -> {
      AppDetailsLink(
        painterResource(R.drawable.ic_donation_liberapay),
        "Liberapay",
        url,
        disableTinting = true,
      )
    }
    url.startsWith("bitcoin:") -> {
      AppDetailsLink(
        painterResource(R.drawable.ic_donation_bitcoin),
        "Bitcoin",
        url,
        disableTinting = true,
      )
    }
    url.startsWith("litecoin:") -> {
      AppDetailsLink(
        painterResource(R.drawable.ic_donation_litecoin),
        "Litecoin",
        url,
        disableTinting = true,
      )
    }
    else -> {
      AppDetailsLink(
        rememberVectorPainter(Icons.Default.Link),
        url,
        url,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun GenericAppDonationLinkPreview() {
  AppDonationLink(
    url = "https://f-droid.org/donate",
  )
}

@Preview(showBackground = true)
@Composable
fun LiberapayAppDonationLinkPreview() {
  AppDonationLink(
    url = "https://liberapay.com/F-Droid-Data/donate",
  )
}

@Preview(showBackground = true)
@Composable
fun OpencollectiveAppDonationLinkPreview() {
  AppDonationLink(
    url = "https://opencollective.com/f-droid/donate",
  )
}

@Preview(showBackground = true)
@Composable
fun BitcoinAppDonationLinkPreview() {
  AppDonationLink(
    url = "bitcoin:bc1qd8few44yaxc3wv5ceeedhdszl238qkvu50rj4v",
  )
}

@Preview(showBackground = true)
@Composable
fun LitecoinAppDonationLinkPreview() {
  AppDonationLink(
    url = "litecoin:bc1qd8few44yaxc3wv5ceeedhdszl238qkvu50rj4v",
  )
}
