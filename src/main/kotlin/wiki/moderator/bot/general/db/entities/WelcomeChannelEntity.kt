/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package wiki.moderator.bot.general.db.entities

import com.kotlindiscord.kord.extensions.modules.extra.welcome.data.WelcomeChannelData
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.UpdateResult
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.kotlinx.KotlinSerializerCodec
import wiki.moderator.bot.general.db.MongoDB

@Serializable
@Suppress("DataClassContainsFunctions", "DataClassShouldBeImmutable")
internal data class WelcomeChannelEntity(
	@Contextual
	override val _id: Snowflake,

	var url: String
) : Entity<Snowflake> {
	suspend inline fun save(): UpdateResult =
		save(this)

	companion object : WelcomeChannelData {
		const val COLLECTION_NAME: String = "welcome-channels"

		val codec = KotlinSerializerCodec.create<WelcomeChannelEntity>()

		private val FilterFunctions = object {
			fun byId(id: Snowflake) =
				eq(WelcomeChannelEntity::_id.name, id)
		}

		private val collection by lazy {
			MongoDB.getCollection<WelcomeChannelEntity>(COLLECTION_NAME)
		}

		override suspend fun getUrlForChannel(channelId: Snowflake): String? =
			collection
				.find<WelcomeChannelEntity>(FilterFunctions.byId(channelId))
				.limit(1)
				.firstOrNull()
				?.url

		override suspend fun getChannelURLs(): Map<Snowflake, String> =
			collection
				.find<WelcomeChannelEntity>()
				.map { it._id to it.url }
				.toList()
				.toMap()

		override suspend fun setUrlForChannel(channelId: Snowflake, url: String) {
			save(WelcomeChannelEntity(channelId, url))
		}

		override suspend fun removeChannel(channelId: Snowflake): String? =
			collection.findOneAndDelete(
				FilterFunctions.byId(channelId),
			)?.url

		suspend fun save(document: WelcomeChannelEntity): UpdateResult =
			collection
				.replaceOne(
					FilterFunctions.byId(document._id),
					document,
					ReplaceOptions().upsert(true)
				)
	}
}
