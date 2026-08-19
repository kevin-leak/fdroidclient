package org.fdroid.ui.details

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.fdroid.ui.icons.Bitcoin
import org.fdroid.ui.icons.Liberapay
import org.fdroid.ui.icons.Litecoin
import org.fdroid.ui.icons.OpenCollective

@Composable
fun AppDonationLink(
  link: DonateLink,
  modifier: Modifier = Modifier
) {
  AppDetailsLink(
    when (link.type) {
      DonateType.OPEN_COLLECTIVE -> OpenCollective
      DonateType.LIBERAPAY -> Liberapay
      DonateType.BITCOIN -> Bitcoin
      DonateType.LITECOIN -> Litecoin
      DonateType.GENERIC -> Icons.Default.Link
    },
    when (link.type) {
      DonateType.OPEN_COLLECTIVE -> "Open Collective"
      DonateType.LIBERAPAY -> "Liberapay"
      DonateType.BITCOIN -> "Bitcoin"
      DonateType.LITECOIN -> "Litecoin"
      DonateType.GENERIC -> link.url
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
    DonateLink(
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
    DonateLink(
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
    DonateLink(
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
    DonateLink(
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
    DonateLink(
      "litecoin:lc1asdfew44asdf3wv5asdfdhdsasdf8qkvasdffff",
      DonateType.LITECOIN,
      "lc1asdfew44asdf3wv5asdfdhdsasdf8qkvasdffff",
    ),
  )
}
