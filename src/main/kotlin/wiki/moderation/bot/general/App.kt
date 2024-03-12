/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package wiki.moderation.bot.general

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.adapters.mongodb.mongoDB
import com.kotlindiscord.kord.extensions.checks.hasRole
import com.kotlindiscord.kord.extensions.modules.extra.phishing.DetectionAction
import com.kotlindiscord.kord.extensions.modules.extra.phishing.extPhishing
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.extPluralKit
import com.kotlindiscord.kord.extensions.modules.extra.tags.tags
import com.kotlindiscord.kord.extensions.modules.extra.welcome.welcomeChannel
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.asChannelOfOrNull
import kotlinx.coroutines.flow.firstOrNull
import wiki.moderation.bot.general.db.MongoDB
import wiki.moderation.bot.general.db.entities.TagEntity
import wiki.moderation.bot.general.db.entities.WelcomeChannelEntity

private val TOKEN = env("TOKEN")   // Get the bot's token from the env vars, or a .env file.

@Suppress("StringLiteralDuplication")
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

			tags(TagEntity) {
				loggingChannelName = "app-bot-general"

				staffCommandCheck {
					hasRole(Snowflake("1131547978331590808"))
				}
			}

			welcomeChannel(WelcomeChannelEntity) {
				getLogChannel { _, guild ->
					guild.channels.firstOrNull {
						it.name.equals("app-bot-general", ignoreCase = true)
					}?.asChannelOfOrNull()
				}

				staffCommandCheck {
					hasRole(Snowflake("1131547978331590808"))
				}
			}

			sentry {
				enableIfDSN(envOrNull("SENTRY_DSN"))
			}
		}

		hooks {
			beforeKoinSetup {
				MongoDB.setup()
			}
		}

		mongoDB()
	}

	bot.start()
}
