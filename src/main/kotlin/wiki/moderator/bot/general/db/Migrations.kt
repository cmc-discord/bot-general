/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package wiki.moderator.bot.general.db

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import mu.KotlinLogging
import wiki.moderator.bot.general.db.entities.MetadataEntity
import wiki.moderator.bot.general.db.entities.TagEntity
import wiki.moderator.bot.general.db.entities.WelcomeChannelEntity

private const val META_NAME: String = "db-version "

private val migrations = mutableMapOf<Int, suspend (db: MongoDatabase) -> Unit>(
	1 to { it.createCollection(MetadataEntity.COLLECTION_NAME) },

	2 to {
		it.createCollection(TagEntity.COLLECTION_NAME)
		it.createCollection(WelcomeChannelEntity.COLLECTION_NAME)
	},
)

internal object Migrations {
	private val logger = KotlinLogging.logger { }

	suspend fun migrate() {
		logger.info { "Running migrations..." }

		var version = MetadataEntity.get(META_NAME)
			?: 0

		val latestVersion = migrations.keys.max()

		while (version < latestVersion) {
			version += 1

			logger.debug { "Migrating: $META_NAME v${version - 1} -> $version" }

			migrations[version]!!(MongoDB.db)
			MetadataEntity.set(META_NAME, version)
		}

		logger.info { "Finished migrating database." }
	}
}
