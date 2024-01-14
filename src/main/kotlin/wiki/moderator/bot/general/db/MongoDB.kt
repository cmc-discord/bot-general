/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package wiki.moderator.bot.general.db

import com.kotlindiscord.kord.extensions.adapters.mongodb.kordExCodecRegistry
import com.kotlindiscord.kord.extensions.utils.env
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import mu.KotlinLogging
import org.bson.BsonInt64
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import wiki.moderator.bot.general.db.entities.MetadataEntity
import wiki.moderator.bot.general.db.entities.TagEntity
import wiki.moderator.bot.general.db.entities.WelcomeChannelEntity

object MongoDB {
	private val logger = KotlinLogging.logger { }
	private val client = MongoClient.create(env("ADAPTER_MONGODB_URI"))

	val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
		kordExCodecRegistry,

		CodecRegistries.fromCodecs(
			// kotlinx.serialization codecs go here.
			MetadataEntity.codec,
			TagEntity.codec,
			WelcomeChannelEntity.codec,
		),

		MongoClientSettings.getDefaultCodecRegistry(),
	)

	val db = client.getDatabase("cmc-general-bot")

	suspend fun setup() {
		val command = Document("ping", BsonInt64(1))

		db.runCommand(command)

		logger.info { "Connected to database." }

		Migrations.migrate()
	}

	inline fun <reified T : Any> getCollection(name: String): MongoCollection<T> =
		db
			.getCollection<T>(name)
			.withCodecRegistry(codecRegistry)
}
