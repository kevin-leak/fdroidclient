package org.fdroid.ui.details

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.net.URI
import org.fdroid.R

@Composable
fun AppDonationLink(
  link: DonateData,
  modifier: Modifier = Modifier
) {
  AppDetailsLink(
    // painterResource(R.drawable.ic_donation_opencollective),
    when (link.type) {
      DonateType.OPEN_COLLECTIVE -> painterResource(R.drawable.ic_donation_opencollective)
      DonateType.LIBERAPAY -> painterResource(R.drawable.ic_donation_liberapay)
      DonateType.BITCOIN -> painterResource(R.drawable.ic_donation_bitcoin)
      DonateType.LITECOIN -> painterResource(R.drawable.ic_donation_litecoin)
      else -> rememberVectorPainter(Icons.Default.Link)
    },
    when (link.type) {
      DonateType.OPEN_COLLECTIVE -> "Open Collective"
      DonateType.LIBERAPAY -> "Liberapay"
      DonateType.BITCOIN -> "Bitcoin"
      DonateType.LITECOIN -> "Litecoin"
      else -> link.url
    },
    link.url,
    disableTinting = link.type != DonateType.GENERIC,
    subTitle = link.subtitle,
    modifier = modifier,
  )
}

@Preview(showBackground = true)
@Composable
fun GenericAppDonationLinkPreview() {
  AppDonationLink(
    DonateData(
      "https://f-droid.org/donate",
      DonateType.GENERIC,
      null,
    ),
  )
}

@Preview(showBackground = true)
@Composable
fun LiberapayAppDonationLinkPreview() {
  AppDonationLink(
    DonateData(
      "https://liberapay.com/F-Droid-Data/donate",
      DonateType.LIBERAPAY,
      "F-Droid-Data",
    ),
  )
}

@Preview(showBackground = true)
@Composable
fun OpencollectiveAppDonationLinkPreview() {
  AppDonationLink(
    DonateData(
      "https://opencollective.com/f-droid/donate",
      DonateType.OPEN_COLLECTIVE,
      "f-droid",
    ),
  )
}

@Preview(showBackground = true)
@Composable
fun BitcoinAppDonationLinkPreview() {
  AppDonationLink(
    DonateData(
      "bitcoin:bc1qd8few44yaxc3wv5ceeedhdszl238qkvu50rj4v",
      DonateType.BITCOIN,
      "bc1qd8few44yaxc3wv5ceeedhdszl238qkvu50rj4v",
    ),
  )
}

@Preview(showBackground = true)
@Composable
fun LitecoinAppDonationLinkPreview() {
  AppDonationLink(
    DonateData(
      "litecoin:lc1asdfew44asdf3wv5asdfdhdsasdf8qkvasdffff",
      DonateType.LITECOIN,
      "lc1asdfew44asdf3wv5asdfdhdsasdf8qkvasdffff",
    ),
  )
}
