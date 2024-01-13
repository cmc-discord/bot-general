/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package template

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.modules.extra.phishing.DetectionAction
import com.kotlindiscord.kord.extensions.modules.extra.phishing.extPhishing
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.extPluralKit
import com.kotlindiscord.kord.extensions.utils.env

private val TOKEN = env("TOKEN")   // Get the bot's token from the env vars, or a .env file.

suspend fun main() {
	val bot = ExtensibleBot(TOKEN) {
		extensions {
			extPhishing {
				appName = "CMC General Bot"
				logChannelName = "app-bot-general"

				detectionAction = DetectionAction.LogOnly
				notifyUser = false
			}

			extPluralKit()

			// TODO: Tags/Welcome when storage is sorted out
		}
	}

	bot.start()
}
