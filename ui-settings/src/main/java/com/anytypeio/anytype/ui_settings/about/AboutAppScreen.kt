package com.anytypeio.anytype.ui_settings.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.ui_settings.R

@Composable
fun AboutAppScreen(
    libraryVersion: String,
    anytypeId: String,
    version: String,
    buildNumber: Int,
    onMetaClicked: () -> Unit,
    onExternalLinkClicked: (AboutAppViewModel.ExternalLink) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Dragger()
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(
                    top = 18.dp,
                    bottom = 16.dp
                )
        ) {
            Text(
                text = stringResource(R.string.about),
                style = Title1,
                color = colorResource(R.color.text_primary)
            )
        }
        Section(title = stringResource(id = R.string.about_help_and_community))
        Option(title = stringResource(id = R.string.about_what_is_new)) {
            onExternalLinkClicked(AboutAppViewModel.ExternalLink.WhatIsNew)
        }
        Divider()
        Option(title = stringResource(id = R.string.about_anytype_community)) {
            onExternalLinkClicked(AboutAppViewModel.ExternalLink.AnytypeCommunity)
        }
        Divider()
        Option(title = stringResource(id = R.string.about_help_and_tutorials)) {
            onExternalLinkClicked(AboutAppViewModel.ExternalLink.HelpAndTutorials)
        }
        Section(title = stringResource(id = R.string.about_legal))
        Option(title = stringResource(id = R.string.about_terms_of_use)) {
            onExternalLinkClicked(AboutAppViewModel.ExternalLink.TermsOfUse)
        }
        Divider()
        Option(title = stringResource(id = R.string.about_privacy_policy)) {
            onExternalLinkClicked(AboutAppViewModel.ExternalLink.PrivacyPolicy)
        }
        Divider()
        Box(
            modifier = Modifier
                .clickable {
                    onMetaClicked()
                }
                .padding(
                    top = 26.dp,
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                )
        ) {
            Text(
                text = stringResource(
                    id = R.string.about_meta_info,
                    version,
                    buildNumber,
                    libraryVersion,
                    anytypeId
                ),
                style = Caption1Regular.copy(
                    color = colorResource(id = R.color.text_secondary)
                )
            )
        }
    }
}

@Composable
fun Option(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick.invoke() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            style = UXBody.copy(color = colorResource(id = R.color.text_primary))
        )
    }
}

@Composable
fun Section(
    modifier: Modifier = Modifier,
    title: String
) {
    Box(modifier = modifier.padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 8.dp)) {
        Text(
            text = title,
            style = Caption1Regular.copy(color = colorResource(id = R.color.text_secondary))
        )
    }
}